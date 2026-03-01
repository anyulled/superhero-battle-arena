# AI Harness Scorecard: superhero-battle-arena

**Grade: C** (61.3/100) | Basic practices present but insufficient for safe AI scaling.

- **Repository**: `/home/runner/work/superhero-battle-arena/superhero-battle-arena`
- **Languages**: java
- **Assessed**: 2026-03-01 12:06 UTC
- **Checks**: 20/31 passed

## Summary

| Category | Weight | Score | Checks |
|----------|--------|-------|--------|
| Architectural Documentation | 20% | 100% [##########] | 5/5 |
| Mechanical Constraints | 25% | 27% [###-------] | 2/7 |
| Testing & Stability | 25% | 42% [####------] | 4/8 |
| Review & Drift Prevention | 15% | 100% [##########] | 6/6 |
| AI-Specific Safeguards | 15% | 60% [######----] | 3/5 |

## Architectural Documentation (100%)

### [PASS] Architecture Documentation (5/5)

_matklad ARCHITECTURE.md guide_

**Evidence**: Found: ARCHITECTURE.md

### [PASS] Agent Instructions (5/5)

_OpenAI Harness Engineering (2026)_

**Evidence**: Found: CLAUDE.md

### [PASS] Architecture Decision Records (3/3)

_DORA 2025 Report - AI-accessible documentation_

**Evidence**: Found ADR directory: docs/adr

### [PASS] Module Boundary Documentation (4/4)

_matklad ARCHITECTURE.md - constraints as absences_

**Evidence**: Module boundary constraints found in docs/adr/0002-hexagonal-architecture.md

### [PASS] API Documentation (3/3)

_DORA 2025 - AI-accessible documentation_

**Evidence**: Doc generation found in CI


## Mechanical Constraints (27%)

### [PASS] CI Pipeline (3/3)

_DORA 2025 Report_

**Evidence**: CI detected: github, github, github, github, github

### [FAIL] Linter Enforcement (0/4)

_OpenAI Harness Engineering - mechanical constraints_

**Evidence**: No linter found in CI

**Remediation**: Add a linter to CI that blocks merges on violations (e.g. cargo clippy -- -D warnings, eslint --max-warnings 0).

### [FAIL] Formatter Enforcement (0/3)

_OpenAI Harness Engineering - mechanical constraints_

**Evidence**: No formatter check found in CI

**Remediation**: Add a formatter check to CI (e.g. cargo fmt --all -- --check, prettier --check).

### [PASS] Type Safety (3/3)

_SlopCodeBench - preventing subtle type errors_

**Evidence**: JVM language: type safety enforced by compiler

### [FAIL] Dependency Auditing (0/4)

_Blog: security infrastructure reliability_

**Evidence**: No dependency auditing found

**Remediation**: Add cargo deny/audit, npm audit, pip-audit, or Snyk to CI as a blocking check.

### [FAIL] Conventional Commits (0/2)

_DORA 2025 - working in small batches_

**Evidence**: No conventional commit enforcement found

**Remediation**: Add commitlint or equivalent to CI to enforce consistent commit message format.

### [FAIL] Unsafe Code Policy (0/3)

_Blog: 80% problem in AI-generated code_

**Evidence**: No explicit policy against unsafe code patterns

**Remediation**: Add unsafe_code = forbid (Rust), security linting (semgrep/bandit), or ESLint rules against dangerous patterns.


## Testing & Stability (42%)

### [PASS] Test Suite (2/3)

_Kent Beck - tests define what correct means_

**Evidence**: Tests found but not confirmed in CI

**Remediation**: Add test execution to your CI pipeline.

### [PASS] Feature Matrix Testing (3/3)

_DORA 2025 - stability through comprehensive testing_

**Evidence**: Matrix/parallel testing strategy found in CI

### [FAIL] Code Coverage (0/4)

_DORA 2025 - stability feedback loops_

**Evidence**: No code coverage measurement found

**Remediation**: Add cargo llvm-cov, pytest-cov, istanbul/c8, or equivalent to CI. Even informational coverage provides a feedback loop.

### [PASS] Mutation Testing (4/4)

_SlopCodeBench - code that 'appears correct but is unreliable'_

**Evidence**: Mutation testing found in CI

### [FAIL] Property-Based Testing (0/3)

_Blog: catching edge cases in AI-generated code_

**Evidence**: No property-based testing found

**Remediation**: Add proptest (Rust), hypothesis (Python), or fast-check (JS/TS) for testing invariants with random structured inputs.

### [FAIL] Fuzz Testing (0/3)

_Blog: 80% problem - catching what AI misses_

**Evidence**: No fuzz testing found

**Remediation**: Add fuzz targets for parsing-heavy and input-handling code paths.

### [FAIL] Contract / Compatibility Tests (0/3)

_OpenAI Harness Engineering - mechanical constraints_

**Evidence**: No contract or compatibility tests found

**Remediation**: Add contract tests that verify external interface stability (golden fixtures, snapshot tests, wire-format checks).

### [PASS] Tests Block Merge (2/2)

_DORA 2025 - stability metrics_

**Evidence**: All test jobs are blocking: test


## Review & Drift Prevention (100%)

### [PASS] Code Review Required (4/4)

_OpenAI Harness Engineering - author/reviewer separation_

**Evidence**: CODEOWNERS file found: .github/CODEOWNERS

### [PASS] Scheduled CI Jobs (3/3)

_OpenAI Harness Engineering - garbage collection agents_

**Evidence**: Scheduled CI pipeline found

### [PASS] Stale Documentation Detection (2/2)

_OpenAI Harness Engineering - quality drift_

**Evidence**: TODO/FIXME scanning found in CI

### [PASS] PR/MR Template (2/2)

_DORA 2025 - working in small batches_

**Evidence**: PR/MR template found: .github/PULL_REQUEST_TEMPLATE.md

### [PASS] Automated Code Review (2/2)

_OpenAI Harness Engineering - separate authoring and reviewing agents_

**Evidence**: Automated review tool configured: .github/dependabot.yml

### [PASS] Documentation Sync Check (2/2)

_OpenAI Harness Engineering - curated knowledge base_

**Evidence**: Doc sync check found in CI: diff\s+.*\.md


## AI-Specific Safeguards (60%)

### [PASS] AI Usage Norms (4/4)

_DORA 2025 - clear organizational stance on AI use_

**Evidence**: AI usage norms found in CLAUDE.md

### [PASS] Small Batch Enforcement (3/3)

_DORA 2025 - working in small batches_

**Evidence**: Small batch guidelines found in CONTRIBUTING.md

### [FAIL] Design-Before-Code Culture (0/3)

_Blog: cognitive offloading guardrails_

**Evidence**: No design-before-code process found

**Remediation**: Create docs/rfcs/ or docs/designs/ directory. Document a process where significant changes start with a design doc or plan before implementation.

### [FAIL] Error Handling Policy (0/3)

_Blog: AI agents deleting tests, using expect()_

**Evidence**: No error handling policy found

**Remediation**: Add clippy lints (unwrap_used, expect_used) for Rust, ESLint rules for JS/TS, or document error handling patterns in agent instructions.

### [PASS] Security-Critical Path Marking (2/2)

_Blog: 80% problem in security infrastructure_

**Evidence**: CODEOWNERS found: .github/CODEOWNERS


## References

- Blog: 80% problem - catching what AI misses
- Blog: 80% problem in AI-generated code
- Blog: 80% problem in security infrastructure
- Blog: AI agents deleting tests, using expect()
- Blog: catching edge cases in AI-generated code
- Blog: cognitive offloading guardrails
- Blog: security infrastructure reliability
- DORA 2025 - AI-accessible documentation
- DORA 2025 - clear organizational stance on AI use
- DORA 2025 - stability feedback loops
- DORA 2025 - stability metrics
- DORA 2025 - stability through comprehensive testing
- DORA 2025 - working in small batches
- DORA 2025 Report
- DORA 2025 Report - AI-accessible documentation
- Kent Beck - tests define what correct means
- OpenAI Harness Engineering (2026)
- OpenAI Harness Engineering - author/reviewer separation
- OpenAI Harness Engineering - curated knowledge base
- OpenAI Harness Engineering - garbage collection agents
- OpenAI Harness Engineering - mechanical constraints
- OpenAI Harness Engineering - quality drift
- OpenAI Harness Engineering - separate authoring and reviewing agents
- SlopCodeBench - code that 'appears correct but is unreliable'
- SlopCodeBench - preventing subtle type errors
- matklad ARCHITECTURE.md - constraints as absences
- matklad ARCHITECTURE.md guide

---
*Generated by [ai-harness-scorecard](https://github.com/markmishaev/ai-harness-scorecard)*