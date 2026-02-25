$(document).ready(function () {
    let currentSessionId = null;
    const baseUrl = window.location.origin;

    function checkActiveSession() {
        API.sessions.active()
            .done(function (session) {
                currentSessionId = session.sessionId;
                $('#sessionStatus').html(`<span class="text-green-400 font-mono text-sm">Active Session: ${session.sessionId}</span>`);

                loadTeams();
            })
            .fail(function () {
                $('#sessionStatus').html('<span class="text-red-400 font-mono text-sm">No Active Session</span>');
                $('#teamsBody').html('<tr><td colspan="3" class="text-center py-4 text-slate-500">No active session found. Please ask an admin to create one.</td></tr>');
            });
    }

    // Load Teams
    function loadTeams() {
        if (!currentSessionId) return;

        API.teams.list(currentSessionId).done(function (teams) {
            const tbody = $('#teamsBody');
            tbody.empty();
            if (teams.length === 0) {
                tbody.html('<tr><td colspan="3" class="text-center py-4 text-slate-500">No teams registered yet. Be the first!</td></tr>');
                return;
            }
            teams.forEach(team => {
                tbody.append(`
            <tr class="hover:bg-slate-800/50">
                <td class="px-6 py-4 font-medium text-white">${team.name}</td>
                <td class="px-6 py-4">${team.members.join(', ')}</td>
                <td class="px-6 py-4 font-mono text-xs text-slate-500">${team.teamId}</td>
            </tr>
        `);
            });
            lucide.createIcons();
        });
    }

    $('#refreshTeams').click(loadTeams);
    checkActiveSession();
    setInterval(function () {
        if (currentSessionId) loadTeams();
        else checkActiveSession();
    }, 5000); // Poll every 5 seconds
    lucide.createIcons();
});
