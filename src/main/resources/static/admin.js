let authHeader = '';
let currentSessionId = null;
let selectedRoundNo = null;
let sessions = [];
let roundsCache = [];
let teamsCache = [];
let teamsById = {};
let allSessionMatches = [];
let currentRoundSubmissions = [];
let constraintOptionsPromise = null;
let autoRefreshIntervalId = null;

const STAGE_PRESETS = {
    stage1: {
        teamSize: 5,
        budgetCap: 1200,
        mapType: 'ARENA_1',
        description: 'Stage 1: Initial Round (Classified Teams)'
    },
    qf: {
        teamSize: 4,
        budgetCap: 1000,
        mapType: 'ARENA_2',
        description: 'Stage 2: Quarter Finals (8 Teams)'
    },
    sf: {
        teamSize: 3,
        budgetCap: 800,
        mapType: 'CITY',
        description: 'Stage 3: Semifinals (Top 4 Prize Winners)'
    },
    finals: {
        teamSize: 3,
        budgetCap: 700,
        mapType: 'COLOSSEUM',
        description: 'Stage 4: Grand Final & 3rd Place Match'
    }
};

const DEFAULT_FILL_TEAM_NAMES = [
    'Cosmic Defenders',
    'Shadow Warriors',
    'Quantum Squad',
    'Mystic Alliance',
    'Tech Titans',
    'Emerald Knights',
    'Thunder Legion',
    'Phantom Force',
    'Velocity Vanguard',
    'Crimson Crusaders',
    'Arctic Avengers',
    'Inferno Brigade',
    'Stealth Syndicate',
    'Psionic Protectors',
    'Aquatic Armada',
    'Savage Squad'
];

const CONSTRAINT_SELECT_IDS = [
    'newRoundRoles',
    'newRoundGenders',
    'newRoundRaces',
    'newRoundPublishers',
    'newRoundAlignments'
];

lucide.createIcons();

document.getElementById('loginForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const username = document.getElementById('username').value;
    const password = document.getElementById('password').value;
    const newAuthHeader = 'Basic ' + btoa(username + ':' + password);

    try {
        const response = await fetch('/api/admin/sessions', {
            headers: { 'Authorization': newAuthHeader }
        });

        if (response.ok) {
            authHeader = newAuthHeader;
            localStorage.setItem('adminAuthHeader', authHeader);
            showDashboard();
        } else {
            showError('loginError', 'Invalid credentials');
            document.getElementById('loginError').classList.remove('hidden');
        }
    } catch (error) {
        showError('loginError', 'Login failed: ' + error.message);
        document.getElementById('loginError').classList.remove('hidden');
    }
});

function bindDashboardEvents() {
    document.getElementById('createSessionBtn')?.addEventListener('click', createSession);
    document.getElementById('copySessionIdBtn')?.addEventListener('click', copyActiveSessionId);
    document.getElementById('createRoundBtn')?.addEventListener('click', createRound);
    document.getElementById('refreshTeamEntriesBtn')?.addEventListener('click', () => refreshAllTelemetry());
    document.getElementById('refreshDashboardBtn')?.addEventListener('click', () => refreshAllTelemetry());
    document.getElementById('autoMatchBtn')?.addEventListener('click', autoMatch);
    document.getElementById('runAllBattlesBtn')?.addEventListener('click', runAllBattles);
    document.getElementById('logoutBtn')?.addEventListener('click', logout);
    document.getElementById('clearAllConstraintsBtn')?.addEventListener('click', clearAllConstraints);
    document.getElementById('quickRegisterTeamBtn')?.addEventListener('click', registerQuickTeam);
    document.getElementById('fillPowerOfTwoBtn')?.addEventListener('click', autoFillBracketToPowerOfTwo);
    document.getElementById('createManualMatchBtn')?.addEventListener('click', createManualMatch);

    document.getElementById('autoRefreshToggle')?.addEventListener('change', configureAutoRefresh);

    document.querySelectorAll('.stage-preset-btn').forEach(btn => {
        btn.addEventListener('click', () => applyStagePreset(btn.dataset.stagePreset));
    });

    const constraintsDetails = document.getElementById('constraintsDetails');
    constraintsDetails?.addEventListener('toggle', () => {
        if (constraintsDetails.open) {
            void loadRoundConstraintOptions().catch(error => {
                console.error('Failed to load round constraint options:', error);
            });
        }
    });

    const resetBtn = document.getElementById('resetDatabaseBtn');
    const confirmGroup = document.getElementById('resetConfirmGroup');
    const confirmBtn = document.getElementById('confirmResetBtn');
    const cancelBtn = document.getElementById('cancelResetBtn');

    resetBtn?.addEventListener('click', () => {
        resetBtn.classList.add('hidden');
        confirmGroup?.classList.remove('hidden');
    });

    cancelBtn?.addEventListener('click', () => {
        confirmGroup?.classList.add('hidden');
        resetBtn?.classList.remove('hidden');
    });

    confirmBtn?.addEventListener('click', () => executeDatabaseReset());
}

