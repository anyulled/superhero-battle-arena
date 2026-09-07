import assert from 'node:assert/strict';
import { execFile, spawn } from 'node:child_process';
import { randomBytes } from 'node:crypto';
import { createWriteStream } from 'node:fs';
import { copyFile, mkdir, mkdtemp, readdir, rm, writeFile } from 'node:fs/promises';
import { createServer } from 'node:net';
import { tmpdir } from 'node:os';
import { join, resolve } from 'node:path';
import { setTimeout as delay } from 'node:timers/promises';
import { promisify } from 'node:util';

const execute = promisify(execFile);
const interrupted = new AbortController();
const applications = [];
const artifactDirectory = resolve('test-results/postgres-startup');
const temporaryDirectory = await mkdtemp(join(tmpdir(), 'arena-startup-'));
const databasePassword = randomBytes(24).toString('hex');
const adminPassword = randomBytes(24).toString('hex');
const java = process.env.JAVA_HOME ? join(process.env.JAVA_HOME, 'bin', 'java') : 'java';
let containerId;

process.on('SIGINT', () => interrupted.abort(new Error('Startup regression interrupted.')));
process.on('SIGTERM', () => interrupted.abort(new Error('Startup regression terminated.')));

async function docker(...arguments_) {
  const result = await execute('docker', arguments_, { timeout: 120_000 });
  return result.stdout.trim();
}

async function query(statement) {
  return docker('exec', containerId, 'psql', '-U', 'postgres', '-d', 'arena_startup', '-At', '-c', statement);
}

async function applicationConnections() {
  return Number(await query("SELECT count(*) FROM pg_stat_activity WHERE usename='arena_app'"));
}

async function availablePort() {
  const listener = createServer();
  await new Promise((resolveListen, reject) => {
    listener.once('error', reject);
    listener.listen(0, '127.0.0.1', resolveListen);
  });
  const { port } = listener.address();
  await new Promise((resolveClose, reject) => listener.close(error => error ? reject(error) : resolveClose()));
  return port;
}

async function prepareDatabase() {
  containerId = await docker(
    'run', '--detach', '--rm', '--label', 'org.barcelonajug.harness=postgres-startup',
    '--publish', '127.0.0.1::5432', '--env', `POSTGRES_PASSWORD=${databasePassword}`, 'postgres:16',
  );
  for (let attempt = 0; attempt < 120; attempt += 1) {
    interrupted.signal.throwIfAborted();
    try {
      await docker('exec', containerId, 'pg_isready', '-h', '127.0.0.1', '-U', 'postgres');
      break;
    } catch (error) {
      if (attempt === 119) throw error;
      await delay(250);
    }
  }
  await docker('exec', containerId, 'psql', '-U', 'postgres', '-At', '-c',
    `CREATE ROLE arena_app LOGIN PASSWORD '${databasePassword}' CONNECTION LIMIT 5`);
  await docker('exec', containerId, 'psql', '-U', 'postgres', '-At', '-c',
    'CREATE DATABASE arena_startup OWNER arena_app');
  const published = await docker('port', containerId, '5432/tcp');
  return `jdbc:postgresql://127.0.0.1:${published.split(':').at(-1)}/arena_startup`;
}

async function startApplication(jarPath, databaseUrl, number) {
  const port = await availablePort();
  const log = createWriteStream(join(artifactDirectory, `application-${number}.log`));
  const inheritedEnvironment = Object.fromEntries(
    Object.entries(process.env).filter(([name]) => !name.startsWith('SPRING_')),
  );
  const application = { port, log, process: null, error: null };
  application.process = spawn(java, ['-jar', jarPath], {
    stdio: ['ignore', 'pipe', 'pipe'],
    env: {
      ...inheritedEnvironment,
      SPRING_PROFILES_ACTIVE: 'postgres',
      SPRING_DOCKER_COMPOSE_ENABLED: 'false',
      SPRING_DATASOURCE_URL: databaseUrl,
      SPRING_DATASOURCE_USERNAME: 'arena_app',
      SPRING_DATASOURCE_PASSWORD: databasePassword,
      SERVER_PORT: String(port),
      SERVER_ADDRESS: '127.0.0.1',
      ADMIN_PASSWORD: adminPassword,
    },
  });
  application.process.stdout.pipe(log);
  application.process.stderr.pipe(log);
  application.process.on('error', error => { application.error = error; });
  applications.push(application);
  return application;
}

