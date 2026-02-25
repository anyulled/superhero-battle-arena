let authHeader = '';
let currentSessionId = null;
let selectedRoundNo = null;
let sessions = [];

// Initialize Lucide icons
lucide.createIcons();

// Login Handler
document.getElementById('loginForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const username = document.getElementById('username').value;
    const password = document.getElementById('password').value;
    const newAuthHeader = 'Basic ' + btoa(username + ':' + password);

    try {
        // Test auth by fetching sessions
        const response = await fetch('/api/admin/sessions', {
            headers: { 'Authorization': newAuthHeader }
        });

        if (response.ok) {
            authHeader = newAuthHeader;
            localStorage.setItem('adminAuthHeader', authHeader);
            showDashboard();
        } else {
            showError('loginError', 'Invalid credentials');
        }
    } catch (error) {
        showError('loginError', 'Login failed: ' + error.message);
    }
});

function showDashboard() {
    document.getElementById('loginPanel').classList.add('hidden');
    document.getElementById('adminPanel').classList.remove('hidden');
    loadSessions();
    lucide.createIcons();
}

// Check for persistent login on load
window.addEventListener('DOMContentLoaded', async () => {
    const savedAuth = localStorage.getItem('adminAuthHeader');
    if (savedAuth) {
        authHeader = savedAuth;
        // Verify token still works
        try {
            const response = await fetch('/api/admin/sessions', {
                headers: { 'Authorization': authHeader }
            });
            if (response.ok) {
                showDashboard();
            } else {
                localStorage.removeItem('adminAuthHeader');
            }
        } catch (error) {
            console.error('Failed to verify saved auth:', error);
        }
    }
});

async function loadSessions() {
    try {
        const response = await fetch('/api/admin/sessions', {
            headers: { 'Authorization': authHeader }
        });
        if (response.ok) {
            const fetchedSessions = await response.json();
            sessions = fetchedSessions.map(s => ({
                id: s.sessionId,
                active: s.active
            }));

            if (sessions.length === 0) {
                await createSession();
            } else {
                if (!currentSessionId) {
                    currentSessionId = sessions[0].id;
                }
                updateSessionSelector();
            }
        }
    } catch (error) {
        console.error('Failed to load sessions:', error);
    }
}

function updateSessionSelector() {
    const selector = document.getElementById('sessionSelector');
    const info = document.getElementById('sessionInfo');
    selector.innerHTML = '';

    if (sessions.length === 0) {
        const option = document.createElement('option');
        option.value = "";
        option.textContent = "No sessions available";
        selector.appendChild(option);
        info.textContent = "";
        return;
    }

    sessions.forEach(session => {
        const option = document.createElement('option');
        option.value = session.id;
        option.textContent = `Session ${session.id.substring(0, 8)}...`;
        selector.appendChild(option);
    });

    if (currentSessionId) {
        selector.value = currentSessionId;
        info.textContent = `Active Session: ${currentSessionId}`;
        loadRounds();
    }

    selector.onchange = function () {
        currentSessionId = this.value;
        info.textContent = `Active Session: ${currentSessionId}`;
        loadRounds();
    };
}

async function createSession() {


    try {
        const response = await fetch('/api/admin/sessions/start', {
            method: 'POST',
            headers: { 'Authorization': authHeader }
        });

        if (response.ok) {
            const sessionId = await response.json();
            currentSessionId = sessionId;
            sessions.push({ id: sessionId });
            updateSessionSelector();
            showSuccess('sessionStatus', `Session created: ${sessionId}`);
            lucide.createIcons();
        } else {
            showError('sessionStatus', 'Failed to create session');
        }
    } catch (error) {
        showError('sessionStatus', 'Error: ' + error.message);
    }
}