window.addEventListener('DOMContentLoaded', async () => {
    bindDashboardEvents();

    const savedAuth = localStorage.getItem('adminAuthHeader');
    if (savedAuth) {
        authHeader = savedAuth;
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

function showDashboard() {
    document.getElementById('loginPanel').classList.add('hidden');
    document.getElementById('adminPanel').classList.remove('hidden');
    void loadSessions();
    configureAutoRefresh();
    lucide.createIcons();
}

function configureAutoRefresh() {
    if (autoRefreshIntervalId) {
        clearInterval(autoRefreshIntervalId);
        autoRefreshIntervalId = null;
    }

    const toggle = document.getElementById('autoRefreshToggle');
    const dot = document.getElementById('liveSyncDot');
    if (toggle && toggle.checked) {
        dot?.classList.remove('opacity-25');
        dot?.classList.add('animate-pulse');
        autoRefreshIntervalId = setInterval(() => {
            if (authHeader && currentSessionId) {
                void refreshAllTelemetry();
            }
        }, 6000);
    } else {
        dot?.classList.remove('animate-pulse');
        dot?.classList.add('opacity-25');
    }
}

async function loadRoundConstraintOptions() {
    if (constraintOptionsPromise) {
        return constraintOptionsPromise;
    }

    constraintOptionsPromise = (async () => {
        const response = await fetch('/api/admin/round-constraints/options', {
            headers: { 'Authorization': authHeader }
        });
        if (!response.ok) {
            throw new Error(`Failed to load round constraint options: ${response.status}`);
        }

        const options = await response.json();
        populateConstraintSelect('newRoundRoles', options.roles ?? []);
        populateConstraintSelect('newRoundGenders', options.genders ?? []);
        populateConstraintSelect('newRoundRaces', options.races ?? []);
        populateConstraintSelect('newRoundPublishers', options.publishers ?? []);
        populateConstraintSelect('newRoundAlignments', options.alignments ?? []);
        updateConstraintCountBadge();
    })();

    try {
        return await constraintOptionsPromise;
    } catch (error) {
        constraintOptionsPromise = null;
        throw error;
    }
}

function populateConstraintSelect(selectId, values) {
    const select = document.getElementById(selectId);
    const chipsContainer = document.getElementById(`chips-${selectId}`);
    if (!select) {
        return;
    }

    select.innerHTML = '';
    if (chipsContainer) {
        chipsContainer.innerHTML = '';
    }

    values.forEach(value => {
        const option = document.createElement('option');
        option.value = value;
        option.textContent = value;
        select.appendChild(option);

        if (chipsContainer) {
            const chip = document.createElement('button');
            chip.type = 'button';
            chip.dataset.value = value;
            chip.textContent = value;
            chip.className = 'px-2.5 py-1 rounded-full text-xs font-medium border transition-colors bg-slate-900 text-slate-300 border-slate-700 hover:border-purple-500/50';
            chip.addEventListener('click', () => {
                option.selected = !option.selected;
                syncChipAppearance(chip, option.selected);
                updateConstraintCountBadge();
            });
            chipsContainer.appendChild(chip);
        }
    });

    select.disabled = false;
}

function syncChipAppearance(chip, isSelected) {
    if (isSelected) {
        chip.className = 'px-2.5 py-1 rounded-full text-xs font-semibold border transition-colors bg-purple-600/30 text-purple-200 border-purple-400 shadow-sm';
    } else {
        chip.className = 'px-2.5 py-1 rounded-full text-xs font-medium border transition-colors bg-slate-900 text-slate-300 border-slate-700 hover:border-purple-500/50';
    }
}

function clearAllConstraints() {
    CONSTRAINT_SELECT_IDS.forEach(selectId => {
        const select = document.getElementById(selectId);
        const chipsContainer = document.getElementById(`chips-${selectId}`);
        if (select) {
            Array.from(select.options).forEach(opt => {
                opt.selected = false;
            });
        }
        if (chipsContainer) {
            chipsContainer.querySelectorAll('button').forEach(chip => syncChipAppearance(chip, false));
        }
    });
    updateConstraintCountBadge();
}

function updateConstraintCountBadge() {
    const badge = document.getElementById('activeConstraintsCountBadge');
    if (!badge) {
        return;
    }

    const totalSelected = CONSTRAINT_SELECT_IDS.reduce(
        (acc, id) => acc + getSelectedValues(id).length,
        0
    );

    if (totalSelected === 0) {
        badge.textContent = 'All heroes allowed';
        badge.className = 'text-[11px] font-mono text-slate-400';
    } else {
        badge.textContent = `${totalSelected} filter(s) active`;
        badge.className = 'text-[11px] font-mono text-purple-300 font-semibold';
    }
}

function applyStagePreset(presetKey) {
    const preset = STAGE_PRESETS[presetKey];
    if (!preset) {
        return;
    }

    document.getElementById('newRoundTeamSize').value = preset.teamSize;
    document.getElementById('newRoundBudget').value = preset.budgetCap;
    document.getElementById('newRoundMapType').value = preset.mapType;
    document.getElementById('newRoundDescription').value = preset.description;
    showInfo('roundStatus', `Applied preset: ${preset.description}`);
}

async function loadSessions() {
    try {
        const response = await fetch('/api/admin/sessions', {
            headers: { 'Authorization': authHeader }
        });
        if (response.ok) {
            const fetchedSessions = await response.json();
            sessions = fetchedSessions.map(s => ({
                id: s.sessionId,
                active: s.active,
                createdAt: s.createdAt
            }));

            if (sessions.length === 0) {
                await createSession();
            } else {
                if (!currentSessionId || !sessions.some(s => s.id === currentSessionId)) {
                    const activeSession = sessions.find(s => s.active) || sessions[0];
                    currentSessionId = activeSession.id;
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
        option.value = '';
        option.textContent = 'No sessions available';
        selector.appendChild(option);
        info.textContent = '';
        return;
    }

    sessions.forEach(session => {
        const option = document.createElement('option');
        option.value = session.id;
        const statusTag = session.active ? ' (ACTIVE)' : '';
        option.textContent = `Session ${session.id.substring(0, 8)}...${statusTag}`;
        selector.appendChild(option);
    });

    if (currentSessionId) {
        selector.value = currentSessionId;
        info.textContent = `Active Session: ${currentSessionId}`;
        void loadRounds();
    }

    selector.onchange = function () {
        currentSessionId = this.value;
        selectedRoundNo = null;
        info.textContent = `Active Session: ${currentSessionId}`;
        void loadRounds();
    };
}

async function copyActiveSessionId() {
    if (!currentSessionId || !navigator.clipboard) {
        return;
    }
    try {
        await navigator.clipboard.writeText(currentSessionId);
        showSuccess('sessionStatus', `Copied Session ID: ${currentSessionId}`);
    } catch (error) {
        console.error('Clipboard copy failed:', error);
    }
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
            selectedRoundNo = null;
            sessions.forEach(s => {
                s.active = false;
            });
            sessions.unshift({ id: sessionId, active: true });
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
            roundsCache = await response.json();
            roundsCache.sort((a, b) => a.roundNo - b.roundNo);
            const selector = document.getElementById('roundSelector');

            if (roundsCache.length === 0) {
                selector.innerHTML = '<option value="">No rounds created</option>';
                document.getElementById('roundInfo').textContent = 'Create a round to get started';
                selectedRoundNo = null;
                renderSelectedRoundSpec();
                await refreshAllTelemetry();
            } else {
                selector.innerHTML = '';
                roundsCache.forEach(round => {
                    const option = document.createElement('option');
                    option.value = round.roundNo;
                    const desc = round.spec?.description || round.specJson?.description || '';
                    const suffix = desc ? ` — ${desc}` : '';
                    option.textContent = `Round ${round.roundNo} [${round.status}]${suffix}`;
                    selector.appendChild(option);
                });

                const roundExists = roundsCache.some(r => r.roundNo === selectedRoundNo);
                if (!selectedRoundNo || !roundExists) {
                    const openRound = roundsCache.find(r => r.status === 'OPEN');
                    const fallbackRound = roundsCache[roundsCache.length - 1];
                    selectedRoundNo = (openRound || fallbackRound).roundNo;
                }

                selector.value = String(selectedRoundNo);
                updateRoundInfoBanner();
                renderSelectedRoundSpec();
                suggestNextStagePreset();
                await refreshAllTelemetry();
            }

            selector.onchange = function () {
                selectedRoundNo = Number.parseInt(this.value, 10);
                updateRoundInfoBanner();
                renderSelectedRoundSpec();
                void refreshAllTelemetry();
            };
        }
    } catch (error) {
        console.error('Failed to load rounds:', error);
    }
}

function updateRoundInfoBanner() {
    const roundObj = roundsCache.find(r => r.roundNo === selectedRoundNo);
    const infoEl = document.getElementById('roundInfo');
    if (!roundObj) {
        infoEl.textContent = selectedRoundNo ? `Round ${selectedRoundNo} selected` : '';
        return;
    }

    const statusBadgeClass = roundObj.status === 'OPEN'
        ? 'bg-emerald-500/20 text-emerald-300 border-emerald-500/40'
        : 'bg-slate-700/60 text-slate-300 border-slate-600';

    infoEl.innerHTML = `
        <span class="inline-flex items-center gap-2">
            <span>Round ${selectedRoundNo} selected</span>
            <span class="px-2 py-0.5 rounded border text-[11px] font-bold uppercase ${statusBadgeClass}">${roundObj.status}</span>
        </span>
    `;
}

function renderSelectedRoundSpec() {
    const container = document.getElementById('selectedRoundSpecSummary');
    if (!container) {
        return;
    }

    const roundObj = roundsCache.find(r => r.roundNo === selectedRoundNo);
    if (!roundObj) {
        container.innerHTML = '<span class="text-slate-400">No round selected. Use a stage preset below to create the first round.</span>';
        return;
    }

    const spec = roundObj.spec || roundObj.specJson || {};
    const roles = (spec.allowedRoles && spec.allowedRoles.length > 0)
        ? spec.allowedRoles.join(', ')
        : 'All Roles';
    const alignments = (spec.allowedAlignments && spec.allowedAlignments.length > 0)
        ? spec.allowedAlignments.join(', ')
        : 'All Alignments';

    container.innerHTML = `
        <div class="flex flex-wrap items-center gap-x-5 gap-y-1.5 text-xs">
            <span class="font-semibold text-white">${escapeHtml(spec.description || `Round ${roundObj.roundNo}`)}</span>
            <span class="text-amber-300 font-mono">Budget: ${spec.budgetCap ?? 1000} pts</span>
            <span class="text-blue-300 font-mono">Team Size: ${spec.teamSize ?? 5}</span>
            <span class="text-indigo-300 font-mono">Map: ${escapeHtml(spec.mapType || 'ARENA_1')}</span>
            <span class="text-slate-400">Roles: <strong class="text-slate-200">${escapeHtml(roles)}</strong></span>
            <span class="text-slate-400">Alignments: <strong class="text-slate-200">${escapeHtml(alignments)}</strong></span>
        </div>
    `;
}

function suggestNextStagePreset() {
    const nextRoundNo = roundsCache.length + 1;
    if (nextRoundNo === 1) {
        applyStagePresetValuesOnly(STAGE_PRESETS.stage1);
    } else if (nextRoundNo === 2) {
        applyStagePresetValuesOnly(STAGE_PRESETS.qf);
    } else if (nextRoundNo === 3) {
        applyStagePresetValuesOnly(STAGE_PRESETS.sf);
    } else if (nextRoundNo === 4) {
        applyStagePresetValuesOnly(STAGE_PRESETS.finals);
    }
}

function applyStagePresetValuesOnly(preset) {
    const sizeEl = document.getElementById('newRoundTeamSize');
    const budgetEl = document.getElementById('newRoundBudget');
    const mapEl = document.getElementById('newRoundMapType');
    const descEl = document.getElementById('newRoundDescription');
    if (sizeEl && budgetEl && mapEl && descEl) {
        sizeEl.value = preset.teamSize;
        budgetEl.value = preset.budgetCap;
        mapEl.value = preset.mapType;
        descEl.value = preset.description;
    }
}

async function createRound() {
    if (!currentSessionId) {
        showError('roundStatus', 'Please select or create a session first');
        return;
    }

    if (constraintOptionsPromise) {
        try {
            await loadRoundConstraintOptions();
        } catch (error) {
            showError('roundStatus', 'Failed to load round constraint values');
            return;
        }
    }

    const teamSize = Number.parseInt(document.getElementById('newRoundTeamSize').value, 10);
    const budgetCap = Number.parseInt(document.getElementById('newRoundBudget').value, 10);
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
        mapType,
        allowedRoles: getSelectedValues('newRoundRoles'),
        allowedGenders: getSelectedValues('newRoundGenders'),
        allowedRaces: getSelectedValues('newRoundRaces'),
        allowedPublishers: getSelectedValues('newRoundPublishers'),
        allowedAlignments: getSelectedValues('newRoundAlignments')
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
                spec
            })
        });

        if (response.ok) {
            const createdRoundNo = await response.json();
            selectedRoundNo = createdRoundNo;
            showSuccess('roundStatus', `Round ${createdRoundNo} created successfully`);
            await loadRounds();
            lucide.createIcons();
        } else {
            const err = await response.json().catch(() => null);
            showError('roundStatus', err?.detail || err?.title || 'Failed to create round');
        }
    } catch (error) {
        showError('roundStatus', 'Error: ' + error.message);
    }
}

