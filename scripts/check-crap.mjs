import { readFile } from 'node:fs/promises';
import path from 'node:path';
import process from 'node:process';

export const CRAP_THRESHOLD = 8;

const attributePattern = /([\w:-]+)="([^"]*)"/g;

function decodeXml(value) {
  return value
    .replaceAll('&lt;', '<')
    .replaceAll('&gt;', '>')
    .replaceAll('&quot;', '"')
    .replaceAll('&apos;', "'")
    .replaceAll('&amp;', '&');
}

function attributes(fragment) {
  const result = {};
  for (const match of fragment.matchAll(attributePattern)) {
    result[match[1]] = decodeXml(match[2]);
  }
  return result;
}

function counters(methodBody) {
  const result = {};
  for (const match of methodBody.matchAll(/<counter\b([^>]*)\/>/g)) {
    const values = attributes(match[1]);
    result[values.type] = {
      covered: Number(values.covered),
      missed: Number(values.missed),
    };
  }
  return result;
}

function isMethodIncluded(method) {
  return method.name !== '<init>' && method.name !== '<clinit>' && !method.name.startsWith('lambda$');
}

function methodMetric(classAttributes, methodAttributes, methodCounters) {
  const complexity = methodCounters.COMPLEXITY;
  const instructions = methodCounters.INSTRUCTION;
  if (!complexity || !instructions || !isMethodIncluded(methodAttributes)) {
    return null;
  }

  const instructionCount = instructions.covered + instructions.missed;
  if (instructionCount === 0) {
    return null;
  }

  const complexityValue = complexity.covered + complexity.missed;
  const coverage = instructions.covered / instructionCount;
  const crap = complexityValue ** 2 * (1 - coverage) ** 3 + complexityValue;
  return {
    className: classAttributes.name.replaceAll('/', '.'),
    methodName: methodAttributes.name,
    line: Number(methodAttributes.line),
    complexity: complexityValue,
    coverage,
    crap,
  };
}

export function parseReport(xml) {
  const metrics = [];
  for (const classMatch of xml.matchAll(/<class\b([^>]*)>([\s\S]*?)<\/class>/g)) {
    const classAttributes = attributes(classMatch[1]);
    for (const methodMatch of classMatch[2].matchAll(/<method\b([^>]*)>([\s\S]*?)<\/method>/g)) {
      const metric = methodMetric(
        classAttributes,
        attributes(methodMatch[1]),
        counters(methodMatch[2]),
      );
      if (metric) {
        metrics.push(metric);
      }
    }
  }
  return metrics.sort((left, right) => right.crap - left.crap || left.className.localeCompare(right.className) || left.line - right.line);
}

function formatMetric(metric) {
  return `${metric.crap.toFixed(2).padStart(8)} ${metric.coverage.toLocaleString('en-US', { style: 'percent', minimumFractionDigits: 1, maximumFractionDigits: 1 }).padStart(8)} ${String(metric.complexity).padStart(4)} ${String(metric.line).padStart(5)} ${metric.className}#${metric.methodName}`;
}

export function formatReport(metrics, threshold = CRAP_THRESHOLD) {
  const lines = [
    `CRAP report (threshold ${threshold.toFixed(1)})`,
    '   CRAP     COVERAGE  CC  LINE  METHOD',
    ...metrics.map(formatMetric),
  ];
  const maximum = metrics[0]?.crap ?? 0;
  lines.push(`Maximum CRAP: ${maximum.toFixed(2)}`);
  return { text: lines.join('\n'), maximum };
}

function usage() {
  return 'Usage: node scripts/check-crap.mjs [--report <path>]';
}

function reportPath(argumentsList) {
  if (argumentsList.length === 0) {
    return path.resolve('target/site/jacoco/jacoco.xml');
  }
  if (argumentsList.length === 2 && argumentsList[0] === '--report') {
    return path.resolve(argumentsList[1]);
  }
  throw new Error(usage());
}

export async function run(argumentsList = process.argv.slice(2), output = process.stdout, errors = process.stderr) {
  let file;
  try {
    file = reportPath(argumentsList);
  } catch (error) {
    errors.write(`${error.message}\n`);
    return 1;
  }

  let xml;
  try {
    xml = await readFile(file, 'utf8');
  } catch {
    errors.write(`JaCoCo XML report not found: ${file}\n`);
    return 1;
  }

  const report = formatReport(parseReport(xml));
  output.write(`${report.text}\n`);
  if (report.maximum > CRAP_THRESHOLD) {
    errors.write(`CRAP threshold exceeded: ${report.maximum.toFixed(2)} > ${CRAP_THRESHOLD.toFixed(1)}\n`);
    return 2;
  }
  return 0;
}

if (process.argv[1] && path.resolve(process.argv[1]) === path.resolve(new URL(import.meta.url).pathname)) {
  process.exitCode = await run();
}
