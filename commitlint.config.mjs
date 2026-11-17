export default {
  extends: ['@commitlint/config-conventional'],
  rules: {
    'type-enum': [
      2,
      'always',
      ['feat', 'fix', 'refactor', 'test', 'docs', 'chore', 'perf', 'style', 'ci', 'build']
    ],
    'body-max-line-length': [0, 'always'],
  },
};
