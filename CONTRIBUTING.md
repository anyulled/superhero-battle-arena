# Contributing Guidelines

Thank you for your interest in contributing to Superhero Battle Arena!

## Local setup

Install Java 25, Docker, Git, Ruby and jq. Select the exact Node version in `.nvmrc` using your version manager, then install npm 11.19.1. Set `JAVA_HOME` and `PATH` to the Java 25 installation.

```sh
nvm install
nvm use
npm install --global npm@11.19.1
npm ci
git config core.hooksPath .githooks
npm run doctor
```

`npm ci` uses the committed lockfile. Do not replace it with an unconstrained install in CI. Node dependencies are development tools; the application still builds with the Maven wrapper.

## Validation commands

| Purpose | Command | Requirements |
| --- | --- | --- |
| Harness regressions | `npm run test:harness` | Locked dependencies, Git, Ruby, jq |
| Skill metadata and aliases | `npm run lint:skills` | Locked dependencies |
| Duplication limit | `npm run lint:duplication` | Locked dependencies |
| Focused Java tests | `./mvnw -Dtest=ArchitectureTest test` | Java 25 |
| Complete Java verification | `./mvnw clean verify -Ppostgres-tests` | Java 25, Docker |
| H2 compatibility | `./mvnw test-compile failsafe:integration-test failsafe:verify -Ph2-compatibility` | Java 25 |
| Mutation testing | `./mvnw test-compile org.pitest:pitest-maven:mutationCoverage` | Java 25 |
| Fuzz regression seeds | `./mvnw test -Pfuzz-tests` | Java 25 |
| Active fuzzing | `JAZZER_FUZZ=1 ./mvnw test -Pfuzz-tests -Dfuzz.max.duration=60s -Dtest=TeamRegistrationFuzzTest` | Java 25 |
| PostgreSQL startup limit | `npm run test:postgres-startup` | Packaged JAR, Java 25, Docker |
| Browser journey | `npm exec -- playwright install chromium` then `npm run test:browser` | Packaged JAR, Java 25, Docker |

The PostgreSQL verification is the acceptance suite. H2 has a separate compatibility suite and is not a replacement for PostgreSQL tests. The core JaCoCo report is `target/site/jacoco/index.html`; the adapter report is `target/site/jacoco-adapters/index.html`. Both final reports are generated after integration tests. All five existing 90% core gates remain enforced; the adapter report makes its separate coverage visible without claiming the entire application meets 90%.

PostgreSQL integration tests use fresh containers with Flyway cleaning disabled. Mutation testing targets core unit tests and enforces a 76% minimum score.

Fuzz regression seeds run on pull requests and main pushes. Weekly active fuzzing runs each of `TeamRegistrationFuzzTest` and `DraftSubmissionFuzzTest` for 60 seconds. CI archives generated corpus, findings and test reports. Reproduce findings locally and commit regression seeds in the corresponding `FuzzTestInputs` resource directory; `.cifuzz-corpus/` remains generated output.

The `postgres` profile caps the shared application and migration pool at two connections per instance, with no minimum idle connections. Clever Cloud runs one instance normally and at most two during deployment, so the application pools can use at most four of the tier's five connections. Other database clients share the remaining capacity. Revisit this budget before increasing instance counts. The startup regression uses a non-superuser PostgreSQL role limited to five connections and starts two applications concurrently; its evidence is under `test-results/postgres-startup/`.

Browser tests create a fresh local PostgreSQL database, seed registration and draft data through the real API, and exercise the existing lobby, admin battle controls, bracket and streamed replay in Chromium. They do not require new product screens. The browser fixture uses the packaged `postgres` configuration. CI reuses the JAR from integration verification, retains failure traces and screenshots, and installs Chromium system dependencies with `--with-deps`.

The pre-commit hook runs Rewrite and Spotless on an isolated copy of the staged files. It leaves the working tree and index intact, including partial staging. If tools change the snapshot, the hook stops and prints a patch path. Review the patch, apply the intended changes, stage them, and retry normally. Never bypass a hook or discard its verified deterministic changes.

API schema metadata belongs to web DTOs. `OpenApiContractIT` compares the full API document with `src/test/resources/contracts/openapi.json`. Change that fixture only for an intentional, reviewed API contract change; include the contract diff in the pull request.

## Publication checks

Deployment runs after the main validation gate for the exact commit. It also rejects a commit that is no longer the current `main`. SonarCloud is provided by the external `sonarqubecloud` GitHub App; the gate checks the successful `SonarCloud Code Analysis` result from that app for the same commit. There is no additional Maven Sonar scan to run locally.

PR dependency review runs without repository secrets. Snyk, OSS Index and OWASP Dependency-Check run on trusted main/scheduled builds. Missing credentials or failed scans must be resolved, not treated as successful validation. Remote branch protection is managed separately; verify exact check names and app identities before changing its required checks.

## Document Size Guidelines

To maintain readability and ease of review, please follow these documentation guidelines:

- **Keep it modular:** Break down large concepts into smaller, logically focused documents (e.g., separate files in the `docs/` directory) rather than maintaining one massive `.md` file.
- **Concise descriptions:** Be clear and to the point.
- **Link resources:** Reference other documents or external standards where applicable, rather than duplicating information.

## CI Guidelines

Ensure that your code is formatted correctly, passes all tests (including property fuzz testing), and does not introduce unresolved `TODO`s or `FIXME`s without good reason (as the GitHub Actions workflow checks for them).
