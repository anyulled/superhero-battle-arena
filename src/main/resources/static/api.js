const API = {
    sessions: {
        active: () => $.get('/api/sessions/active')
    },
    teams: {
        list: (sessionId) => $.get('/api/teams', sessionId ? { sessionId } : {}),
        getHeroes: () => $.get('/api/teams/heroes'),
        register: (name, members) => $.post('/api/teams/register', { name, members })
    },
    matches: {
        list: () => $.get('/api/matches'),
        create: (teamA, teamB) => $.post('/api/matches/create', { teamA, teamB }),
        autoMatch: (roundNo) => $.post('/api/matches/auto-match', { roundNo }),
        get: (id) => $.get(`/api/matches/${id}`),
        run: (id) => $.post(`/api/matches/${id}/run`),
        eventsStream: (id) => new EventSource(`/api/matches/${id}/events/stream`)
    },
    rounds: {
        get: (roundNo) => $.get(`/api/rounds/${roundNo}`),
        getSubmission: (roundNo, teamId) => $.get(`/api/rounds/${roundNo}/submission?teamId=${teamId}`),
        submit: (roundNo, teamId, draft) => $.ajax({
            url: `/api/rounds/${roundNo}/submit?teamId=${teamId}`,
            type: 'POST',
            contentType: 'application/json',
            data: JSON.stringify(draft)
        })
    }
};