async function loadRounds() {
    if (!currentSessionId) {
        document.getElementById('roundSelector').innerHTML = '<option value="">Select a session first</option>';
        document.getElementById('roundInfo').textContent = '';
        selectedRoundNo = null;
        return;
    }

    try {
        const response = await fetch(`/api/rounds?sessionId=${currentSessionId}`);
        if (response.ok) {
            const rounds = await response.json();
            const selector = document.getElementById('roundSelector');

            if (rounds.length === 0) {
                selector.innerHTML = '<option value="">No rounds created</option>';
                document.getElementById('roundInfo').textContent = 'Create a round to get started';
                selectedRoundNo = null;
            } else {
                selector.innerHTML = '';
                rounds.forEach(round => {
                    const option = document.createElement('option');
                    option.value = round.roundNo;
                    option.textContent = `Round ${round.roundNo}`;
                    selector.appendChild(option);
                });

                // Select first round if none selected
                if (!selectedRoundNo && rounds.length > 0) {
                    selectedRoundNo = rounds[0].roundNo;
                }
                selector.value = selectedRoundNo;
                document.getElementById('roundInfo').textContent = `Round ${selectedRoundNo} selected`;

                // Refresh team entries for selected round
                refreshTeamEntries();
            }

            selector.onchange = function () {
                selectedRoundNo = Number.parseInt(this.value);
                document.getElementById('roundInfo').textContent = `Round ${selectedRoundNo} selected`;
                refreshTeamEntries();
            };
        }
    } catch (error) {
        console.error('Failed to load rounds:', error);
    }
}

async function createRound() {
    if (!currentSessionId) {
        showError('roundStatus', 'Please select or create a session first');
        return;
    }

    // roundNo manual reading removed
    const teamSize = Number.parseInt(document.getElementById('newRoundTeamSize').value);
    const budgetCap = Number.parseInt(document.getElementById('newRoundBudget').value);
    const description = document.getElementById('newRoundDescription').value;
    const mapType = document.getElementById('newRoundMapType').value;

    const spec = {
        description,
        teamSize,
        budgetCap,
        requiredRoles: {},
        maxSameRole: {},
        bannedTags: [],
        tagModifiers: {},
        mapType
    };

    try {
        const response = await fetch('/api/admin/rounds/create', {
            method: 'POST',
            headers: {
                'Authorization': authHeader,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                sessionId: currentSessionId,
                // roundNo removed
                spec
            })
        });

        if (response.ok) {
            const createdRoundNo = await response.json();
            selectedRoundNo = createdRoundNo;
            showSuccess('roundStatus', `Round ${createdRoundNo} created successfully`);
            updateRoundSelector(createdRoundNo);
            lucide.createIcons();
        } else {
            showError('roundStatus', 'Failed to create round');
        }
    } catch (error) {
        showError('roundStatus', 'Error: ' + error.message);
    }
}

function updateRoundSelector(roundNo) {
    const selector = document.getElementById('roundSelector');
    const option = document.createElement('option');
    option.value = roundNo;
    option.textContent = `Round ${roundNo}`;

    if (selector.options.length === 1 && selector.options[0].value === '') {
        selector.innerHTML = '';
    }

    selector.appendChild(option);
    selector.value = roundNo;
    document.getElementById('roundInfo').textContent = `Round ${roundNo} selected`;

    selector.addEventListener('change', function () {
        selectedRoundNo = Number.parseInt(this.value);
        refreshTeamEntries();
    });
}

async function refreshTeamEntries() {
    if (!selectedRoundNo) {
        document.getElementById('teamEntriesCount').textContent = '-';
        return;
    }

    try {
        // Fetch submissions for the round
        const response = await fetch(`/api/rounds/${selectedRoundNo}/submissions?sessionId=${currentSessionId}`);
        if (response.ok) {
            const submissions = await response.json();
            const count = submissions.length;
            document.getElementById('teamEntriesCount').textContent = count;
            document.getElementById('teamEntriesCount').className = count > 0 ? 'text-6xl font-bold text-green-400 mb-2' : 'text-6xl font-bold text-slate-500 mb-2';
        }
    } catch (error) {
        console.error('Error fetching team entries:', error);
    }
}

