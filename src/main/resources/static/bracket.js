$(document).ready(function () {

    let teamsCache = {};
    let allMatches = [];

    function loadTeams(sessionId) {
        return API.teams.list(sessionId || undefined).then(teams => {
            teams.forEach(t => teamsCache[t.teamId] = t.name);
        });
    }

    function renderMatches(matches) {
        const container = $('#matchesContainer');
        container.empty();

        if (matches.length === 0) {
            container.append('<div class="col-span-full text-center text-slate-500 py-10">No matches found.</div>');
            return;
        }

        matches.sort((a, b) => b.roundNo - a.roundNo || a.matchId.localeCompare(b.matchId));

        matches.forEach(match => {
            const teamAName = teamsCache[match.teamA] || 'Team A';
            const teamBName = teamsCache[match.teamB] || 'Team B';

            let statusColor = 'bg-slate-700 text-slate-300';
            let statusText = match.status;
            let actionBtn = '';

            if (match.status === 'COMPLETED') {
                statusColor = 'bg-green-900/50 text-green-300 border border-green-700/50';
                actionBtn = `<a href="battle.html?matchId=${match.matchId}" class="block w-full text-center bg-blue-600/20 hover:bg-blue-600/40 text-blue-300 border border-blue-500/30 py-2 rounded transition-colors uppercase text-sm font-bold tracking-wider">Watch Replay</a>`;
            } else if (match.status === 'RUNNING') {
                statusColor = 'bg-yellow-900/50 text-yellow-300 border border-yellow-700/50';
                actionBtn = `<a href="battle.html?matchId=${match.matchId}" class="block w-full text-center bg-yellow-600/20 hover:bg-yellow-600/40 text-yellow-300 border border-yellow-500/30 py-2 rounded transition-colors uppercase text-sm font-bold tracking-wider animate-pulse">Watch Live</a>`;
            } else {
                statusText = 'PENDING / READY';
                actionBtn = `<a href="battle.html?matchId=${match.matchId}" class="block w-full text-center bg-slate-700 text-slate-500 cursor-not-allowed py-2 rounded uppercase text-sm font-bold tracking-wider">Waiting for Battle</a>`;
            }

            const card = `
            <article class="bg-slate-800 rounded-xl p-6 border border-slate-700 hover:border-slate-600 transition-all shadow-lg hover:shadow-2xl relative overflow-hidden group">
               <div class="absolute top-0 right-0 p-2 opacity-10 group-hover:opacity-20 transition-opacity">
                    <span class="text-6xl font-black">VS</span>
               </div>
               
               <div class="flex flex-col gap-2 mb-4">
                    <div class="flex justify-between items-center">
                         <span class="bg-slate-900 text-slate-400 text-xs font-mono px-2 py-1 rounded">Round ${match.roundNo}</span>
                         <span class="px-2 py-1 rounded text-xs font-bold ${statusColor}">${statusText}</span>
                    </div>
                    <div class="text-[10px] text-slate-600 font-mono select-all">Session: ${match.sessionId}</div>
               </div>

               <div class="space-y-4 mb-6">
                    <div class="flex justify-between items-center p-3 bg-slate-900/50 rounded-lg ${match.winnerTeam === match.teamA ? 'border border-green-500/30 shadow-[0_0_15px_rgba(34,197,94,0.1)]' : ''}">
                        <span class="font-bold text-lg ${match.winnerTeam === match.teamA ? 'text-green-400' : 'text-slate-300'}">${teamAName}</span>
                        ${match.winnerTeam === match.teamA ? '<i data-lucide="crown" class="w-5 h-5 text-yellow-500"></i>' : ''}
                    </div>
                    <div class="flex justify-center text-xs text-slate-600 font-bold">VS</div>
                    <div class="flex justify-between items-center p-3 bg-slate-900/50 rounded-lg ${match.winnerTeam === match.teamB ? 'border border-green-500/30 shadow-[0_0_15px_rgba(34,197,94,0.1)]' : ''}">
                        <span class="font-bold text-lg ${match.winnerTeam === match.teamB ? 'text-green-400' : 'text-slate-300'}">${teamBName}</span>
                        ${match.winnerTeam === match.teamB ? '<i data-lucide="crown" class="w-5 h-5 text-yellow-500"></i>' : ''}
                    </div>
               </div>

               ${actionBtn}
            </article>
        `;
            container.append(card);
        });
        lucide.createIcons();
    }

    function renderSubmissions(submissions) {
        const section = $('#submittedTeamsSection');
        const container = $('#submittedTeamsContainer');
        container.empty();

        if (!submissions || submissions.length === 0) {
            section.addClass('hidden');
            return;
        }

        section.removeClass('hidden');
        submissions.forEach(sub => {
            const teamName = teamsCache[sub.teamId] || 'Unknown Team';
            container.append(`
                <div class="bg-blue-900/40 border border-blue-500/30 text-blue-200 px-4 py-2 rounded-lg flex items-center gap-2 shadow-sm font-medium">
                    <i data-lucide="shield-check" class="w-4 h-4 text-blue-400"></i>
                    ${teamName}
                </div>
            `);
        });
        lucide.createIcons();
    }

    function renderWinners(matches) {
        const section = $('#roundWinnersSection');
        const container = $('#roundWinnersContainer');
        container.empty();

        const winners = matches
            .filter(m => m.status === 'COMPLETED' && m.winnerTeam)
            .map(m => m.winnerTeam);

        const uniqueWinners = [...new Set(winners)];

        if (uniqueWinners.length === 0) {
            section.addClass('hidden');
            return;
        }

        section.removeClass('hidden');
        uniqueWinners.forEach(teamId => {
            const teamName = teamsCache[teamId] || 'Unknown Team';
            container.append(`
                <div class="bg-yellow-900/40 border border-yellow-500/30 text-yellow-200 px-4 py-2 rounded-lg flex items-center gap-2 shadow-sm font-bold">
                    <i data-lucide="crown" class="w-4 h-4 text-yellow-400"></i>
                    ${teamName}
                </div>
            `);
        });
        lucide.createIcons();
    }

    function applyFilters() {
        const sessionId = $('#sessionSelect').val();
        const roundNo = $('#roundSelect').val();

        let filtered = allMatches;
        if (sessionId) filtered = filtered.filter(m => m.sessionId === sessionId);
        if (roundNo) filtered = filtered.filter(m => String(m.roundNo) === String(roundNo));

        renderMatches(filtered);
        renderWinners(filtered);

        if (roundNo && sessionId) {
            API.rounds.getSubmissions(roundNo, sessionId).done(function (submissions) {
                renderSubmissions(submissions);
            }).fail(function () {
                renderSubmissions([]);
            });
        } else {
            $('#submittedTeamsSection').addClass('hidden');
        }
    }

    function populateRoundSelector(rounds) {
        const roundSelect = $('#roundSelect');
        const currentVal = roundSelect.val();
        roundSelect.find('option:not(:first)').remove();

        const roundNumbers = [...new Set(rounds.map(r => r.roundNo))].sort((a, b) => a - b);
        roundNumbers.forEach(r => roundSelect.append(`<option value="${r}">Round ${r}</option>`));
        roundSelect.prop('disabled', roundNumbers.length === 0);
        if (roundNumbers.includes(Number(currentVal))) roundSelect.val(currentVal);
    }

    function refresh() {
        const sessionId = $('#sessionSelect').val() || undefined;
        loadTeams(sessionId).then(() => {
            if (sessionId) {
                API.rounds.list(sessionId).then(rounds => {
                    populateRoundSelector(rounds);
                    API.matches.list(sessionId).done(function (matches) {
                        allMatches = matches;
                        applyFilters();
                    });
                });
            } else {
                populateRoundSelector([]);
                API.matches.list().done(function (matches) {
                    allMatches = matches;
                    applyFilters();
                });
            }
        });
    }

    function init() {
        API.sessions.list().then(sessions => {
            const select = $('#sessionSelect');
            sessions
                .sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
                .forEach(s => {
                    const label = s.active ? `${s.sessionId} (active)` : s.sessionId;
                    select.append(`<option value="${s.sessionId}">${label}</option>`);
                });

            const active = sessions.find(s => s.active);
            if (active) select.val(active.sessionId);

            refresh();
        });
    }

    $('#sessionSelect').on('change', function () {
        $('#roundSelect').val('');
        refresh();
    });

    $('#roundSelect').on('change', applyFilters);
    $('#refreshBtn').on('click', refresh);

    init();
});
