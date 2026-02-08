$(document).ready(async function () {
    const urlParams = new URLSearchParams(window.location.search);
    const matchId = urlParams.get('matchId');

    if (!matchId) {
        alert('No Match ID provided!');
        window.location.href = 'bracket.html';
        return;
    }

    // State
    const state = {
        heroes: {}, // Metadata from RosterService
        match: null,
        teamA: null,
        teamB: null,
        rosters: { // heroId -> { ...hero, teamId, currentHealth }
        },
        teamAffiliation: {}, // heroId -> teamId
    };

    // UI Elements
    const els = {
        status: $('#battleStatus'),
        log: $('#battleLog'),
        teamAName: $('#teamAName'),
        teamBName: $('#teamBName'),
        teamARoster: $('#teamARoster'),
        teamBRoster: $('#teamBRoster'),
        winnerModal: $('#winnerModal'),
        winnerName: $('#winnerName')
    };

    // Helper: Log message
    function log(msg, type = 'info') {
        const color = type === 'damage' ? 'text-red-400' : (type === 'special' ? 'text-yellow-400 font-bold' : 'text-slate-300');
        const time = new Date().toLocaleTimeString();
        const el = $(`<div class="${color} border-l-2 border-slate-700 pl-2 mb-1"><span class="text-slate-500 text-xs mr-2">[${time}]</span>${msg}</div>`);
        els.log.prepend(el);
    }

    // Helper: Render Hero Card
    function renderHeroCard(heroId, container, teamId) {
        const hero = state.heroes[heroId];
        const uniqueId = `${teamId}_${heroId}`;

        // Calculate Max HP (durability) - matching backend BattleEngine logic
        // BattleEngine uses hero.powerstats.durability() directly (no multiplier in latest version)
        // Wait, step 198 removed the multiplier? Let's check.
        // Step 198: this.currentHp = hero.powerstats().durability();
        // Step 202: (int) (hero.powerstats().durability() * multiplier.doubleValue()) in FatigueService
        // But in BattleEngine constructor it is just durability().
        // So Max HP = durability.
        const maxHealth = hero.powerstats ? hero.powerstats.durability : 100;

        // Store initial state
        state.rosters[uniqueId] = { ...hero, currentHealth: maxHealth, maxHealth: maxHealth };

        const card = $(`
            <div id="hero-${uniqueId}" class="hero-card bg-slate-800 p-3 rounded-lg border border-slate-700 flex items-center gap-3 relative overflow-hidden">
                <img src="${hero.images?.sm || hero.images?.md || hero.imageUrl || 'https://via.placeholder.com/50'}" class="w-12 h-12 rounded-full border-2 border-slate-500 object-cover" alt="${hero.name}">
                <div class="flex-1">
                    <div class="flex justify-between items-center">
                        <span class="font-bold text-sm text-white">${hero.name}</span>
                        <span class="text-xs text-slate-400 font-mono hp-text">${maxHealth}/${maxHealth}</span>
                    </div>
                    <div class="w-full bg-slate-700 h-2 rounded-full mt-1 overflow-hidden">
                        <div class="hp-bar bg-green-500 h-full w-full transition-all duration-300"></div>
                    </div>
                </div>
            </div>
        `);
        container.append(card);
    }

    try {
        els.status.text('LOADING MATCH DATA...');

        // 1. Load Heroes Metadata
        const heroesList = await API.teams.getHeroes();
        state.heroes = heroesList.reduce((acc, h) => ({ ...acc, [h.id]: h }), {});

        // 2. Load Match Info
        state.match = await API.matches.get(matchId);

        // 2b. Load Round Info to get Map Type
        try {
            const roundSpec = await API.rounds.get(state.match.roundNo);
            const mapType = roundSpec.mapType;
            let bgImage = 'images/backgrounds/arena_1.png'; // Default

            if (mapType === 'ARENA_2') bgImage = 'images/backgrounds/arena_2.png';
            else if (mapType === 'COSMIC') bgImage = 'images/backgrounds/cosmic.png';

            // Apply background
            $('body').css({
                'background-image': `linear-gradient(rgba(15, 23, 42, 0.7), rgba(15, 23, 42, 0.8)), url('${bgImage}')`,
                'background-size': 'cover',
                'background-position': 'center',
                'background-attachment': 'fixed'
            });

        } catch (e) {
            console.warn('Failed to load round spec for background', e);
        }

        // 3. Load Team Names
        const allTeams = await API.teams.list();
        state.teamA = allTeams.find(t => t.teamId === state.match.teamA);
        state.teamB = allTeams.find(t => t.teamId === state.match.teamB);

        els.teamAName.text(state.teamA ? state.teamA.name : 'Unknown Team A');
        els.teamBName.text(state.teamB ? state.teamB.name : 'Unknown Team B');

        // 4. Load Rosters (Submissions)
        // Try to fetch submissions for current round (or 1)
        const roundNo = state.match.roundNo || 1;

        try {
            const subA = await API.rounds.getSubmission(roundNo, state.match.teamA);
            subA.heroIds.forEach(id => {
                state.teamAffiliation[id] = state.match.teamA;
                renderHeroCard(id, els.teamARoster, state.match.teamA);
            });
        } catch (e) {
            console.warn('Failed to load roster A', e);
            els.teamARoster.html('<div class="text-red-500 text-xs">Roster hidden/failed</div>');
        }

        try {
            const subB = await API.rounds.getSubmission(roundNo, state.match.teamB);
            subB.heroIds.forEach(id => {
                state.teamAffiliation[id] = state.match.teamB;
                renderHeroCard(id, els.teamBRoster, state.match.teamB);
            });
        } catch (e) {
            console.warn('Failed to load roster B', e);
            els.teamBRoster.html('<div class="text-red-500 text-xs">Roster hidden/failed</div>');
        }

        // 5. Connect SSE
        els.status.text('Connecting Stream...');
        const evtSource = API.matches.eventsStream(matchId);

        evtSource.onmessage = function (e) {
            const event = JSON.parse(e.data);
            processEvent(event);
        };

        evtSource.onerror = function () {
            els.status.text('STREAM CLOSED / FINISHED');
            // If match is completed, we might just be done.
            // evtSource.close();
        };

        els.status.text('LIVE REPLAY');


    } catch (err) {
        console.error(err);
        alert('Failed to initialize battle: ' + err.message);
    }

    // Event Processor
    function processEvent(event) {
        console.log('Event:', event);
        const { type, description, actorId, targetId, value } = event;

        switch (type) {
            case 'MATCH_START':
                log('====== MATCH START ======', 'special');
                break;
            case 'ROUND_START':
                log(`--- Round ${value} Start ---`, 'special');
                $('#roundIndicator').text(`ROUND ${value}`).removeClass('hidden');
                break;
            case 'ROUND_END':
                log(`--- Round ${value} End ---`, 'special');
                break;
            case 'ATTACK_PERFORMED':
                handleAttack(actorId, targetId, value, description);
                break;
            case 'HERO_KNOCKED_OUT':
                handleKO(targetId);
                log(description, 'damage');
                break;
            case 'MATCH_END':
                handleWin(value); // value is winning team ID
                break;
            case 'HIT': // Fallthrough or explicit
                handleAttack(actorId, targetId, value, description);
                break;
            default:
                log(description);
        }
    }

    function handleAttack(actorId, targetId, damage, desc) {
        // IDs are now composite strings from backend: teamId_heroId
        // But we need to be careful. DOM IDs are hero-{uniqueId}
        // Using document.getElementById to avoid any selector escaping issues
        const actorCard = $(document.getElementById(`hero-${actorId}`));
        const targetCard = $(document.getElementById(`hero-${targetId}`));

        // Visuals
        const actorTeamId = actorId.split('_')[0];
        const isTeamA = actorTeamId === state.match.teamA;

        if (actorCard.length) {
            actorCard.addClass(isTeamA ? 'attacking-right' : 'attacking-left');
            setTimeout(() => {
                actorCard.removeClass('attacking-right attacking-left');
            }, 500);
        }

        if (targetCard.length) {
            targetCard.addClass('target shake');
            setTimeout(() => {
                targetCard.removeClass('target shake');
            }, 500);

            const dmgEl = $(`<div class="damage-number">-${damage}</div>`);
            targetCard.append(dmgEl);
            setTimeout(() => dmgEl.remove(), 1000);
        } else {
            console.warn(`Target card not found for ID: hero-${targetId}`);
        }

        // updateHealth should run regardless of UI presence to keep state consistent
        updateHealth(targetId, -damage);

        log(desc);
    }

    // NOTE: If the backend sends HEALTH_CHANGED events separately, use them.
    // If NOT, we rely on attack damage.
    // Looking at MatchEventType: HEALTH_CHANGED might exist?
    // Let's handle it if it comes.

    function updateHealth(uniqueId, deltaOrExact, isExact = false) {
        const heroData = state.rosters[uniqueId];
        if (!heroData) {
            console.warn(`Health update failed: No hero data for ${uniqueId}`);
            return;
        }

        const oldHp = heroData.currentHealth;
        if (isExact) {
            heroData.currentHealth = deltaOrExact;
        } else {
            heroData.currentHealth += deltaOrExact;
        }
        if (heroData.currentHealth < 0) heroData.currentHealth = 0;

        console.debug(`Updated Health for ${heroData.name} (${uniqueId}): ${oldHp} -> ${heroData.currentHealth} (Max: ${heroData.maxHealth})`);

        // Update UI
        const pct = Math.max(0, Math.min(100, (heroData.currentHealth / heroData.maxHealth) * 100));
        const card = $(document.getElementById(`hero-${uniqueId}`));

        if (card.length) {
            card.find('.hp-bar').css('width', `${pct}%`).removeClass('bg-green-500 bg-yellow-500 bg-red-500')
                .addClass(pct > 50 ? 'bg-green-500' : (pct > 20 ? 'bg-yellow-500' : 'bg-red-500'));
            card.find('.hp-text').text(`${Math.ceil(heroData.currentHealth)}/${heroData.maxHealth}`);
        } else {
            console.warn(`UI update skipped: Card #hero-${uniqueId} not found`);
        }
    }

    function handleKO(uniqueId) {
        const card = $(document.getElementById(`hero-${uniqueId}`));
        card.addClass('ko');
        updateHealth(uniqueId, 0, true);
    }

    function handleWin(winnerTeamId) {
        log('Match Ended!', 'special');
        $('#winnerModal').removeClass('hidden').addClass('flex');

        const winner = (winnerTeamId === state.match.teamA) ? state.teamA : state.teamB;
        if (winner) els.winnerName.text(winner.name);
    }

});