function getSelectedValues(selectId) {
    const select = document.getElementById(selectId);
    if (!select) {
        return [];
    }
    return Array.from(select.selectedOptions, option => option.value).filter(value => value !== '');
}

async function refreshAllTelemetry() {
    await refreshTeamEntries();
    await loadSessionTeamsAndMatches();
    renderTeamReadiness();
    renderRoundMatchesAndOutcomes();
    updatePipelineTelemetry();
    const syncLabel = document.getElementById('lastSyncedLabel');
    if (syncLabel) {
        syncLabel.textContent = `Synced ${new Date().toLocaleTimeString()}`;
    }
    lucide.createIcons();
}

async function refreshTeamEntries() {
    const countEl = document.getElementById('teamEntriesCount');
    if (!selectedRoundNo || !currentSessionId) {
        countEl.textContent = '-';
        currentRoundSubmissions = [];
        return;
    }

    try {
        const response = await fetch(`/api/rounds/${selectedRoundNo}/submissions?sessionId=${currentSessionId}`);
        if (response.ok) {
            currentRoundSubmissions = await response.json();
            const count = currentRoundSubmissions.length;
            countEl.textContent = String(count);
            countEl.className = count > 0
                ? 'text-5xl font-extrabold text-emerald-400 mb-1'
                : 'text-5xl font-extrabold text-slate-500 mb-1';
        }
    } catch (error) {
        console.error('Error fetching team entries:', error);
    }
}

