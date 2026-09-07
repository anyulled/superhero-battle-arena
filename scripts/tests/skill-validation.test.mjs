import assert from 'node:assert/strict';
import { spawnSync } from 'node:child_process';
import { mkdir, mkdtemp, rm, symlink, writeFile } from 'node:fs/promises';
import { tmpdir } from 'node:os';
import { join, resolve } from 'node:path';
import test from 'node:test';
import {
  validateAllSkills, validateConfig, validateSkill,
} from '../../.agents/skills/skill-creator/scripts/quick_validate.mjs';

const validator = resolve('.agents/skills/skill-creator/scripts/quick_validate.mjs');
const validFrontmatter = '---\nname: example-skill\ndescription: Validate an example skill.\n---\n';

async function fixture(context, frontmatter = validFrontmatter) {
  const directory = await mkdtemp(join(tmpdir(), 'arena-skill-validation-'));
  context.after(() => rm(directory, { recursive: true, force: true }));
  const canonical = join(directory, 'canonical');
  const aliases = join(directory, 'aliases');
  const skill = join(canonical, 'example-skill');
  await mkdir(skill, { recursive: true });
  await mkdir(aliases);
  await writeFile(join(skill, 'SKILL.md'), frontmatter);
  return { canonical, aliases, skill };
}

test('accepts a valid canonical skill and its compatibility symlink', async context => {
  const { canonical, aliases, skill } = await fixture(context);
  await symlink(skill, join(aliases, 'example-skill'));

  const result = await validateAllSkills(canonical, aliases);

  assert.deepEqual(result, { count: 1, errors: [] });
});

for (const [label, frontmatter, expected] of [
  ['missing frontmatter', '# Example', /frontmatter delimiters/],
  ['malformed YAML', '---\nname: [\n---\n', /Frontmatter:/],
  ['duplicate YAML keys', `${validFrontmatter.replace('description:', 'name: other\ndescription:')}`, /Map keys must be unique/],
  ['empty name', validFrontmatter.replace('name: example-skill', 'name: ""'), /nonempty lowercase/],
  ['empty description', validFrontmatter.replace('Validate an example skill.', '""'), /Description must be a nonempty/],
  ['unknown metadata', validFrontmatter.replace('description:', 'unexpected: true\ndescription:'), /Unexpected frontmatter property/],
]) {
  test(`rejects ${label}`, async context => {
    const { skill } = await fixture(context, frontmatter);

    const errors = await validateSkill(skill);

    assert.match(errors.join('\n'), expected);
  });
}

test('configuration failure exits nonzero despite valid directory structure', async context => {
  const { skill } = await fixture(context);
  await mkdir(join(skill, 'assets'));
  await writeFile(join(skill, 'assets/config.yaml'),
    'skill:\n  name: example-skill\n  version: "1.0"\nsettings:\n  log_level: invalid\n');

  const result = spawnSync(process.execPath, [validator, skill], { encoding: 'utf8' });

  assert.equal(result.status, 1);
  assert.match(result.stderr, /Invalid configuration log_level/);
});

for (const text of ['null', '[]', 'skill: []', 'skill: [', 'skill:\n  name: other\n']) {
  test(`rejects invalid configuration shape ${JSON.stringify(text)}`, () => {
    const expectedName = 'example-skill';

    const errors = validateConfig(text, expectedName);

    assert.ok(errors.length > 0);
  });
}

test('rejects missing local Markdown targets', async context => {
  const { skill } = await fixture(context, `${validFrontmatter}\n[Guide](references/missing.md)\n`);

  const errors = await validateSkill(skill);

  assert.match(errors.join('\n'), /Broken local Markdown link/);
});

test('does not treat external links or fenced examples as local resources', async context => {
  const { skill } = await fixture(context,
    `${validFrontmatter}\n[Online](https://example.invalid)\n[Section](#section)\n\`\`\`markdown\n[Example](missing.md)\n\`\`\`\n`);

  const errors = await validateSkill(skill);

  assert.deepEqual(errors, []);
});

test('rejects compatibility copies instead of symlinks', async context => {
  const { canonical, aliases } = await fixture(context);
  await mkdir(join(aliases, 'example-skill'));

  const result = await validateAllSkills(canonical, aliases);

  assert.match(result.errors.join('\n'), /must be a symlink/);
});

test('rejects a broken compatibility symlink', async context => {
  const { canonical, aliases } = await fixture(context);
  await symlink(join(canonical, 'missing'), join(aliases, 'example-skill'));

  const result = await validateAllSkills(canonical, aliases);

  assert.match(result.errors.join('\n'), /missing or broken compatibility link/);
});

test('rejects a compatibility link to another directory', async context => {
  const { canonical, aliases } = await fixture(context);
  await symlink(aliases, join(aliases, 'example-skill'));

  const result = await validateAllSkills(canonical, aliases);

  assert.match(result.errors.join('\n'), /must resolve to the canonical skill/);
});

test('structured Java skills reject missing configuration and required directories', async context => {
  const { canonical } = await fixture(context);
  const skill = join(canonical, 'java-testing');
  await mkdir(skill);
  await writeFile(join(skill, 'SKILL.md'), validFrontmatter.replace('example-skill', 'java-testing'));

  const errors = await validateSkill(skill);

  assert.match(errors.join('\n'), /Missing required directory/);
  assert.match(errors.join('\n'), /Cannot read assets\/config.yaml/);
});
