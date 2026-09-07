import { spawnSync } from 'node:child_process';
import { readFileSync } from 'node:fs';

const manifest = JSON.parse(readFileSync(new URL('../package.json', import.meta.url)));
let failures = 0;

function requireCheck(label, passed, instruction) {
  console.log(`${passed ? 'PASS' : 'FAIL'} ${label}${passed ? '' : `: ${instruction}`}`);
  if (!passed) failures += 1;
}

function command(name, args) {
  const result = spawnSync(name, args, { encoding: 'utf8' });
  return { success: result.status === 0, output: `${result.stdout || ''}${result.stderr || ''}`.trim() };
}

requireCheck('Node', process.versions.node === manifest.engines.node, `Use Node ${manifest.engines.node} from .nvmrc.`);
const npm = command('npm', ['--version']);
requireCheck('npm', npm.success && npm.output === manifest.engines.npm, `Install npm@${manifest.engines.npm}.`);
const java = command('java', ['-version']);
requireCheck('Java', java.success && /version "25(?:\.|\")/.test(java.output), 'Set JAVA_HOME and PATH to Java 25.');
const docker = command('docker', ['version', '--format', '{{.Server.Version}}']);
requireCheck('Docker', docker.success && docker.output.length > 0, 'Start Docker for PostgreSQL integration and browser tests.');
const hooks = command('git', ['config', '--get', 'core.hooksPath']);
requireCheck('Git hooks', hooks.success && hooks.output === '.githooks', 'Run git config core.hooksPath .githooks in this checkout.');
for (const [name, args] of [['ruby', ['--version']], ['jq', ['--version']]]) {
  requireCheck(name, command(name, args).success, `Install ${name} for workflow regression tests.`);
}
process.exitCode = failures ? 1 : 0;
