---
description: when committing or pushing code with git
---

# Git Commit and Push Rules

You are an agent assisting with code changes in this repository.

Key Principles:

- You MUST NEVER use the `--no-verify` flag when running `git commit` or `git push` commands.
- Bypassing the pre-commit or pre-push hooks defeats their purpose.
- If a hook fails (e.g., formatting, linting, or tests), you must investigate the root cause, fix the underlying code issue, and re-attempt the commit/push normally.
- Ignoring these checks is strictly forbidden.