async function loadSessionTeamsAndMatches() {
    if (!currentSessionId) {
        teamsCache = [];
        teamsById = {};
        allSessionMatches = [];
        return;
    }

    try {
        const teamsResp = await fetch(`/api/teams?sessionId=${currentSessionId}`);
        if (teamsResp.ok) {
            teamsCache = await teamsResp.json();
            teamsById = {};
            teamsCache.forEach(t => {
                teamsById[t.teamId] = t;
            });
            populateManualMatchTeamSelectors();
        }

        const matchesResp = await fetch('/api/matches');
        if (matchesResp.ok) {
            const allMatches = await matchesResp.json();
            allSessionMatches = allMatches.filter(m => m.sessionId === currentSessionId);
        }
    } catch (error) {
        console.error('Error loading teams or matches:', error);
    }
}

function populateManualMatchTeamSelectors() {
    const selectA = document.getElementById('manualTeamASelect');
    const selectB = document.getElementById('manualTeamBSelect');
    if (!selectA || !selectB) {
        return;
    }

    const prevA = selectA.value;
    const prevB = selectB.value;

    const optionsHtml = ['<option value="">Select Team</option>']
        .concat(teamsCache.map(t => `<option value="${t.teamId}">${escapeHtml(t.name)}</option>`))
        .join('');

    selectA.innerHTML = optionsHtml;
    selectB.innerHTML = optionsHtml;

    if (prevA && teamsById[prevA]) selectA.value = prevA;
    if (prevB && teamsById[prevB]) selectB.value = prevB;
}

function computeEligibleTeamIdsForRound(roundNo) {
    if (!roundNo || roundNo <= 1) {
        return new Set(teamsCache.map(t => t.teamId));
    }

    const prevRoundMatches = allSessionMatches.filter(m => m.roundNo === roundNo - 1);
    if (prevRoundMatches.length === 0) {
        return new Set(teamsCache.map(t => t.teamId));
    }

    const isSemifinalTransition = prevRoundMatches.length === 2
        && prevRoundMatches.every(m => m.matchType === 'KNOCKOUT');

    const eligibleIds = new Set();
    prevRoundMatches.forEach(match => {
        if (match.winnerTeam) {
            eligibleIds.add(match.winnerTeam);
        }
        if (isSemifinalTransition && match.winnerTeam) {
            const loserId = match.winnerTeam === match.teamA ? match.teamB : match.teamA;
            if (loserId) {
                eligibleIds.add(loserId);
            }
        }
    });

    return eligibleIds;
}

function isPowerOfTwo(n) {
    return n >= 2 && (n & (n - 1)) === 0;
}

function nextPowerOfTwo(n) {
    if (n <= 2) return 2;
    if (n <= 4) return 4;
    if (n <= 8) return 8;
    return 16;
}

