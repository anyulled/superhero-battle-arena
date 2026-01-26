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
    function renderHeroCard(heroId, container) {
        const hero = state.heroes[heroId];
        const currentHealth = 100; // Default start
        // Store initial state
        state.rosters[heroId] = { ...hero, currentHealth: 100, maxHealth: 100 }; // Assuming 100 for now, or fetch from stats?
        // Actually stats are: strength, defense... health is usually derived?
        // Let's assume 100 HP base for visualization if not provided.
        // Or better: `hero.durability` * 10?
        // The simulation engine determines max health. But we don't have it here initially?
        // We will update it on first health update or assume standard.
        // Let's assume 100 for percentage bar.

        const card = $(`
            <div id="hero-${heroId}" class="hero-card bg-slate-800 p-3 rounded-lg border border-slate-700 flex items-center gap-3 relative overflow-hidden">
                <img src="${hero.imageUrl || 'https://via.placeholder.com/50'}" class="w-12 h-12 rounded-full border-2 border-slate-500 object-cover" alt="${hero.name}">
                <div class="flex-1">
                    <div class="flex justify-between items-center">
                        <span class="font-bold text-sm text-white">${hero.name}</span>
                        <span class="text-xs text-slate-400 font-mono hp-text">100/100</span>
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
                renderHeroCard(id, els.teamARoster);
            });
        } catch (e) {
            console.warn('Failed to load roster A', e);
            els.teamARoster.html('<div class="text-red-500 text-xs">Roster hidden/failed</div>');
        }

        try {
            const subB = await API.rounds.getSubmission(roundNo, state.match.teamB);
            subB.heroIds.forEach(id => {
                state.teamAffiliation[id] = state.match.teamB;
                renderHeroCard(id, els.teamBRoster);
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
            default:
                log(description);
        }
    }

    function handleAttack(actorId, targetId, damage, desc) {
        const actorCard = $(`#hero-${actorId}`);
        const targetCard = $(`#hero-${targetId}`);

        // Visuals
        const isTeamA = state.teamAffiliation[actorId] === state.match.teamA;
        actorCard.addClass(isTeamA ? 'attacking-right' : 'attacking-left');
        targetCard.addClass('target');

        setTimeout(() => {
            actorCard.removeClass('attacking-right attacking-left');
            targetCard.removeClass('target');
        }, 500);

        // Show damage number
        if (targetCard.length) {
            const dmgEl = $(`<div class="damage-number">-${damage}</div>`);
            // Position roughly center
            targetCard.append(dmgEl);
            setTimeout(() => dmgEl.remove(), 1000);

            // Update Health Logic
            // The event might not carry current health, only damage.
            // But usually we get a HEALTH_CHANGED event?
            // If not, we deduct locally.
            updateHealth(targetId, -damage);
        }

        log(desc);
    }

    // NOTE: If the backend sends HEALTH_CHANGED events separately, use them.
    // If NOT, we rely on attack damage.
    // Looking at MatchEventType: HEALTH_CHANGED might exist?
    // Let's handle it if it comes.

    function updateHealth(heroId, deltaOrExact, isExact = false) {
        const heroData = state.rosters[heroId];
        if (!heroData) return;

        if (isExact) {
            heroData.currentHealth = deltaOrExact;
        } else {
            heroData.currentHealth += deltaOrExact;
        }
        if (heroData.currentHealth < 0) heroData.currentHealth = 0;

        // Update UI
        const pct = Math.max(0, Math.min(100, (heroData.currentHealth / heroData.maxHealth) * 100)); // Assuming max 100
        const card = $(`#hero-${heroId}`);
        card.find('.hp-bar').css('width', `${pct}%`).removeClass('bg-green-500 bg-yellow-500 bg-red-500')
            .addClass(pct > 50 ? 'bg-green-500' : (pct > 20 ? 'bg-yellow-500' : 'bg-red-500'));
        card.find('.hp-text').text(`${Math.ceil(heroData.currentHealth)}HP`);
    }

    function handleKO(heroId) {
        $(`#hero-${heroId}`).addClass('ko');
        updateHealth(heroId, 0, true);
    }

    function handleWin(winnerTeamId) {
        log('Match Ended!', 'special');
        $('#winnerModal').removeClass('hidden').addClass('flex');

        const winner = (winnerTeamId === state.match.teamA) ? state.teamA : state.teamB;
        if (winner) els.winnerName.text(winner.name);
    }

});
