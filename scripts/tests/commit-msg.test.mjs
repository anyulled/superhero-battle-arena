import assert from 'node:assert/strict';
import { spawnSync } from 'node:child_process';
import { copyFileSync, mkdirSync, mkdtempSync, rmSync, symlinkSync, writeFileSync } from 'node:fs';
import { tmpdir } from 'node:os';
import { join, resolve } from 'node:path';
import test from 'node:test';

const repository = resolve(import.meta.dirname, '../..');

function fixture(t, dependencies = true) {
  const directory = mkdtempSync(join(tmpdir(), 'arena-commit-message-'));
  t.after(() => rmSync(directory, { recursive: true, force: true }));
  mkdirSync(join(directory, '.git'));
  writeFileSync(join(directory, '.git/COMMIT_EDITMSG'), 'invalid default message\n');
  copyFileSync(join(repository, 'commitlint.config.mjs'), join(directory, 'commitlint.config.mjs'));
  if (dependencies) symlinkSync(join(repository, 'node_modules'), join(directory, 'node_modules'));
  return directory;
}

function validate(directory, message) {
  const messagePath = join(directory, 'message with spaces.txt');
  writeFileSync(messagePath, message);
  return spawnSync('sh', [join(repository, '.githooks/commit-msg'), messagePath], {
    cwd: directory,
    encoding: 'utf8',
  });
}

test('validates the explicitly supplied message path', t => {
  const directory = fixture(t);

  const result = validate(directory, 'fix: preserve explicit message paths\n');

  assert.equal(result.status, 0, result.stdout + result.stderr);
});

for (const message of ['fix: Uppercase subject\n', 'revert: unsupported type\n', 'invalid message\n']) {
  test(`rejects ${message.trim()}`, t => {
    const directory = fixture(t);

    const result = validate(directory, message);

    assert.notEqual(result.status, 0);
  });
}

test('preserves the existing long-body policy', t => {
  const directory = fixture(t);

  const result = validate(directory, `docs: explain validation\n\n${'detail '.repeat(40)}\n`);

  assert.equal(result.status, 0, result.stdout + result.stderr);
});

test('fails with installation instructions when locked dependencies are absent', t => {
  const directory = fixture(t, false);

  const result = validate(directory, 'fix: require installed dependencies\n');

  assert.notEqual(result.status, 0);
  assert.match(result.stderr, /npm ci/);
});