function renderTeamReadiness() {
    const regCountEl = document.getElementById('registeredTeamsCount');
    const powerBadgeEl = document.getElementById('bracketPowerBadge');
    const fillBtn = document.getElementById('fillPowerOfTwoBtn');
    const fractionEl = document.getElementById('readinessFractionLabel');
    const progressEl = document.getElementById('readinessProgressBar');
    const listEl = document.getElementById('teamReadinessList');

    const totalTeams = teamsCache.length;
    if (regCountEl) {
        regCountEl.textContent = String(totalTeams);
    }

    if (powerBadgeEl) {
        if (totalTeams === 0) {
            powerBadgeEl.textContent = 'Need 2, 4, 8, or 16 teams';
            powerBadgeEl.className = 'mt-1.5 text-[11px] font-semibold text-amber-400';
            fillBtn?.classList.remove('hidden');
        } else if (isPowerOfTwo(totalTeams)) {
            powerBadgeEl.textContent = `Valid ${totalTeams}-Team Bracket`;
            powerBadgeEl.className = 'mt-1.5 text-[11px] font-semibold text-emerald-400';
            fillBtn?.classList.add('hidden');
        } else {
            const target = nextPowerOfTwo(totalTeams);
            const needed = target - totalTeams;
            powerBadgeEl.textContent = `Need +${needed} for ${target}-team bracket`;
            powerBadgeEl.className = 'mt-1.5 text-[11px] font-semibold text-amber-400';
            if (fillBtn) {
                fillBtn.textContent = `+ Auto-Fill ${needed} Team(s) to ${target}`;
                fillBtn.classList.remove('hidden');
            }
        }
    }

    const eligibleSet = computeEligibleTeamIdsForRound(selectedRoundNo || 1);
    const eligibleCount = eligibleSet.size;
    const submittedByTeamId = {};
    currentRoundSubmissions.forEach(sub => {
        submittedByTeamId[sub.teamId] = sub;
    });

    const submittedCount = currentRoundSubmissions.length;
    if (fractionEl) {
        fractionEl.textContent = `${submittedCount} / ${eligibleCount} eligible submitted`;
    }
    if (progressEl) {
        const pct = eligibleCount > 0 ? Math.min(100, Math.round((submittedCount / eligibleCount) * 100)) : 0;
        progressEl.style.width = `${pct}%`;
    }

    if (!listEl) {
        return;
    }

    if (teamsCache.length === 0) {
        listEl.innerHTML = '<div class="text-xs text-slate-500 italic py-3 text-center">No teams registered for this session yet.</div>';
        return;
    }

    listEl.innerHTML = teamsCache.map(team => {
        const sub = submittedByTeamId[team.teamId];
        const isEligible = eligibleSet.has(team.teamId);
        const heroCount = sub?.submissionJson?.heroIds?.length ?? sub?.heroIds?.length ?? 0;

        let badgeHtml;
        if (sub) {
            badgeHtml = `<span class="px-2 py-0.5 rounded-full text-[11px] font-semibold bg-emerald-500/20 text-emerald-300 border border-emerald-500/30">Submitted (${heroCount}H)</span>`;
        } else if (isEligible) {
            badgeHtml = '<span class="px-2 py-0.5 rounded-full text-[11px] font-semibold bg-amber-500/20 text-amber-300 border border-amber-500/30">Pending Draft</span>';
        } else {
            badgeHtml = '<span class="px-2 py-0.5 rounded-full text-[11px] font-medium bg-slate-800 text-slate-500 border border-slate-700">Eliminated</span>';
        }

        const membersText = (team.members && team.members.length > 0) ? team.members.join(', ') : 'No members';

        return `
            <div class="p-2.5 rounded-lg bg-slate-900/80 border border-slate-800 flex items-center justify-between gap-2 text-xs">
                <div class="truncate">
                    <div class="font-semibold text-white truncate">${escapeHtml(team.name)}</div>
                    <div class="text-[11px] text-slate-400 truncate">${escapeHtml(membersText)}</div>
                </div>
                <div class="shrink-0">${badgeHtml}</div>
            </div>
        `;
    }).join('');
}

async function registerQuickTeam() {
    if (!currentSessionId) {
        showError('quickTeamStatus', 'Select or create a session first');
        return;
    }

    const nameInput = document.getElementById('quickTeamName');
    const membersInput = document.getElementById('quickTeamMembers');
    const name = nameInput.value.trim();
    const members = membersInput.value.trim() || 'Player 1, Player 2';

    if (!name) {
        showError('quickTeamStatus', 'Please enter a team name');
        return;
    }

    try {
        const params = new URLSearchParams({
            name,
            members,
            sessionId: currentSessionId
        });
        const response = await fetch(`/api/teams/register?${params.toString()}`, {
            method: 'POST'
        });

        if (response.ok) {
            nameInput.value = '';
            membersInput.value = '';
            showSuccess('quickTeamStatus', `Registered team "${escapeHtml(name)}"`);
            await refreshAllTelemetry();
        } else {
            const err = await response.json().catch(() => null);
            showError('quickTeamStatus', err?.detail || 'Failed to register team');
        }
    } catch (error) {
        showError('quickTeamStatus', 'Error: ' + error.message);
    }
}

async function autoFillBracketToPowerOfTwo() {
    if (!currentSessionId) {
        return;
    }

    const currentCount = teamsCache.length;
    const targetCount = nextPowerOfTwo(currentCount);
    const needed = targetCount - currentCount;
    if (needed <= 0) {
        return;
    }

    const existingNames = new Set(teamsCache.map(t => t.name));
    let created = 0;

    for (let i = 0; i < DEFAULT_FILL_TEAM_NAMES.length && created < needed; i++) {
        const candidateName = DEFAULT_FILL_TEAM_NAMES[i];
        if (existingNames.has(candidateName)) {
            continue;
        }
        const params = new URLSearchParams({
            name: candidateName,
            members: `Hero Captain ${created + 1}, Vanguard ${created + 1}`,
            sessionId: currentSessionId
        });
        const response = await fetch(`/api/teams/register?${params.toString()}`, {
            method: 'POST'
        });
        if (response.ok) {
            existingNames.add(candidateName);
            created++;
        }
    }

    await refreshAllTelemetry();
}

function resolveTeamName(teamId) {
    if (!teamId) {
        return 'TBD';
    }
    return teamsById[teamId]?.name || `Team ${String(teamId).substring(0, 8)}...`;
}

