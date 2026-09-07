import assert from 'node:assert/strict';
import { spawnSync } from 'node:child_process';
import { chmod, copyFile, mkdir, mkdtemp, readFile, rm } from 'node:fs/promises';
import { existsSync, writeFileSync } from 'node:fs';
import { tmpdir } from 'node:os';
import { join } from 'node:path';
import test from 'node:test';
import { parse } from 'yaml';

async function fixture(t) {
  const directory = await mkdtemp(join(tmpdir(), 'arena-security-audit-'));
  await mkdir(join(directory, 'scripts'));
  await mkdir(join(directory, '.mvn'));
  await copyFile('scripts/security-audit.sh', join(directory, 'scripts/security-audit.sh'));
  await copyFile('.mvn/security-settings.xml', join(directory, '.mvn/security-settings.xml'));
  const wrapper = join(directory, 'mvnw');
  writeFileSync(wrapper, '#!/bin/sh\nprintf "%s\\n" "$@" > "$AUDIT_ARGUMENTS"\nexit "${MOCK_AUDIT_STATUS:-0}"\n');
  await chmod(wrapper, 0o755);
  t.after(() => rm(directory, { recursive: true, force: true }));
  const argumentsPath = join(directory, 'arguments.txt');
  return {
    argumentsPath,
    run(overrides = {}) {
      return spawnSync('sh', [join(directory, 'scripts/security-audit.sh')], {
        encoding: 'utf8',
        env: {
          PATH: process.env.PATH,
          GUIDE_API_TOKEN: 'test-guide-token',
          NVD_API_KEY: 'test-nvd-key',
          AUDIT_ARGUMENTS: argumentsPath,
          ...overrides,
        },
      });
    },
  };
}

for (const missingSecret of ['GUIDE_API_TOKEN', 'NVD_API_KEY']) {
  test(`rejects missing ${missingSecret} before launching Maven`, async t => {
    const audit = await fixture(t);

    const result = audit.run({ [missingSecret]: '' });

    assert.notEqual(result.status, 0);
    assert.match(result.stderr, new RegExp(missingSecret));
    assert.equal(existsSync(audit.argumentsPath), false);
  });
}

test('runs the pinned scanner with strict remote failure handling and indirect credentials', async t => {
  const audit = await fixture(t);

  const result = audit.run();
  const arguments_ = (await readFile(audit.argumentsPath, 'utf8')).trim().split('\n');

  assert.equal(result.status, 0);
  for (const required of [
    'org.owasp:dependency-check-maven:13.0.0:check',
    '-DfailOnError=true',
    '-DfailBuildOnCVSS=7',
    '-DossIndexAnalyzerEnabled=true',
    '-DossIndexWarnOnlyOnRemoteErrors=false',
    '-DossIndexAnalyzerUrl=https://api.guide.sonatype.com',
    '-DossIndexAnalyzerUseCache=false',
    '-DossIndexServerId=sonatype-guide',
    '-DnvdApiKeyEnvironmentVariable=NVD_API_KEY',
  ]) assert.ok(arguments_.includes(required), `Missing scanner configuration: ${required}`);
  assert.ok(arguments_.includes('.mvn/security-settings.xml'));
  assert.ok(arguments_.every(argument => !argument.includes('test-guide-token') && !argument.includes('test-nvd-key')));
});

test('propagates scanner failure instead of reporting a successful audit', async t => {
  const audit = await fixture(t);

  const result = audit.run({ MOCK_AUDIT_STATUS: '7' });

  assert.equal(result.status, 7);
});

test('Maven settings resolve the Guide token from the environment', async () => {
  const settings = await readFile('.mvn/security-settings.xml', 'utf8');

  const password = settings.match(/<password>(.*?)<\/password>/)?.[1];

  assert.equal(password, '${env.GUIDE_API_TOKEN}');
  assert.match(settings, /<id>sonatype-guide<\/id>/);
  assert.match(settings, /<username>guide<\/username>/);
});

test('only trusted security scans receive Guide credentials', async () => {
  const workflow = parse(await readFile('.github/workflows/security-audit.yml', 'utf8'));
  const caller = parse(await readFile('.github/workflows/ci.yml', 'utf8'));

  const trusted = workflow.jobs['security-audit'];
  const scan = trusted.steps.find(step => step.run === 'sh scripts/security-audit.sh');

  assert.equal(scan.env.GUIDE_API_TOKEN, '${{ secrets.GUIDE_API_TOKEN }}');
  assert.equal(scan.env.NVD_API_KEY, '${{ secrets.NVD_API_KEY }}');
  assert.equal(workflow.jobs['dependency-review'].if, "github.event_name == 'pull_request'");
  assert.ok(!JSON.stringify(workflow.jobs['dependency-review']).includes('secrets.'));
  assert.equal(caller.jobs['security-audit'].secrets.GUIDE_API_TOKEN, '${{ secrets.GUIDE_API_TOKEN }}');
  assert.equal(caller.jobs['security-audit'].secrets.NVD_API_KEY, '${{ secrets.NVD_API_KEY }}');
  assert.equal(caller.jobs['dependency-review'].secrets, undefined);
  assert.ok(!JSON.stringify(workflow).includes('ossindex-maven-plugin'));
});

test('caches Dependency-Check data independently from Maven dependencies', async () => {
  const workflow = parse(await readFile('.github/workflows/security-audit.yml', 'utf8'));
  const steps = workflow.jobs['security-audit'].steps;
  const restore = steps.find(step => step.id === 'dependency-check-cache-restore');
  const save = steps.find(step => step.name === 'Save Dependency-Check data');
  const expectedPath = '~/.m2/repository/org/owasp/dependency-check-data';
  const expectedKey = 'dependency-check-${{ runner.os }}-v13-${{ github.run_id }}';

  assert.equal(restore.uses, 'actions/cache/restore@0057852bfaa89a56745cba8c7296529d2fc39830');
  assert.equal(restore.with.path, expectedPath);
  assert.equal(restore.with.key, expectedKey);
  assert.equal(restore.with['restore-keys'], 'dependency-check-${{ runner.os }}-v13-\n');
  assert.equal(save.uses, 'actions/cache/save@0057852bfaa89a56745cba8c7296529d2fc39830');
  assert.equal(save.if, 'success()');
  assert.equal(save.with.path, expectedPath);
  assert.equal(save.with.key, expectedKey);
});
