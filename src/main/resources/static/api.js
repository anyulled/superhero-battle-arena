const API = {
    sessions: {
        active: () => $.get('/api/sessions/active'),
        list: () => $.get('/api/sessions')
    },
    teams: {
        list: (sessionId) => $.get('/api/teams', sessionId ? { sessionId } : {}),
        getHeroes: () => $.get('/api/teams/heroes'),
        register: (name, members) => $.post('/api/teams/register', { name, members })
    },
    matches: {
        list: (sessionId, roundNo) => $.get('/api/matches', Object.fromEntries(
            Object.entries({ sessionId, roundNo }).filter(([, v]) => v != null)
        )),
        create: (teamA, teamB) => $.post('/api/admin/matches/create', { teamA, teamB }),
        autoMatch: (roundNo) => $.post('/api/admin/matches/auto-match', { roundNo }),
        get: (id) => $.get(`/api/matches/${id}`),
        run: (id) => $.post(`/api/admin/matches/${id}/run`),
        eventsStream: (id) => new EventSource(`/api/matches/${id}/events/stream`)
    },
    rounds: {
        list: (sessionId) => $.get('/api/rounds', { sessionId }),
        get: (roundNo, sessionId) => $.get(`/api/rounds/${roundNo}${sessionId ? '?sessionId=' + sessionId : ''}`),
        getSubmission: (roundNo, teamId) => $.get(`/api/rounds/${roundNo}/submission?teamId=${teamId}`),
        submit: (roundNo, teamId, draft) => $.ajax({
            url: `/api/rounds/${roundNo}/submit?teamId=${teamId}`,
            type: 'POST',
            contentType: 'application/json',
            data: JSON.stringify(draft)
        })
    }
};
