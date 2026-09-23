import assert from 'node:assert/strict';
import { mkdtemp, readFile, rm, writeFile } from 'node:fs/promises';
import path from 'node:path';
import test from 'node:test';
import { tmpdir } from 'node:os';

import { formatReport, parseReport, run } from '../check-crap.mjs';

const report = `<?xml version="1.0"?><report><package name="example"><class name="example/Example" sourcefilename="Example.java"><method name="covered" line="10"><counter type="INSTRUCTION" missed="0" covered="10"/><counter type="COMPLEXITY" missed="0" covered="1"/></method><method name="risky" line="20"><counter type="INSTRUCTION" missed="5" covered="5"/><counter type="COMPLEXITY" missed="3" covered="1"/></method><method name="&lt;init&gt;" line="2"><counter type="INSTRUCTION" missed="5" covered="0"/><counter type="COMPLEXITY" missed="1" covered="0"/></method><method name="lambda$covered$0" line="12"><counter type="INSTRUCTION" missed="5" covered="0"/><counter type="COMPLEXITY" missed="1" covered="0"/></method></class></package></report>`;

test('computes CRAP from JaCoCo complexity and instruction coverage', () => {
  const metrics = parseReport(report);

  assert.deepEqual(metrics.map(({ methodName }) => methodName), ['risky', 'covered']);
  assert.equal(metrics[0].complexity, 4);
  assert.equal(metrics[0].coverage, 0.5);
  assert.equal(metrics[0].crap, 6);
});

test('formats the maximum score and stable method details', () => {
  const { text, maximum } = formatReport(parseReport(report));

  assert.match(text, /CRAP report \(threshold 8\.0\)/);
  assert.match(text, /example\.Example#risky/);
  assert.equal(maximum, 6);
});

test('returns the threshold failure exit code', async () => {
  const directory = await mkdtemp(path.join(tmpdir(), 'crap-check-'));
  const reportPath = path.join(directory, 'jacoco.xml');
  await writeFile(reportPath, report.replace('missed="3" covered="1"', 'missed="5" covered="1"'));
  let output = '';
  let errors = '';

  const exitCode = await run(
    ['--report', reportPath],
    { write: (value) => { output += value; } },
    { write: (value) => { errors += value; } },
  );

  await rm(directory, { recursive: true, force: true });
  assert.equal(exitCode, 2);
  assert.match(output, /Maximum CRAP: 10\.50/);
  assert.match(errors, /CRAP threshold exceeded/);
});

test('pre-push runs CRAP after Maven verification', async () => {
  const hook = await readFile(new URL('../../.githooks/pre-push', import.meta.url), 'utf8');

  assert.ok(hook.indexOf('./mvnw clean verify -Ppostgres-tests') < hook.indexOf('npm run lint:crap'));
});
