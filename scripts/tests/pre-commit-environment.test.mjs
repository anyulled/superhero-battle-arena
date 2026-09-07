import assert from 'node:assert/strict';
import { execFileSync, spawnSync } from 'node:child_process';
import { mkdtempSync, readFileSync, rmSync, writeFileSync } from 'node:fs';
import { tmpdir } from 'node:os';
import { join, resolve } from 'node:path';
import test from 'node:test';

test('hook regression fixtures cannot change the invoking worktree or shared Git configuration', context => {
  const directory = mkdtempSync(join(tmpdir(), 'arena-hook-environment-'));
  context.after(() => rmSync(directory, { recursive: true, force: true }));
  const repository = join(directory, 'repository');
  const worktree = join(directory, 'worktree');
  const environment = { ...process.env, GIT_CONFIG_NOSYSTEM: '1', GIT_CONFIG_GLOBAL: '/dev/null' };
  const localVariables = execFileSync('git', ['rev-parse', '--local-env-vars'], { encoding: 'utf8' }).trim().split('\n');
  for (const variable of localVariables) delete environment[variable];
  const git = (arguments_, cwd = directory) => execFileSync('git', arguments_, { cwd, env: environment, encoding: 'utf8', stdio: ['ignore', 'pipe', 'pipe'] }).trim();
  git(['init', '--quiet', repository]);
  git(['config', 'user.name', 'Environment Test'], repository);
  git(['config', 'user.email', 'environment@example.invalid'], repository);
  writeFileSync(join(repository, 'tracked.txt'), 'preserved\n');
  git(['add', 'tracked.txt'], repository);
  git(['commit', '--quiet', '-m', 'baseline'], repository);
  git(['worktree', 'add', '--detach', worktree], repository);
  const gitDirectory = git(['rev-parse', '--absolute-git-dir'], worktree);
  const commonDirectory = join(repository, '.git');
  const before = {
    config: readFileSync(join(commonDirectory, 'config'), 'utf8'),
    head: git(['rev-parse', 'HEAD'], worktree),
    index: git(['write-tree'], worktree),
  };

  const result = spawnSync('sh', [resolve(import.meta.dirname, 'pre-commit-test.sh')], {
    cwd: worktree, encoding: 'utf8',
    env: { ...environment, GIT_DIR: gitDirectory, GIT_COMMON_DIR: commonDirectory, GIT_WORK_TREE: worktree, GIT_INDEX_FILE: join(gitDirectory, 'index') },
  });

  assert.equal(result.status, 0, result.stdout + result.stderr);
  assert.equal(readFileSync(join(commonDirectory, 'config'), 'utf8'), before.config);
  assert.equal(git(['rev-parse', 'HEAD'], worktree), before.head);
  assert.equal(git(['write-tree'], worktree), before.index);
  assert.equal(git(['status', '--porcelain'], worktree), '');
});
