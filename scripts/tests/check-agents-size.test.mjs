import assert from 'node:assert/strict';
import { spawnSync } from 'node:child_process';
import { mkdtempSync, rmSync, writeFileSync } from 'node:fs';
import { tmpdir } from 'node:os';
import { join, resolve } from 'node:path';
import test from 'node:test';

const repository = resolve(import.meta.dirname, '../..');
const scriptPath = join(repository, 'scripts/check-agents-size.sh');

test('passes when AGENTS.md is within 500 lines', () => {
  const result = spawnSync('sh', [scriptPath], { encoding: 'utf8' });
  assert.equal(result.status, 0);
  assert.match(result.stdout, /AGENTS\.md line count check passed/);
});

test('fails with instruction when target file exceeds 500 lines', t => {
  const directory = mkdtempSync(join(tmpdir(), 'arena-agents-check-'));
  t.after(() => rmSync(directory, { recursive: true, force: true }));

  const oversizedPath = join(directory, 'AGENTS.md');
  writeFileSync(oversizedPath, 'line\n'.repeat(501));

  const result = spawnSync('sh', [scriptPath, oversizedPath], { encoding: 'utf8' });

  assert.equal(result.status, 1);
  assert.match(result.stderr, /exceeds 500 lines \(current: 501\)/);
  assert.match(result.stderr, /Por favor, separa el fichero en varios más pequeños enlazados por categoría/);
});

test('fails if target file does not exist', () => {
  const result = spawnSync('sh', [scriptPath, '/non/existent/AGENTS.md'], { encoding: 'utf8' });
  assert.equal(result.status, 1);
  assert.match(result.stderr, /AGENTS\.md not found/);
});
