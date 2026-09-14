$(document).ready(function () {
    let teamsCache = {};
    let teamsList = [];
    let allMatches = [];
    let roundsList = [];
    let currentSessionId = null;
    let autoRefreshTimer = null;

    function loadTeams(sessionId) {
        return API.teams.list(sessionId || undefined).then(teams => {
            teamsList = teams || [];
            teamsCache = {};
            teamsList.forEach(t => {
                teamsCache[t.teamId] = t;
            });
            renderTeamsTab();
        });
    }

    function renderMatchCard(match, isHighlight) {
        const teamA = teamsCache[match.teamA];
        const teamB = teamsCache[match.teamB];
        const teamAName = teamA ? teamA.name : (match.teamA ? 'Team A' : 'TBD');
        const teamBName = teamB ? teamB.name : (match.teamB ? 'Team B' : 'TBD');

        const isAWinner = match.winnerTeam === match.teamA;
        const isBWinner = match.winnerTeam === match.teamB;

        let statusColor = 'bg-slate-800 text-slate-400 border border-slate-700';
        let statusText = match.status;
        let actionBtn = '';

        if (match.status === 'COMPLETED') {
            statusColor = 'bg-emerald-950/60 text-emerald-300 border border-emerald-500/40';
            actionBtn = `<a href="battle.html?matchId=${match.matchId}" class="block w-full text-center bg-blue-600/20 hover:bg-blue-600/40 text-blue-300 border border-blue-500/30 py-1.5 rounded-lg transition-colors uppercase text-xs font-bold tracking-wider flex items-center justify-center gap-1.5"><i data-lucide="play" class="w-3.5 h-3.5"></i> Watch Replay</a>`;
        } else if (match.status === 'RUNNING') {
            statusColor = 'bg-amber-950/60 text-amber-300 border border-amber-500/40';
            actionBtn = `<a href="battle.html?matchId=${match.matchId}" class="block w-full text-center bg-amber-600/20 hover:bg-amber-600/40 text-amber-300 border border-amber-500/30 py-1.5 rounded-lg transition-colors uppercase text-xs font-bold tracking-wider animate-pulse flex items-center justify-center gap-1.5"><i data-lucide="radio" class="w-3.5 h-3.5"></i> Watch Live</a>`;
        } else {
            statusText = 'READY';
            actionBtn = `<a href="battle.html?matchId=${match.matchId}" class="block w-full text-center bg-slate-800/80 text-slate-400 hover:text-white border border-slate-700 hover:border-slate-600 py-1.5 rounded-lg transition-colors uppercase text-xs font-bold tracking-wider flex items-center justify-center gap-1.5"><i data-lucide="swords" class="w-3.5 h-3.5"></i> View Match</a>`;
        }

        const borderClass = isHighlight ? 'border-yellow-500/50 shadow-yellow-500/10 shadow-lg' : 'border-slate-700/80';

        return `
            <article class="bg-slate-900/80 rounded-xl p-4 border ${borderClass} hover:border-slate-600 transition-all shadow-md relative overflow-hidden group">
                <div class="flex justify-between items-center mb-3">
                    <span class="text-[10px] font-mono uppercase tracking-wider text-slate-400">Match #${match.matchId.substring(0, 6)}</span>
                    <span class="px-2 py-0.5 rounded text-[10px] font-bold ${statusColor}">${statusText}</span>
                </div>

                <div class="space-y-2 mb-3">
                    <div class="flex justify-between items-center p-2.5 rounded-lg bg-slate-950/60 ${isAWinner ? 'border border-emerald-500/40 bg-emerald-950/20 shadow-[0_0_12px_rgba(16,185,129,0.15)]' : ''}">
                        <div class="flex items-center gap-2 truncate">
                            <span class="w-2 h-2 rounded-full ${isAWinner ? 'bg-emerald-400' : 'bg-slate-600'}"></span>
                            <span class="font-semibold text-sm truncate ${isAWinner ? 'text-emerald-300' : 'text-slate-200'}">${teamAName}</span>
                        </div>
                        ${isAWinner ? '<i data-lucide="crown" class="w-4 h-4 text-yellow-400 shrink-0"></i>' : ''}
                    </div>

                    <div class="text-[10px] text-center font-bold text-slate-500 tracking-wider">VS</div>

                    <div class="flex justify-between items-center p-2.5 rounded-lg bg-slate-950/60 ${isBWinner ? 'border border-emerald-500/40 bg-emerald-950/20 shadow-[0_0_12px_rgba(16,185,129,0.15)]' : ''}">
                        <div class="flex items-center gap-2 truncate">
                            <span class="w-2 h-2 rounded-full ${isBWinner ? 'bg-emerald-400' : 'bg-slate-600'}"></span>
                            <span class="font-semibold text-sm truncate ${isBWinner ? 'text-emerald-300' : 'text-slate-200'}">${teamBName}</span>
                        </div>
                        ${isBWinner ? '<i data-lucide="crown" class="w-4 h-4 text-yellow-400 shrink-0"></i>' : ''}
                    </div>
                </div>

                ${actionBtn}
            </article>
        `;
    }

    function renderEmptyStage(container, message) {
        container.html(`
            <div class="p-6 text-center text-slate-500 bg-slate-900/40 rounded-xl border border-dashed border-slate-800 text-xs">
                ${message}
            </div>
        `);
    }

    function resolveFinalMatches() {
        const round3Matches = allMatches.filter(m => m.roundNo === 3);
        const round4Matches = allMatches.filter(m => m.roundNo === 4);

        const sfCompleted = round3Matches.filter(m => m.status === 'COMPLETED' && m.winnerTeam);
        let sfWinners = [];
        let sfLosers = [];

        if (sfCompleted.length >= 2) {
            sfCompleted.forEach(sf => {
                const winner = sf.winnerTeam;
                const loser = sf.winnerTeam === sf.teamA ? sf.teamB : sf.teamA;
                sfWinners.push(winner);
                if (loser) sfLosers.push(loser);
            });
        }

        let finalMatch = null;
        let thirdPlaceMatch = null;

        if (round4Matches.length > 0) {
            if (sfWinners.length >= 2) {
                finalMatch = round4Matches.find(m =>
                    (m.teamA === sfWinners[0] && m.teamB === sfWinners[1]) ||
                    (m.teamA === sfWinners[1] && m.teamB === sfWinners[0])
                );
            }
            if (sfLosers.length >= 2) {
                thirdPlaceMatch = round4Matches.find(m =>
                    (m.teamA === sfLosers[0] && m.teamB === sfLosers[1]) ||
                    (m.teamA === sfLosers[1] && m.teamB === sfLosers[0])
                );
            }

            if (!finalMatch && round4Matches.length > 0) {
                finalMatch = round4Matches[0];
            }
            if (!thirdPlaceMatch && round4Matches.length > 1) {
                thirdPlaceMatch = round4Matches[1];
            }
        }

        return { sfCompleted, finalMatch, thirdPlaceMatch };
    }

    function renderBracket() {
        const round1Matches = allMatches.filter(m => m.roundNo === 1);
        const round2Matches = allMatches.filter(m => m.roundNo === 2);
        const round3Matches = allMatches.filter(m => m.roundNo === 3);
        const round4Matches = allMatches.filter(m => m.roundNo === 4);

        updateStepper(round1Matches, round2Matches, round3Matches, round4Matches);

        const colInitial = $('#colInitialMatches');
        colInitial.empty();
        if (round1Matches.length === 0) {
            renderEmptyStage(colInitial, 'Waiting for Initial Round matchmaking...');
        } else {
            round1Matches.forEach(m => colInitial.append(renderMatchCard(m, false)));
        }

        const colQuarter = $('#colQuarterMatches');
        colQuarter.empty();
        if (round2Matches.length === 0) {
            renderEmptyStage(colQuarter, 'Quarter Finals will unlock after Round 1');
        } else {
            round2Matches.forEach(m => colQuarter.append(renderMatchCard(m, false)));
        }

        const colSemi = $('#colSemiMatches');
        colSemi.empty();
        if (round3Matches.length === 0) {
            renderEmptyStage(colSemi, 'Semifinals will unlock after Quarter Finals');
        } else {
            round3Matches.forEach(m => colSemi.append(renderMatchCard(m, true)));
        }

        const colFinal = $('#colFinalMatch');
        const colThird = $('#colThirdPlaceMatch');
        colFinal.empty();
        colThird.empty();

        const { sfCompleted, finalMatch, thirdPlaceMatch } = resolveFinalMatches();

        if (finalMatch) {
            colFinal.html(renderMatchCard(finalMatch, true));
        } else {
            renderEmptyStage(colFinal, 'Grand Final match will be set after Semifinals');
        }

        if (thirdPlaceMatch) {
            colThird.html(renderMatchCard(thirdPlaceMatch, false));
        } else {
            renderEmptyStage(colThird, '3rd vs 4th Place match will be set after Semifinals');
        }

        renderPodium(sfCompleted, finalMatch, thirdPlaceMatch);
        lucide.createIcons();
    }

    function updateStepper(r1, r2, r3, r4) {
        function setStep(id, matches) {
            const el = $(`#${id}`);
            if (matches.length === 0) {
                el.removeClass('border-green-500/50 bg-green-950/20 border-yellow-500/50 bg-yellow-950/20')
                  .addClass('border-slate-700 bg-slate-900/50 opacity-60');
            } else if (matches.every(m => m.status === 'COMPLETED')) {
                el.removeClass('border-slate-700 bg-slate-900/50 opacity-60 border-yellow-500/50 bg-yellow-950/20')
                  .addClass('border-green-500/50 bg-green-950/20');
            } else {
                el.removeClass('border-slate-700 bg-slate-900/50 opacity-60 border-green-500/50 bg-green-950/20')
                  .addClass('border-yellow-500/50 bg-yellow-950/20');
            }
        }

        setStep('step-initial', r1);
        setStep('step-qf', r2);
        setStep('step-sf', r3);
        setStep('step-finals', r4);
    }

    function renderPodium(sfCompleted, finalMatch, thirdPlaceMatch) {
        let firstTeam = null;
        let secondTeam = null;
        let thirdTeam = null;
        let fourthTeam = null;

        if (finalMatch && finalMatch.status === 'COMPLETED' && finalMatch.winnerTeam) {
            firstTeam = teamsCache[finalMatch.winnerTeam];
            const runnerUpId = finalMatch.winnerTeam === finalMatch.teamA ? finalMatch.teamB : finalMatch.teamA;
            secondTeam = teamsCache[runnerUpId];
        }

        if (thirdPlaceMatch && thirdPlaceMatch.status === 'COMPLETED' && thirdPlaceMatch.winnerTeam) {
            thirdTeam = teamsCache[thirdPlaceMatch.winnerTeam];
            const fourthId = thirdPlaceMatch.winnerTeam === thirdPlaceMatch.teamA ? thirdPlaceMatch.teamB : thirdPlaceMatch.teamA;
            fourthTeam = teamsCache[fourthId];
        }

        if (sfCompleted.length >= 2) {
            const sfWinners = sfCompleted.map(m => m.winnerTeam);
            const sfLosers = sfCompleted.map(m => m.winnerTeam === m.teamA ? m.teamB : m.teamA);

            if (!firstTeam && sfWinners.length >= 2) {
                $('#winnerFirstTeam').text(`${teamsCache[sfWinners[0]]?.name || 'TBD'} or ${teamsCache[sfWinners[1]]?.name || 'TBD'}`);
                $('#winnerFirstMembers').text('Awaiting Grand Final Result');
            }
            if (!secondTeam && sfWinners.length >= 2) {
                $('#winnerSecondTeam').text('Grand Final Runner-up');
                $('#winnerSecondMembers').text('Awaiting Grand Final Result');
            }
            if (!thirdTeam && sfLosers.length >= 2) {
                $('#winnerThirdTeam').text(`${teamsCache[sfLosers[0]]?.name || 'TBD'} or ${teamsCache[sfLosers[1]]?.name || 'TBD'}`);
                $('#winnerThirdMembers').text('Awaiting 3rd Place Match');
            }
            if (!fourthTeam && sfLosers.length >= 2) {
                $('#winnerFourthTeam').text('3rd Place Match Runner-up');
                $('#winnerFourthMembers').text('Awaiting 3rd Place Match');
            }
        }

        if (firstTeam) {
            $('#winnerFirstTeam').text(firstTeam.name);
            $('#winnerFirstMembers').text(firstTeam.members ? firstTeam.members.join(', ') : 'Registered squad');
        }
        if (secondTeam) {
            $('#winnerSecondTeam').text(secondTeam.name);
            $('#winnerSecondMembers').text(secondTeam.members ? secondTeam.members.join(', ') : 'Registered squad');
        }
        if (thirdTeam) {
            $('#winnerThirdTeam').text(thirdTeam.name);
            $('#winnerThirdMembers').text(thirdTeam.members ? thirdTeam.members.join(', ') : 'Registered squad');
        }
        if (fourthTeam) {
            $('#winnerFourthTeam').text(fourthTeam.name);
            $('#winnerFourthMembers').text(fourthTeam.members ? fourthTeam.members.join(', ') : 'Registered squad');
        }
    }

    function renderTeamsTab() {
        const tbody = $('#registeredTeamsTbody');
        const badge = $('#teamsCountBadge');
        tbody.empty();

        badge.text(`${teamsList.length} Teams Registered`);

        if (teamsList.length === 0) {
            tbody.append('<tr><td colspan="3" class="px-6 py-6 text-center text-slate-500">No teams registered for this session yet.</td></tr>');
            return;
        }

        const { finalMatch, thirdPlaceMatch } = resolveFinalMatches();

        teamsList.forEach(team => {
            const membersStr = (team.members && team.members.length > 0)
                ? team.members.join(', ')
                : 'No members listed';

            const teamMatches = allMatches.filter(m => m.teamA === team.teamId || m.teamB === team.teamId);
            let statusBadge = '<span class="px-2.5 py-0.5 rounded-full text-xs font-semibold bg-slate-800 text-slate-400">Registered</span>';

            const isChampion = finalMatch && finalMatch.status === 'COMPLETED' && finalMatch.winnerTeam === team.teamId;
            const isRunnerUp = finalMatch && finalMatch.status === 'COMPLETED' &&
                (finalMatch.teamA === team.teamId || finalMatch.teamB === team.teamId) &&
                finalMatch.winnerTeam !== team.teamId;

            const isThirdPlace = thirdPlaceMatch && thirdPlaceMatch.status === 'COMPLETED' && thirdPlaceMatch.winnerTeam === team.teamId;
            const isFourthPlace = thirdPlaceMatch && thirdPlaceMatch.status === 'COMPLETED' &&
                (thirdPlaceMatch.teamA === team.teamId || thirdPlaceMatch.teamB === team.teamId) &&
                thirdPlaceMatch.winnerTeam !== team.teamId;

            const inFinals = allMatches.some(m => m.roundNo === 4 && (m.teamA === team.teamId || m.teamB === team.teamId));
            const inSemis = allMatches.some(m => m.roundNo === 3 && (m.teamA === team.teamId || m.teamB === team.teamId));

            if (isChampion) {
                statusBadge = '<span class="px-2.5 py-0.5 rounded-full text-xs font-bold bg-yellow-500/20 text-yellow-300 border border-yellow-500/40">🥇 Champion</span>';
            } else if (isRunnerUp) {
                statusBadge = '<span class="px-2.5 py-0.5 rounded-full text-xs font-bold bg-slate-400/20 text-slate-200 border border-slate-400/40">🥈 Runner-up</span>';
            } else if (isThirdPlace) {
                statusBadge = '<span class="px-2.5 py-0.5 rounded-full text-xs font-bold bg-amber-700/20 text-amber-300 border border-amber-600/40">🥉 3rd Place</span>';
            } else if (isFourthPlace) {
                statusBadge = '<span class="px-2.5 py-0.5 rounded-full text-xs font-bold bg-blue-900/20 text-blue-300 border border-blue-600/30">4th Place</span>';
            } else if (inFinals) {
                statusBadge = '<span class="px-2.5 py-0.5 rounded-full text-xs font-bold bg-purple-500/20 text-purple-300 border border-purple-500/40">Stage 4 Finalist</span>';
            } else if (inSemis) {
                statusBadge = '<span class="px-2.5 py-0.5 rounded-full text-xs font-bold bg-amber-500/20 text-amber-300 border border-amber-500/40">Top 4 Semifinalist</span>';
            } else if (teamMatches.length > 0) {
                const maxRound = Math.max(...teamMatches.map(m => m.roundNo));
                statusBadge = `<span class="px-2.5 py-0.5 rounded-full text-xs font-medium bg-blue-500/20 text-blue-300">Round ${maxRound}</span>`;
            }

            tbody.append(`
                <tr class="hover:bg-slate-800/40 transition-colors">
                    <td class="px-6 py-4 font-bold text-white flex items-center gap-2">
                        <i data-lucide="shield" class="w-4 h-4 text-purple-400"></i>
                        ${team.name}
                    </td>
                    <td class="px-6 py-4 text-slate-300 text-xs">
                        ${membersStr}
                    </td>
                    <td class="px-6 py-4">
                        ${statusBadge}
                    </td>
                </tr>
            `);
        });

        lucide.createIcons();
    }

    function renderRoundConstraintsTab() {
        const select = $('#roundConstraintSelect');
        const card = $('#roundSpecCard');
        const submissionsContainer = $('#roundSubmissionsContainer');

        const selectedRoundNo = parseInt(select.val(), 10) || (roundsList[0] ? roundsList[0].roundNo : 1);

        select.find('option').remove();
        roundsList.forEach(r => {
            select.append(`<option value="${r.roundNo}" ${r.roundNo === selectedRoundNo ? 'selected' : ''}>Round ${r.roundNo} (${r.status})</option>`);
        });

        if (roundsList.length === 0) {
            card.html('<div class="text-slate-500 text-center py-4">No rounds configured for this session.</div>');
            submissionsContainer.empty();
            return;
        }

        const activeRound = roundsList.find(r => r.roundNo === selectedRoundNo) || roundsList[0];
        const spec = activeRound.specJson || activeRound.spec || {};

        const rolesBadge = (spec.allowedRoles && spec.allowedRoles.length > 0)
            ? spec.allowedRoles.map(r => `<span class="px-2 py-0.5 bg-blue-900/40 text-blue-300 border border-blue-500/30 rounded text-xs font-mono">${r}</span>`).join(' ')
            : '<span class="text-slate-500 text-xs italic">All roles allowed</span>';

        const alignBadge = (spec.allowedAlignments && spec.allowedAlignments.length > 0)
            ? spec.allowedAlignments.map(a => `<span class="px-2 py-0.5 bg-purple-900/40 text-purple-300 border border-purple-500/30 rounded text-xs font-mono">${a}</span>`).join(' ')
            : '<span class="text-slate-500 text-xs italic">All alignments allowed</span>';

        card.html(`
            <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
                <div class="space-y-1">
                    <span class="text-[11px] text-slate-400 font-semibold uppercase tracking-wider block">Round Number &amp; Status</span>
                    <div class="flex items-center gap-2">
                        <span class="text-xl font-bold text-white">Round ${activeRound.roundNo}</span>
                        <span class="px-2 py-0.5 rounded text-xs font-bold ${activeRound.status === 'OPEN' ? 'bg-green-900/50 text-green-300' : 'bg-slate-700 text-slate-300'}">${activeRound.status}</span>
                    </div>
                    <p class="text-xs text-slate-400">${spec.description || 'World Cup Stage'}</p>
                </div>

                <div class="space-y-1">
                    <span class="text-[11px] text-slate-400 font-semibold uppercase tracking-wider block">Budget &amp; Map</span>
                    <div class="text-sm font-bold text-amber-400 flex items-center gap-2">
                        <i data-lucide="coins" class="w-4 h-4"></i> Budget Cap: ${spec.budgetCap || 1000} pts
                    </div>
                    <div class="text-xs text-slate-300 flex items-center gap-2">
                        <i data-lucide="map-pin" class="w-3.5 h-3.5 text-indigo-400"></i> Map: <span class="font-semibold text-white">${spec.mapType || 'ARENA_1'}</span>
                    </div>
                    <div class="text-xs text-slate-300 flex items-center gap-2">
                        <i data-lucide="users" class="w-3.5 h-3.5 text-blue-400"></i> Team Size: <span class="font-semibold text-white">${spec.teamSize || 5} heroes</span>
                    </div>
                </div>

                <div class="space-y-2">
                    <div>
                        <span class="text-[11px] text-slate-400 font-semibold uppercase tracking-wider block mb-1">Allowed Roles</span>
                        <div class="flex flex-wrap gap-1">${rolesBadge}</div>
                    </div>
                    <div>
                        <span class="text-[11px] text-slate-400 font-semibold uppercase tracking-wider block mb-1">Allowed Alignments</span>
                        <div class="flex flex-wrap gap-1">${alignBadge}</div>
                    </div>
                </div>
            </div>
        `);

        if (currentSessionId && activeRound.roundNo) {
            API.rounds.getSubmissions(activeRound.roundNo, currentSessionId).done(function (submissions) {
                submissionsContainer.empty();
                if (!submissions || submissions.length === 0) {
                    submissionsContainer.html('<div class="col-span-full text-slate-500 text-xs italic py-2">No squads submitted for this round yet.</div>');
                    return;
                }

                submissions.forEach(sub => {
                    const team = teamsCache[sub.teamId];
                    const teamName = team ? team.name : 'Team ' + sub.teamId.substring(0, 6);
                    const subJson = sub.submissionJson || {};
                    const heroCount = sub.heroes ? sub.heroes.length : (sub.heroIds ? sub.heroIds.length : (subJson.heroIds ? subJson.heroIds.length : 0));

                    submissionsContainer.append(`
                        <div class="p-3 rounded-lg bg-slate-950/60 border border-slate-700/80 flex justify-between items-center">
                            <div class="flex items-center gap-2.5 truncate">
                                <i data-lucide="shield-check" class="w-4 h-4 text-emerald-400 shrink-0"></i>
                                <span class="font-semibold text-sm text-white truncate">${teamName}</span>
                            </div>
                            <span class="text-xs font-mono bg-blue-950 text-blue-300 px-2 py-0.5 rounded border border-blue-500/30 shrink-0">${heroCount} Heroes</span>
                        </div>
                    `);
                });
                lucide.createIcons();
            }).fail(function () {
                submissionsContainer.html('<div class="col-span-full text-slate-500 text-xs italic py-2">Unable to load submissions.</div>');
            });
        }
        lucide.createIcons();
    }

    function switchView(tabId) {
        $('.view-tab').removeClass('bg-purple-600 text-white').addClass('text-slate-400 hover:text-white');
        $(`#${tabId}`).removeClass('text-slate-400 hover:text-white').addClass('bg-purple-600 text-white');

        $('#viewBracketContainer').addClass('hidden');
        $('#viewPodiumContainer').addClass('hidden');
        $('#viewTeamsContainer').addClass('hidden');
        $('#viewRoundsContainer').addClass('hidden');

        if (tabId === 'tabBracket') {
            $('#viewBracketContainer').removeClass('hidden');
        } else if (tabId === 'tabPodium') {
            $('#viewPodiumContainer').removeClass('hidden');
        } else if (tabId === 'tabTeams') {
            $('#viewTeamsContainer').removeClass('hidden');
            renderTeamsTab();
        } else if (tabId === 'tabRounds') {
            $('#viewRoundsContainer').removeClass('hidden');
            renderRoundConstraintsTab();
        }
    }

    function refresh() {
        const sessionId = $('#sessionSelect').val() || undefined;
        currentSessionId = sessionId;

        loadTeams(sessionId).then(() => {
            if (sessionId) {
                API.rounds.list(sessionId).then(rounds => {
                    roundsList = rounds || [];
                    API.matches.list(sessionId).done(function (matches) {
                        allMatches = matches || [];
                        renderBracket();
                        renderTeamsTab();
                        renderRoundConstraintsTab();
                    });
                });
            } else {
                roundsList = [];
                API.matches.list().done(function (matches) {
                    allMatches = matches || [];
                    renderBracket();
                    renderTeamsTab();
                });
            }
        });
    }

    function setupAutoRefresh() {
        if (autoRefreshTimer) clearInterval(autoRefreshTimer);
        if ($('#autoRefreshToggle').is(':checked')) {
            $('#liveIndicator').removeClass('opacity-20').addClass('opacity-100 animate-pulse');
            autoRefreshTimer = setInterval(refresh, 8000);
        } else {
            $('#liveIndicator').removeClass('opacity-100 animate-pulse').addClass('opacity-20');
        }
    }

    function init() {
        API.sessions.list().then(sessions => {
            const select = $('#sessionSelect');
            select.empty();

            if (!sessions || sessions.length === 0) {
                select.append('<option value="">No sessions available</option>');
                refresh();
                return;
            }

            sessions
                .sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
                .forEach(s => {
                    const label = s.active ? `${s.sessionId} (Active)` : s.sessionId;
                    select.append(`<option value="${s.sessionId}">${label}</option>`);
                });

            const active = sessions.find(s => s.active);
            if (active) select.val(active.sessionId);

            const urlParams = new URLSearchParams(window.location.search);
            const initialTab = urlParams.get('tab');
            if (initialTab && ['tabBracket', 'tabPodium', 'tabTeams', 'tabRounds'].includes(initialTab)) {
                switchView(initialTab);
            }

            refresh();
            setupAutoRefresh();
        });
    }

    $('#tabBracket').on('click', () => switchView('tabBracket'));
    $('#tabPodium').on('click', () => switchView('tabPodium'));
    $('#tabTeams').on('click', () => switchView('tabTeams'));
    $('#tabRounds').on('click', () => switchView('tabRounds'));

    $('#sessionSelect').on('change', refresh);
    $('#roundConstraintSelect').on('change', renderRoundConstraintsTab);
    $('#refreshBtn').on('click', refresh);
    $('#autoRefreshToggle').on('change', setupAutoRefresh);

    init();
});