function renderRoundMatchesAndOutcomes() {
    const matchResultsDiv = document.getElementById('matchResults');
    const battleResultsDiv = document.getElementById('battleResults');
    const matchCountLabel = document.getElementById('matchResultsCountLabel');
    const battleCountLabel = document.getElementById('battleResultsCountLabel');
    const summaryBadge = document.getElementById('roundMatchesSummaryBadge');

    if (!matchResultsDiv || !battleResultsDiv) {
        return;
    }

    const roundMatches = allSessionMatches
        .filter(m => m.roundNo === selectedRoundNo)
        .sort((a, b) => (a.bracketPosition ?? 0) - (b.bracketPosition ?? 0));

    const completedMatches = roundMatches.filter(m => m.status === 'COMPLETED');
    const pendingMatches = roundMatches.filter(m => m.status === 'PENDING');

    if (matchCountLabel) matchCountLabel.textContent = String(roundMatches.length);
    if (battleCountLabel) battleCountLabel.textContent = String(completedMatches.length);
    if (summaryBadge) {
        summaryBadge.textContent = `${roundMatches.length} matches (${pendingMatches.length} pending, ${completedMatches.length} completed)`;
    }

    if (roundMatches.length === 0) {
        matchResultsDiv.innerHTML = '<div class="text-xs text-slate-500 italic py-4 text-center">No matches created for this round yet.</div>';
        battleResultsDiv.innerHTML = '<div class="text-xs text-slate-500 italic py-4 text-center">Run battles after matchmaking to see winners.</div>';
        return;
    }

    matchResultsDiv.innerHTML = roundMatches.map(match => {
        const teamAName = escapeHtml(resolveTeamName(match.teamA));
        const teamBName = escapeHtml(resolveTeamName(match.teamB));
        const typeLabel = match.matchType === 'FINAL'
            ? '<span class="px-2 py-0.5 rounded bg-yellow-500/20 text-yellow-300 border border-yellow-500/40 text-[10px] font-bold">FINAL</span>'
            : match.matchType === 'THIRD_PLACE'
                ? '<span class="px-2 py-0.5 rounded bg-amber-600/20 text-amber-300 border border-amber-500/40 text-[10px] font-bold">3RD PLACE</span>'
                : `<span class="px-2 py-0.5 rounded bg-purple-500/20 text-purple-300 border border-purple-500/30 text-[10px] font-mono">#${(match.bracketPosition ?? 0) + 1}</span>`;

        const statusBadge = match.status === 'COMPLETED'
            ? '<span class="px-2 py-0.5 rounded bg-emerald-500/20 text-emerald-300 text-[10px] font-bold">COMPLETED</span>'
            : match.status === 'CANCELLED'
                ? '<span class="px-2 py-0.5 rounded bg-slate-700 text-slate-400 text-[10px] font-bold">CANCELLED</span>'
                : '<span class="px-2 py-0.5 rounded bg-amber-500/20 text-amber-300 text-[10px] font-bold">PENDING</span>';

        const runSingleBtn = match.status === 'PENDING'
            ? `<button type="button" onclick="runSingleMatch('${match.matchId}')" class="px-2.5 py-1 rounded bg-red-600/80 hover:bg-red-500 text-white text-[11px] font-semibold transition-colors">Run Match</button>`
            : '';

        return `
            <div class="p-3 bg-slate-950/70 rounded-xl border border-slate-800 space-y-2 text-xs">
                <div class="flex items-center justify-between gap-2">
                    <div class="flex items-center gap-1.5">
                        ${typeLabel}
                        <span class="font-mono text-[11px] text-slate-400">ID: ${match.matchId.substring(0, 8)}...</span>
                    </div>
                    ${statusBadge}
                </div>
                <div class="font-semibold text-white flex items-center justify-between gap-2">
                    <span class="truncate">${teamAName}</span>
                    <span class="text-[10px] text-slate-500 font-bold uppercase px-1">vs</span>
                    <span class="truncate text-right">${teamBName}</span>
                </div>
                <div class="flex items-center justify-end gap-2 pt-1 border-t border-slate-900">
                    ${runSingleBtn}
                    <a href="battle.html?matchId=${match.matchId}" target="_blank"
                        class="px-2.5 py-1 rounded bg-blue-600/20 hover:bg-blue-600/30 text-blue-300 border border-blue-500/30 text-[11px] font-semibold transition-colors">
                        Open Battle Arena
                    </a>
                </div>
            </div>
        `;
    }).join('');

    if (completedMatches.length === 0) {
        battleResultsDiv.innerHTML = '<div class="text-xs text-slate-500 italic py-4 text-center">Pending simulation. Click "Run All Battles" to execute.</div>';
        return;
    }

    battleResultsDiv.innerHTML = completedMatches.map(match => {
        const winnerName = match.winnerTeam ? escapeHtml(resolveTeamName(match.winnerTeam)) : 'DRAW';
        const turns = match.resultJson?.durationSeconds ?? 0;
        const outcomeTag = turns === 0 && match.winnerTeam ? 'Walkover' : `${turns} turns`;

        return `
            <div class="p-3 bg-slate-950/70 rounded-xl border border-emerald-500/30 space-y-1.5 text-xs">
                <div class="flex items-center justify-between">
                    <span class="font-mono text-[11px] text-slate-400">Match: ${match.matchId.substring(0, 8)}...</span>
                    <span class="text-[11px] font-mono text-slate-400">${outcomeTag}</span>
                </div>
                <div class="flex items-center justify-between gap-2">
                    <div class="flex items-center gap-1.5 text-amber-300 font-bold truncate">
                        <i data-lucide="trophy" class="w-4 h-4 text-amber-400 shrink-0"></i>
                        <span class="truncate">Winner: ${winnerName}</span>
                    </div>
                    <a href="battle.html?matchId=${match.matchId}" target="_blank"
                        class="text-[11px] text-blue-300 hover:text-blue-200 underline shrink-0">
                        Watch Replay
                    </a>
                </div>
            </div>
        `;
    }).join('');
}

