# Project skills

`.agents/skills` is the canonical source. `.agent/skills` contains compatibility symlinks to the same directories; edit the canonical files once. Skill persona metadata is descriptive and does not define runnable subagents.

## Routing

| Work | Start with | Add only when needed |
| --- | --- | --- |
| Java domain or application behavior | `java-pro` | Relevant SOLID, KISS, DRY, Demeter or responsibility skill |
| Package boundaries and architectural changes | `java-architect` | `jpa-patterns` for persistence mapping |
| Spring controllers, wiring and configuration | `java-spring-boot` | `springboot-patterns` |
| Java tests | `java-testing`, `aaa-pattern` | `springboot-tdd` when developing test first |
| Persistence queries and transactions | `jpa-patterns` | `postgres-patterns` for SQL |
| Requested Spring security work | `springboot-security` | `java-spring-boot` |
| JavaScript/TypeScript or Node tooling | `coding-standards` | `backend-patterns` only for applicable Node work |
| Skill authoring | `skill-creator` | Existing skill references as needed |

Load the smallest relevant set. Java tasks should not inherit Express, Next.js, Supabase or React implementation patterns simply because a generic backend skill is installed.

## Compatibility and precedence

[AGENTS.md](../AGENTS.md), the [POM](../pom.xml) and accepted [ADRs](adr/) define this project. Skills provide supporting techniques and do not authorize changing its architecture or tooling.

- Use Java 25 without preview features and Spring Boot 4.1.1. Do not introduce preview-only structured concurrency APIs.
- Use `./mvnw`; preserve lint rules and git hook commands. Ask for instructions when a required check cannot run.
- Keep domain behavior independent of Spring, persistence and web APIs. Outbound ports belong in `application.port.out`; entities stay in the persistence adapter and web DTOs stay in the web adapter.
- Unit tests use JUnit Jupiter and Mockito with separate arrange, act and assert phases. Use blank lines instead of WHAT comments.
- Database integration tests use PostgreSQL Testcontainers. H2 compatibility is a separate concern, not an automatic database substitute.
- The coverage target is 90%; the POM defines the enforced counters and exclusions. Do not copy older 80% examples.
- Use `@MockitoBean` for Spring bean replacement and Testcontainers 2's `org.testcontainers.postgresql.PostgreSQLContainer` without generic parameters. Boot 4 MVC test annotations live in `org.springframework.boot.webmvc.test.autoconfigure`.

Read the [project setup reference](../.agents/skills/java-architect/references/spring-boot-setup.md) and [testing reference](../.agents/skills/java-architect/references/testing-patterns.md) for concrete repository paths. Domain or application code must not adopt a generic example that exposes a JPA entity or passes a web request directly into a use case.
