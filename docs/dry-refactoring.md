# DRY Refactoring and Code Duplication Management

This document describes how code duplication is monitored, managed, and resolved in this repository using [jscpd](https://github.com/kucherenko/jscpd) and the `dry-refactoring` agent skill.

---

## 1. Overview & Constraints

This repository enforces strict code quality standards, including a zero-tolerance duplication gate:
- **Maximum Duplication Threshold:** $\le 10\%$.
- **Pre-Push Hook & CI:** Verified before push and in CI via `npm run lint:duplication` (defined in `.githooks/pre-push` and `.github/workflows/ci.yml`).
- **Zero-Tolerance Rule:** Git hooks and linter configurations must **never** be bypassed or commented out.
- **Rule on Comments:** No explanatory inline or doc comments (WHAT the code does); refactorings must remain self-documenting.

When new features or tests push duplication above 10%, the `dry-refactoring` skill provides a systematic protocol to reduce duplication cleanly without creating premature or brittle abstractions.

---

## 2. What is `dry-refactoring`?

`dry-refactoring` is a specialized workflow skill for AI agents and developers based on `jscpd`. Instead of haphazardly extracting code or deleting necessary tests, it guides systematic identification and resolution of clone patterns:

### Clone Classifications
1. **Type-1 (Exact Clones):** Identical code blocks copied across files. Highest refactoring priority.
2. **Type-2 (Renamed Clones):** Same algorithmic structure, but variable names, constants, or identifiers differ (`(renamed)`).
3. **Type-3 (Near-Miss Clones):** Functions or blocks sharing identical structure with small gaps or added statements (`[~N gap]`, `[~N ast]`).

---

## 3. Refactoring Strategies

When resolving duplication, choose the pattern matching the clone type:

| Pattern | Clone Type | Typical Use Case |
|---|---|---|
| **Base Class / Support Fixture** | Type-1 / Type-2 across tests | Repeated test setup, WebMvc execution, or entity builders (e.g. `AbstractTournamentE2eIT`). |
| **Extract Method / Utility** | Type-1 across services/domain | Identical calculation or parsing logic in multiple places. |
| **Parameterize** | Type-2 (renamed) | Algorithmic logic differing only by parameters, literals, or configuration. |
| **Compose / Invert Dependency** | Type-3 (near-miss) | Shared skeleton with diverging steps handled via callbacks or strategies. |

> [!IMPORTANT]
> **Triage Before Refactoring:** Do not unify intentional boilerplate (such as independent DTOs, route declarations, or standard model constructors). Any refactoring must produce clear, meaningful abstractions.

---

## 4. Agent Workflow

When invoked or tasked with resolving duplication, the agent follows this sequence:

1. **Locate hotspots:**
   Run `jscpd` with AI reporter and summary to find the files with the highest duplication percentage (`dup%`):
   ```bash
   npx jscpd --reporters ai --summary src/
   ```
2. **Inspect and Triage:**
   Read both duplicated fragments. Confirm whether extracting an abstraction simplifies the codebase and allows deletion of duplicate lines.
3. **Apply Clean Refactoring:**
   Extract shared logic to a common helper, base test class, or utility according to hexagonal architecture boundaries.
4. **Verify Duplication Metric:**
   Run the project duplication check:
   ```bash
   npm run lint:duplication
   ```
5. **Verify Build and Formatting:**
   Ensure no regressions in code style or tests:
   ```bash
   ./mvnw spotless:apply
   ./mvnw test
   ```

---

## 5. Quick Commands Reference

```bash
# Check duplication via project script
npm run lint:duplication

# AI-optimized summary report (ranked hotspots)
npx jscpd --reporters ai --summary src/

# Inspect renamed clones (Type-2)
npx jscpd --reporters ai --ignore-identifiers --min-tokens 70 src/
```