function updatePipelineTelemetry() {
    const bannerEl = document.getElementById('nextActionBanner');
    const stepSession = document.getElementById('pipelineStepSession');
    const stepTeams = document.getElementById('pipelineStepTeams');
    const stepRound = document.getElementById('pipelineStepRound');
    const stepSquads = document.getElementById('pipelineStepSquads');
    const stepBattles = document.getElementById('pipelineStepBattles');

    if (!bannerEl) {
        return;
    }

    document.getElementById('pipelineSessionText').textContent = currentSessionId
        ? `${currentSessionId.substring(0, 8)}...`
        : 'No Session';
    document.getElementById('pipelineSessionSub').textContent = currentSessionId ? 'Active Session' : 'Create session';

    const totalTeams = teamsCache.length;
    const validBracket = isPowerOfTwo(totalTeams);
    document.getElementById('pipelineTeamsText').textContent = `${totalTeams} Registered`;
    document.getElementById('pipelineTeamsSub').textContent = validBracket
        ? 'Valid power-of-2'
        : 'Needs power of 2 (2,4,8,16)';

    const currentRound = roundsCache.find(r => r.roundNo === selectedRoundNo);
    document.getElementById('pipelineRoundText').textContent = currentRound
        ? `Round ${currentRound.roundNo} (${currentRound.status})`
        : 'No Round';
    document.getElementById('pipelineRoundSub').textContent = currentRound
        ? (currentRound.spec?.description || currentRound.specJson?.description || 'Configured')
        : 'Create Round 1';

    const eligibleCount = computeEligibleTeamIdsForRound(selectedRoundNo || 1).size;
    const subCount = currentRoundSubmissions.length;
    document.getElementById('pipelineSquadsText').textContent = `${subCount} / ${eligibleCount} Submitted`;
    document.getElementById('pipelineSquadsSub').textContent = subCount >= eligibleCount && eligibleCount > 0
        ? 'All eligible ready'
        : 'Waiting for squads';

    const roundMatches = allSessionMatches.filter(m => m.roundNo === selectedRoundNo);
    const pendingMatches = roundMatches.filter(m => m.status === 'PENDING');
    const completedMatches = roundMatches.filter(m => m.status === 'COMPLETED');
    document.getElementById('pipelineBattlesText').textContent = `${completedMatches.length} / ${roundMatches.length} Done`;
    document.getElementById('pipelineBattlesSub').textContent = pendingMatches.length > 0
        ? `${pendingMatches.length} pending simulation`
        : (roundMatches.length > 0 ? 'Stage complete' : 'Not matched yet');

    setPipelineCardState(stepSession, Boolean(currentSessionId), !currentSessionId);
    setPipelineCardState(stepTeams, validBracket, Boolean(currentSessionId) && !validBracket);
    setPipelineCardState(stepRound, Boolean(currentRound), validBracket && !currentRound);
    setPipelineCardState(stepSquads, subCount > 0 && subCount >= eligibleCount, Boolean(currentRound) && roundMatches.length === 0 && subCount < eligibleCount);
    setPipelineCardState(stepBattles, roundMatches.length > 0 && pendingMatches.length === 0, roundMatches.length > 0 && pendingMatches.length > 0);

    const hasCompletedFinal = allSessionMatches.some(m => m.matchType === 'FINAL' && m.status === 'COMPLETED');

    if (!currentSessionId) {
        bannerEl.textContent = 'Next Action: Start or select an active tournament session.';
    } else if (roundsCache.length === 0) {
        bannerEl.textContent = 'Next Action: Choose a Stage Preset and click "Create Round" to open Round 1.';
    } else if (!validBracket && selectedRoundNo === 1 && roundMatches.length === 0) {
        bannerEl.textContent = `Next Action: Register teams to reach a power of 2 (currently ${totalTeams} teams).`;
    } else if (roundMatches.length === 0) {
        bannerEl.textContent = subCount < eligibleCount
            ? `Waiting for team submissions (${subCount}/${eligibleCount} submitted). Click "Auto-Match Teams" when ready to lock pairings.`
            : `All ${subCount} eligible teams submitted! Click "Auto-Match Teams" to generate bracket pairings.`;
    } else if (pendingMatches.length > 0) {
        bannerEl.textContent = `Next Action: ${pendingMatches.length} match(es) ready — click "Run All Battles" to simulate.`;
    } else if (hasCompletedFinal) {
        bannerEl.textContent = 'Tournament Complete! Champion and podium standings are finalized on the Live Bracket.';
    } else {
        bannerEl.textContent = `Round ${selectedRoundNo} complete! Select the next Stage Preset and click "Create Round" to advance the bracket.`;
    }
}

function setPipelineCardState(el, isDone, isActive) {
    if (!el) {
        return;
    }
    if (isActive) {
        el.className = 'p-3 rounded-xl border border-amber-500/60 bg-amber-950/20 space-y-1 transition-all shadow';
    } else if (isDone) {
        el.className = 'p-3 rounded-xl border border-emerald-500/40 bg-emerald-950/15 space-y-1 transition-all';
    } else {
        el.className = 'p-3 rounded-xl border border-slate-700 bg-slate-900/60 space-y-1 transition-all';
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
            resultsDiv.innerHTML = matchIds.map((id, index) =>
                `<div class="p-3 bg-slate-950/70 rounded-xl border border-slate-800 space-y-2 text-xs">
                    <div class="flex items-center justify-between gap-2">
                        <span class="font-mono text-[11px] text-slate-300">
                            <i data-lucide="check-circle" class="w-3.5 h-3.5 inline text-emerald-400 mr-1"></i>
                            Match #${index + 1}: ${id.substring(0, 8)}...
                        </span>
                        <span class="px-2 py-0.5 rounded bg-amber-500/20 text-amber-300 text-[10px] font-bold">PENDING</span>
                    </div>
                    <div class="flex items-center justify-end gap-2 pt-1 border-t border-slate-900">
                        <button type="button" onclick="runSingleMatch('${id}')" class="px-2.5 py-1 rounded bg-red-600/80 hover:bg-red-500 text-white text-[11px] font-semibold transition-colors">Run Match</button>
                        <a href="battle.html?matchId=${id}" target="_blank" class="px-2.5 py-1 rounded bg-blue-600/20 hover:bg-blue-600/30 text-blue-300 border border-blue-500/30 text-[11px] font-semibold transition-colors">Open Battle Arena</a>
                    </div>
                </div>`
            ).join('');

            const matchCountLabel = document.getElementById('matchResultsCountLabel');
            const summaryBadge = document.getElementById('roundMatchesSummaryBadge');
            if (matchCountLabel) matchCountLabel.textContent = String(matchIds.length);
            if (summaryBadge) summaryBadge.textContent = `${matchIds.length} matches (${matchIds.length} pending)`;
            lucide.createIcons();
        } else {
            const err = await response.json().catch(() => null);
            const msg = err?.detail || err?.title || 'Failed to create matches';
            showError('matchStatus', msg);
        }
    } catch (error) {
        showError('matchStatus', 'Error: ' + error.message);
    }
}

