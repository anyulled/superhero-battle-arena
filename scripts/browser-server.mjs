import { spawn, execFile } from 'node:child_process';
import { randomBytes } from 'node:crypto';
import { copyFile, mkdir, mkdtemp, rm } from 'node:fs/promises';
import { tmpdir } from 'node:os';
import { join, resolve } from 'node:path';
import { promisify } from 'node:util';

const execute = promisify(execFile);
const temporaryDirectory = await mkdtemp(join(tmpdir(), 'arena-browser-'));
const databasePassword = randomBytes(24).toString('hex');
const port = process.env.BROWSER_TEST_PORT || '18085';
const jarSource = resolve(
  process.env.BROWSER_TEST_JAR || 'target/superhero-battle-arena-0.0.1-SNAPSHOT.jar',
);
const jarPath = join(temporaryDirectory, 'application.jar');
let containerId;
let application;
let stopping = false;
let databaseStartup;

async function shutdown(exitCode) {
  if (stopping) return;
  stopping = true;
  try {
    if (databaseStartup && !containerId) {
      const started = await databaseStartup;
      containerId = started.stdout.trim();
    }
    if (application?.pid && application.exitCode === null && application.signalCode === null) {
      await new Promise(resolveExit => {
        const deadline = setTimeout(() => application.kill('SIGKILL'), 5_000);
        application.once('exit', () => {
          clearTimeout(deadline);
          resolveExit();
        });
        application.kill('SIGTERM');
      });
    }
    if (containerId) await execute('docker', ['stop', '--time', '5', containerId]);
    await rm(temporaryDirectory, { recursive: true, force: true });
  } catch (error) {
    console.error('Browser fixture cleanup failed:', error.message);
    exitCode = 1;
  }
  process.exit(exitCode);
}

process.on('SIGTERM', () => void shutdown(0));
process.on('SIGINT', () => void shutdown(130));

async function start() {
  await copyFile(jarSource, jarPath);
  if (stopping) return;
  await mkdir('test-results', { recursive: true });
  if (stopping) return;
  databaseStartup = execute('docker', [
    'run', '--detach', '--rm',
    '--label', 'org.barcelonajug.harness=browser',
    '--publish', '127.0.0.1::5432',
    '--env', 'POSTGRES_DB=arena_browser',
    '--env', 'POSTGRES_USER=arena_browser',
    '--env', `POSTGRES_PASSWORD=${databasePassword}`,
    'postgres:16',
  ]);
  const started = await databaseStartup;
  containerId = started.stdout.trim();
  if (stopping) return;
  const published = await execute('docker', ['port', containerId, '5432/tcp']);
  const databasePort = published.stdout.trim().split(':').at(-1);
  let databaseReady = false;
  for (let attempt = 0; attempt < 60 && !stopping; attempt += 1) {
    try {
      await execute('docker', ['exec', containerId, 'pg_isready', '-h', '127.0.0.1', '-U', 'arena_browser', '-d', 'arena_browser']);
      databaseReady = true;
      break;
    } catch {
      await new Promise(resolveDelay => setTimeout(resolveDelay, 500));
    }
  }
  if (stopping) return;
  if (!databaseReady) throw new Error('PostgreSQL did not become ready within 30 seconds.');
  const java = process.env.JAVA_HOME ? join(process.env.JAVA_HOME, 'bin', 'java') : 'java';
  const inheritedEnvironment = Object.fromEntries(
    Object.entries(process.env).filter(([name]) => !name.startsWith('SPRING_')),
  );
  application = spawn(java, ['-jar', jarPath], {
    stdio: 'inherit',
    env: {
      ...inheritedEnvironment,
      SPRING_PROFILES_ACTIVE: 'postgres',
      SPRING_DOCKER_COMPOSE_ENABLED: 'false',
      SPRING_DATASOURCE_URL: `jdbc:postgresql://127.0.0.1:${databasePort}/arena_browser`,
      SPRING_DATASOURCE_USERNAME: 'arena_browser',
      SPRING_DATASOURCE_PASSWORD: databasePassword,
      ADMIN_PASSWORD: 'browser-test-password',
      SERVER_PORT: port,
      SERVER_ADDRESS: '127.0.0.1',
    },
  });
  application.on('error', error => {
    console.error('Browser application failed to start:', error.message);
    void shutdown(1);
  });
  application.on('exit', (code, signal) => {
    if (!stopping) {
      console.error(`Browser application exited before test completion (${code ?? signal}).`);
      void shutdown(code || 1);
    }
  });
}

try {
  await start();
} catch (error) {
  console.error('Browser fixture failed:', error.message);
  await shutdown(1);
}