async function autoMatch() {
    if (!currentSessionId || !selectedRoundNo) {
        showError('matchStatus', 'Please select a session and round first');
        return;
    }

    try {
        const response = await fetch(`/api/admin/matches/auto-match?sessionId=${currentSessionId}&roundNo=${selectedRoundNo}`, {
            method: 'POST',
            headers: { 'Authorization': authHeader }
        });

        if (response.ok) {
            const matchIds = await response.json();
            showSuccess('matchStatus', `Created ${matchIds.length} matches`);

            const resultsDiv = document.getElementById('matchResults');
            resultsDiv.innerHTML = matchIds.map(id =>
                `<div class="p-3 bg-slate-900/50 rounded-lg border border-slate-700 text-sm">
                    <i data-lucide="check-circle" class="w-4 h-4 inline text-green-400"></i>
                    Match ID: ${id.substring(0, 8)}...
                </div>`
            ).join('');
            lucide.createIcons();
        } else {
            showError('matchStatus', 'Failed to create matches');
        }
    } catch (error) {
        showError('matchStatus', 'Error: ' + error.message);
    }
}

async function runAllBattles() {
    if (!selectedRoundNo) {
        showError('battleStatus', 'Please select a round first');
        return;
    }

    try {
        showInfo('battleStatus', 'Running battles...');

        const response = await fetch(`/api/admin/matches/run-all?roundNo=${selectedRoundNo}${currentSessionId ? '&sessionId=' + currentSessionId : ''}`, {
            method: 'POST',
            headers: { 'Authorization': authHeader }
        });

        if (response.ok) {
            const result = await response.json();
            showSuccess('battleStatus', `Completed ${result.successfulSimulations}/${result.totalMatches} battles`);

            const resultsDiv = document.getElementById('battleResults');
            resultsDiv.innerHTML = result.matchIds.map(matchId => {
                const winner = result.winners[matchId];
                return `<div class="p-3 bg-slate-900/50 rounded-lg border border-slate-700 text-sm">
                    <i data-lucide="trophy" class="w-4 h-4 inline text-amber-400"></i>
                    Match: ${matchId.substring(0, 8)}...<br>
                    <span class="text-slate-400">Winner: ${winner || 'DRAW'}</span>
                </div>`;
            }).join('');
            lucide.createIcons();
        } else {
            showError('battleStatus', 'Failed to run battles');
        }
    } catch (error) {
        showError('battleStatus', 'Error: ' + error.message);
    }
}

function showSuccess(elementId, message) {
    document.getElementById(elementId).innerHTML =
        `<div class="p-3 bg-green-500/20 border border-green-500 rounded-lg text-green-300 text-sm flex items-center gap-2">
            <i data-lucide="check-circle" class="w-4 h-4"></i>${message}
        </div>`;
    lucide.createIcons();
}

function showError(elementId, message) {
    document.getElementById(elementId).innerHTML =
        `<div class="p-3 bg-red-500/20 border border-red-500 rounded-lg text-red-300 text-sm flex items-center gap-2">
            <i data-lucide="alert-circle" class="w-4 h-4"></i>${message}
        </div>`;
    lucide.createIcons();
}

function showInfo(elementId, message) {
    document.getElementById(elementId).innerHTML =
        `<div class="p-3 bg-blue-500/20 border border-blue-500 rounded-lg text-blue-300 text-sm flex items-center gap-2">
            <i data-lucide="info" class="w-4 h-4"></i>${message}
        </div>`;
    lucide.createIcons();
}

function logout() {
    authHeader = '';
    localStorage.removeItem('adminAuthHeader');
    currentSessionId = null;
    selectedRoundNo = null;
    sessions = [];
    document.getElementById('loginPanel').classList.remove('hidden');
    document.getElementById('adminPanel').classList.add('hidden');
    document.getElementById('loginForm').reset();
    document.getElementById('loginError').classList.add('hidden');
}