async function createManualMatch() {
    if (!currentSessionId || !selectedRoundNo) {
        showError('matchStatus', 'Please select a session and round first');
        return;
    }

    const teamA = document.getElementById('manualTeamASelect').value;
    const teamB = document.getElementById('manualTeamBSelect').value;

    if (!teamA || !teamB || teamA === teamB) {
        showError('matchStatus', 'Please select two distinct teams for a custom match');
        return;
    }

    try {
        const params = new URLSearchParams({
            teamA,
            teamB,
            roundNo: String(selectedRoundNo),
            sessionId: currentSessionId
        });
        const response = await fetch(`/api/admin/matches/create?${params.toString()}`, {
            method: 'POST',
            headers: { 'Authorization': authHeader }
        });

        if (response.ok) {
            const matchId = await response.json();
            showSuccess('matchStatus', `Created custom match ${String(matchId).substring(0, 8)}...`);
            await refreshAllTelemetry();
        } else {
            const err = await response.json().catch(() => null);
            showError('matchStatus', err?.detail || 'Failed to create custom match');
        }
    } catch (error) {
        showError('matchStatus', 'Error: ' + error.message);
    }
}

async function runSingleMatch(matchId) {
    try {
        showInfo('battleStatus', `Simulating match ${matchId.substring(0, 8)}...`);
        const response = await fetch(`/api/admin/matches/${matchId}/run`, {
            method: 'POST',
            headers: { 'Authorization': authHeader }
        });

        if (response.ok) {
            const msg = await response.text();
            showSuccess('battleStatus', msg);
            await loadRounds();
        } else {
            const err = await response.json().catch(() => null);
            showError('battleStatus', err?.detail || err?.title || 'Failed to run match');
        }
    } catch (error) {
        showError('battleStatus', 'Error: ' + error.message);
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
                const winnerId = result.winners[matchId];
                const winnerLabel = winnerId ? resolveTeamName(winnerId) : 'DRAW';
                return `<div class="p-3 bg-slate-950/70 rounded-xl border border-emerald-500/30 space-y-1.5 text-xs">
                    <div class="flex items-center justify-between">
                        <span class="font-mono text-[11px] text-slate-400">Match: ${matchId.substring(0, 8)}...</span>
                        <span class="px-2 py-0.5 rounded bg-emerald-500/20 text-emerald-300 text-[10px] font-bold">COMPLETED</span>
                    </div>
                    <div class="flex items-center justify-between gap-2">
                        <div class="flex items-center gap-1.5 text-amber-300 font-bold truncate">
                            <i data-lucide="trophy" class="w-4 h-4 text-amber-400 shrink-0"></i>
                            <span class="truncate">Winner: ${escapeHtml(winnerLabel)}</span>
                        </div>
                        <a href="battle.html?matchId=${matchId}" target="_blank" class="text-[11px] text-blue-300 hover:text-blue-200 underline shrink-0">Watch Replay</a>
                    </div>
                </div>`;
            }).join('');

            const battleCountLabel = document.getElementById('battleResultsCountLabel');
            if (battleCountLabel) battleCountLabel.textContent = String(result.successfulSimulations);

            const roundObj = roundsCache.find(r => r.roundNo === selectedRoundNo);
            if (roundObj && result.successfulSimulations === result.totalMatches && result.totalMatches > 0) {
                roundObj.status = 'CLOSED';
                updateRoundInfoBanner();
                suggestNextStagePreset();
            }
            lucide.createIcons();
        } else {
            const err = await response.json().catch(() => null);
            const msg = err?.detail || err?.title || 'Failed to run battles';
            showError('battleStatus', msg);
        }
    } catch (error) {
        showError('battleStatus', 'Error: ' + error.message);
    }
}

function showSuccess(elementId, message) {
    const el = document.getElementById(elementId);
    if (!el) return;
    el.innerHTML =
        `<div class="p-3 bg-green-500/20 border border-green-500 rounded-lg text-green-300 text-sm flex items-center gap-2">
            <i data-lucide="check-circle" class="w-4 h-4 shrink-0"></i><span>${message}</span>
        </div>`;
    lucide.createIcons();
}

function showError(elementId, message) {
    const el = document.getElementById(elementId);
    if (!el) return;
    el.innerHTML =
        `<div class="p-3 bg-red-500/20 border border-red-500 rounded-lg text-red-300 text-sm flex items-center gap-2">
            <i data-lucide="alert-circle" class="w-4 h-4 shrink-0"></i><span>${message}</span>
        </div>`;
    lucide.createIcons();
}

function showInfo(elementId, message) {
    const el = document.getElementById(elementId);
    if (!el) return;
    el.innerHTML =
        `<div class="p-3 bg-blue-500/20 border border-blue-500 rounded-lg text-blue-300 text-sm flex items-center gap-2">
            <i data-lucide="info" class="w-4 h-4 shrink-0"></i><span>${message}</span>
        </div>`;
    lucide.createIcons();
}

function escapeHtml(value) {
    return String(value ?? '')
        .replaceAll('&', '&amp;')
        .replaceAll('<', '&lt;')
        .replaceAll('>', '&gt;')
        .replaceAll('"', '&quot;')
        .replaceAll("'", '&#39;');
}

function logout() {
    authHeader = '';
    localStorage.removeItem('adminAuthHeader');
    currentSessionId = null;
    selectedRoundNo = null;
    sessions = [];
    if (autoRefreshIntervalId) {
        clearInterval(autoRefreshIntervalId);
        autoRefreshIntervalId = null;
    }
    document.getElementById('loginPanel').classList.remove('hidden');
    document.getElementById('adminPanel').classList.add('hidden');
    document.getElementById('loginForm').reset();
    document.getElementById('loginError').classList.add('hidden');
}

async function resetDatabase() {
    await executeDatabaseReset();
}

async function executeDatabaseReset() {
    try {
        const response = await fetch('/api/admin/reset', {
            method: 'POST',
            headers: { 'Authorization': authHeader }
        });

        if (response.ok) {
            window.location.reload();
        } else {
            const errorText = await response.text();
            showError('sessionStatus', 'Reset failed: ' + (errorText || 'Unknown error'));
        }
    } catch (error) {
        showError('sessionStatus', 'Error connecting to server: ' + error.message);
    }
}