async function isHealthy(application) {
  if (application.error) throw application.error;
  if (application.process.exitCode !== null || application.process.signalCode !== null) {
    throw new Error(`Application on port ${application.port} exited. Inspect ${artifactDirectory}.`);
  }
  let response;
  try {
    response = await fetch(`http://127.0.0.1:${application.port}/actuator/health`, {
      headers: { Authorization: `Basic ${Buffer.from(`admin:${adminPassword}`).toString('base64')}` },
      signal: AbortSignal.timeout(1_000),
    });
  } catch (error) {
    if (error.name === 'TimeoutError' || error.cause?.code === 'ECONNREFUSED') return false;
    throw error;
  }
  if (response.status === 401 || response.status === 403) {
    throw new Error(`Health authentication failed (${response.status}).`);
  }
  if (!response.ok) return false;
  return (await response.json()).status === 'UP';
}

async function awaitHealthyApplications() {
  const started = Date.now();
  const samples = [];
  while (Date.now() - started < 120_000) {
    interrupted.signal.throwIfAborted();
    const connections = await applicationConnections();
    samples.push({ elapsedMs: Date.now() - started, connections });
    assert.ok(connections <= 4, `Two instances exceeded their four-connection budget: ${connections}`);
    const readiness = await Promise.all(applications.map(isHealthy));
    if (readiness.every(Boolean)) return samples;
    await delay(150);
  }
  throw new Error(`Both applications must start within 120 seconds. Inspect ${artifactDirectory}.`);
}

async function stopApplications() {
  await Promise.all(applications.map(async ({ process: child, log }) => {
    if (child.pid && child.exitCode === null && child.signalCode === null) {
      await new Promise(resolveExit => {
        const deadline = setTimeout(() => child.kill('SIGKILL'), 5_000);
        child.once('exit', () => {
          clearTimeout(deadline);
          resolveExit();
        });
        child.kill('SIGTERM');
      });
    }
    log.end();
  }));
}

try {
  await mkdir(artifactDirectory, { recursive: true });
  await rm(join(artifactDirectory, 'result.json'), { force: true });
  const jarPath = join(temporaryDirectory, 'application.jar');
  await copyFile(resolve(process.env.STARTUP_TEST_JAR || 'target/superhero-battle-arena-0.0.1-SNAPSHOT.jar'), jarPath);
  const migrationDirectories = ['common', 'postgresql'];
  const migrations = (await Promise.all(migrationDirectories.map(directory =>
    readdir(`src/main/resources/db/migration/${directory}`)))).flat().filter(name => /^V\d+__.*\.sql$/.test(name));
  const databaseUrl = await prepareDatabase();
  const role = await query("SELECT rolconnlimit || ':' || rolsuper FROM pg_roles WHERE rolname='arena_app'");
  assert.equal(role, '5:false', 'The application must use a restricted non-superuser role.');

  await Promise.all([startApplication(jarPath, databaseUrl, 1), startApplication(jarPath, databaseUrl, 2)]);
  const samples = await awaitHealthyApplications();

  const history = await query('SELECT count(*) || \':\' || count(DISTINCT version) || \':\' || bool_and(success) FROM flyway_schema_history');
  assert.equal(history, `${migrations.length}:${migrations.length}:true`, 'Each migration must be successfully recorded exactly once.');
  const peak = Math.max(...samples.map(sample => sample.connections));
  assert.ok(peak >= 2, 'The regression must observe real application database connections.');
  const steadyConnections = await applicationConnections();
  assert.ok(steadyConnections >= 2 && steadyConnections <= 4, 'Two running instances must fit the shared connection budget.');
  await stopApplications();
  assert.equal(await applicationConnections(), 0, 'All application connections must close after shutdown.');
  await writeFile(join(artifactDirectory, 'result.json'), JSON.stringify({
    instances: 2, roleConnectionLimit: 5, sampledPeakConnections: peak,
    steadyConnections, successfulUniqueMigrations: migrations.length, connectionsAfterShutdown: 0, samples,
  }, null, 2));
  console.log(`PASS: two PostgreSQL instances started; peak ${peak}/5 connections; ${migrations.length} unique migrations; clean shutdown.`);
} catch (error) {
  console.error(error);
  process.exitCode = 1;
} finally {
  try {
    await stopApplications();
    if (containerId) await docker('stop', '--time', '5', containerId);
    await rm(temporaryDirectory, { recursive: true, force: true });
  } catch (error) {
    console.error('Startup regression cleanup failed:', error);
    process.exitCode = 1;
  }
}
