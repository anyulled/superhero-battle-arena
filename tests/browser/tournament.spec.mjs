import { test, expect } from '@playwright/test';

const adminHeaders = {
  Authorization: `Basic ${Buffer.from('admin:browser-test-password').toString('base64')}`,
};

async function post(request, path, options = {}) {
  const response = await request.post(path, options);
  expect(response.ok(), `${path}: ${response.status()} ${await response.text()}`).toBeTruthy();
  return response;
}

async function createTournament(request) {
  const session = await post(request, '/api/admin/sessions/start', { headers: adminHeaders });
  const sessionId = await session.json();
  const round = await post(request, '/api/admin/rounds/create', {
    headers: adminHeaders,
    data: {
      sessionId,
      spec: {
        description: 'Browser acceptance round', teamSize: 1, budgetCap: 10000,
        requiredRoles: {}, maxSameRole: {}, bannedTags: [], tagModifiers: {},
        mapType: 'ARENA', allowedRoles: [], allowedGenders: [], allowedRaces: [],
        allowedPublishers: [], allowedAlignments: [],
      },
    },
  });
  const roundNo = await round.json();
  const roster = await request.get('/api/teams/heroes');
  expect(roster.ok()).toBeTruthy();
  const heroes = await roster.json();
  expect(heroes.length).toBeGreaterThanOrEqual(2);
  for (const [index, name] of ['Browser Avengers', 'Browser Defenders'].entries()) {
    const team = await post(request, '/api/teams/register', {
      params: { sessionId, name, members: `Participant ${index + 1}A,Participant ${index + 1}B` },
    });
    const teamId = await team.json();
    await post(request, `/api/rounds/${roundNo}/submit`, {
      params: { teamId }, data: { heroIds: [heroes[index].id], strategy: 'balanced' },
    });
  }
  return { sessionId, roundNo };
}

test('registered teams progress through admin battle execution to a public replay', async ({ page, request }) => {
  const { sessionId, roundNo } = await createTournament(request);
  const browserErrors = [];
  page.on('pageerror', error => browserErrors.push(error.message));

  await test.step('show registered teams in the lobby', async () => {
    await page.goto('/lobby.html');
    await expect(page.locator('#teamsBody')).toContainText('Browser Avengers');
    await expect(page.locator('#teamsBody')).toContainText('Browser Defenders');
    await expect(page.locator('#sessionStatus')).toContainText(sessionId);
  });
  await test.step('run submitted teams through the admin controls', async () => {
    await page.goto('/admin.html');
    await page.getByLabel('Username').fill('admin');
    await page.getByLabel('Password').fill('browser-test-password');
    await page.locator('#loginForm button[type=submit]').click();
    await expect(page.locator('#adminPanel')).toBeVisible();
    await expect(page.locator('#sessionSelector')).toHaveValue(sessionId);
    await expect(page.locator('#roundSelector')).toHaveValue(String(roundNo));
    await expect(page.locator('#teamEntriesCount')).toHaveText('2');
    await page.getByRole('button', { name: 'Auto-Match Teams', exact: true }).click();
    await expect(page.locator('#matchStatus')).toContainText('Created 1 matches');
    await page.getByRole('button', { name: 'Run All Battles', exact: true }).click();
    await expect(page.locator('#battleStatus')).toContainText('Completed 1/1 battles');
  });
  await test.step('view the completed match and its streamed replay', async () => {
    await page.goto('/bracket.html');
    await expect(page.locator('#matchesContainer')).toContainText('Browser Avengers');
    await expect(page.locator('#matchesContainer')).toContainText('Browser Defenders');
    await page.getByRole('link', { name: 'Watch Replay', exact: true }).click();
    await expect(page.locator('#teamAName')).toHaveText(/Browser (Avengers|Defenders)/);
    await expect(page.locator('#teamBName')).toHaveText(/Browser (Avengers|Defenders)/);
    await expect(page.locator('#battleLog')).toContainText('MATCH START');
    await expect(page.locator('#winnerModal')).toBeVisible({ timeout: 60000 });
    await expect(page.locator('#winnerName')).toHaveText(/Browser (Avengers|Defenders)|Draw!/);
    await page.screenshot({ path: 'test-results/browser/completed-replay.png', fullPage: true });
  });

  expect(browserErrors).toEqual([]);
});
