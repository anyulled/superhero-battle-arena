import { lstat, readFile, readdir, realpath, stat } from 'node:fs/promises';
import { basename, resolve } from 'node:path';
import { pathToFileURL } from 'node:url';
import { parseDocument } from 'yaml';

const repositoryRoot = resolve(import.meta.dirname, '../../../..');
const allowedProperties = new Set([
  'name', 'description', 'license', 'allowed-tools', 'metadata',
  'triggers', 'role', 'scope', 'output-format', 'sasmp_version',
  'version', 'bonded_agent', 'bond_type', 'parameters',
]);
const structuredSkills = new Set(['java-testing', 'java-spring-boot']);

function mapping(value) {
  return value !== null && typeof value === 'object' && !Array.isArray(value);
}

function parseMapping(text, label) {
  const document = parseDocument(text, { uniqueKeys: true });
  if (document.errors.length) throw new Error(`${label}: ${document.errors.map(error => error.message).join('; ')}`);
  const value = document.toJS({ maxAliasCount: 100 });
  if (!mapping(value)) throw new Error(`${label} must be a YAML mapping.`);
  return value;
}

export function validateConfig(text, expectedName) {
  const errors = [];
  let config;
  try {
    config = parseMapping(text, 'Configuration');
  } catch (error) {
    return [error.message];
  }
  if (!mapping(config.skill)) {
    errors.push('Configuration must contain a skill mapping.');
  } else {
    if (config.skill.name !== expectedName) errors.push('Configuration skill.name must match the skill directory.');
    if (typeof config.skill.version !== 'string' || !config.skill.version.trim()) errors.push('Configuration skill.version must be a nonempty string.');
  }
  if (config.settings !== undefined) {
    if (!mapping(config.settings)) {
      errors.push('Configuration settings must be a mapping.');
    } else if (config.settings.log_level !== undefined && !['debug', 'info', 'warn', 'error'].includes(config.settings.log_level)) {
      errors.push(`Invalid configuration log_level: ${config.settings.log_level}`);
    }
  }
  return errors;
}

export async function validateSkill(skillDirectory) {
  const errors = [];
  const name = basename(skillDirectory);
  let content;
  try {
    content = await readFile(resolve(skillDirectory, 'SKILL.md'), 'utf8');
  } catch (error) {
    return [`Cannot read SKILL.md: ${error.message}`];
  }
  const frontmatterMatch = content.match(/^---\r?\n([\s\S]*?)\r?\n---(?:\r?\n|$)/);
  if (!frontmatterMatch) return ['SKILL.md must begin with valid YAML frontmatter delimiters.'];
  let frontmatter;
  try {
    frontmatter = parseMapping(frontmatterMatch[1], 'Frontmatter');
  } catch (error) {
    return [error.message];
  }
  for (const key of Object.keys(frontmatter)) {
    if (!allowedProperties.has(key)) errors.push(`Unexpected frontmatter property: ${key}`);
  }
  if (typeof frontmatter.name !== 'string' || !/^[a-z0-9]+(?:-[a-z0-9]+)*$/.test(frontmatter.name) || frontmatter.name.length > 64) {
    errors.push('Skill name must be nonempty lowercase hyphen-case and at most 64 characters.');
  } else if (frontmatter.name !== name) {
    errors.push('Frontmatter name must match the skill directory.');
  }
  if (typeof frontmatter.description !== 'string' || !frontmatter.description.trim() || frontmatter.description.length > 1024 || /[<>]/.test(frontmatter.description)) {
    errors.push('Description must be a nonempty string of at most 1024 characters without angle brackets.');
  }
  for (const key of ['metadata', 'parameters']) {
    if (frontmatter[key] !== undefined && !mapping(frontmatter[key])) errors.push(`${key} must be a mapping.`);
  }
  if (frontmatter.triggers !== undefined && (!Array.isArray(frontmatter.triggers) || frontmatter.triggers.some(trigger => typeof trigger !== 'string'))) {
    errors.push('triggers must be a list of strings.');
  }
  if (structuredSkills.has(name)) {
    for (const directory of ['assets', 'scripts', 'references']) {
      try {
        const entries = await readdir(resolve(skillDirectory, directory));
        if (!entries.some(entry => entry !== '.gitkeep')) errors.push(`${directory}/ must contain real content.`);
      } catch {
        errors.push(`Missing required directory: ${directory}/`);
      }
    }
  }
  try {
    const config = await readFile(resolve(skillDirectory, 'assets/config.yaml'), 'utf8');
    errors.push(...validateConfig(config, name));
  } catch (error) {
    if (error.code !== 'ENOENT' || structuredSkills.has(name)) errors.push(`Cannot read assets/config.yaml: ${error.message}`);
  }
  const prose = content.replace(/```[\s\S]*?```/g, '');
  for (const match of prose.matchAll(/\[[^\]]*\]\(([^)]+)\)/g)) {
    const destination = match[1].trim().replace(/^<|>$/g, '');
    if (/^(?:[a-z][a-z\d+.-]*:|#)/i.test(destination)) continue;
    try {
      await stat(resolve(skillDirectory, decodeURIComponent(destination.split('#')[0])));
    } catch {
      errors.push(`Broken local Markdown link: ${destination}`);
    }
  }
  return errors;
}

export async function validateAllSkills(canonicalRoot, compatibilityRoot) {
  const errors = [];
  const directories = await readdir(canonicalRoot, { withFileTypes: true });
  const skills = directories.filter(entry => entry.isDirectory()).map(entry => entry.name).sort();
  if (!skills.length) errors.push('No canonical skills found.');
  for (const name of skills) {
    const skillErrors = await validateSkill(resolve(canonicalRoot, name));
    errors.push(...skillErrors.map(error => `${name}: ${error}`));
    try {
      const alias = resolve(compatibilityRoot, name);
      if (!(await lstat(alias)).isSymbolicLink()) {
        errors.push(`${name}: compatibility entry must be a symlink.`);
      } else if (await realpath(alias) !== await realpath(resolve(canonicalRoot, name))) {
        errors.push(`${name}: compatibility link must resolve to the canonical skill.`);
      }
    } catch (error) {
      errors.push(`${name}: missing or broken compatibility link (${error.code}).`);
    }
  }
  const aliases = await readdir(compatibilityRoot);
  for (const alias of aliases) {
    if (!skills.includes(alias)) errors.push(`Compatibility entry has no canonical skill: ${alias}`);
  }
  return { count: skills.length, errors };
}

export async function runValidation(skillDirectory) {
  const result = skillDirectory
    ? { count: 1, errors: await validateSkill(resolve(skillDirectory)) }
    : await validateAllSkills(resolve(repositoryRoot, '.agents/skills'), resolve(repositoryRoot, '.agent/skills'));
  if (result.errors.length) {
    for (const error of result.errors) console.error(error);
    return 1;
  }
  console.log(`Validated ${result.count} skill${result.count === 1 ? '' : 's'}.`);
  return 0;
}

if (process.argv[1] && import.meta.url === pathToFileURL(resolve(process.argv[1])).href) {
  try {
    if (process.argv.length > 3) throw new Error('Usage: node quick_validate.mjs [skill-directory]');
    process.exitCode = await runValidation(process.argv[2]);
  } catch (error) {
    console.error(error.message);
    process.exitCode = 1;
  }
}
