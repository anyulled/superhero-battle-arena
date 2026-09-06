# Agent Instructions

This project adheres to strict architectural and coding guidelines. As an AI assistant, you must continuously adhere to the following constraints when generating, refactoring, or reviewing code.

## 1. Project Context & Stack

- **Language:** Java 25, without preview features.
- **Framework:** Spring Boot 4.1.1 (the Maven parent in `pom.xml` is authoritative).
- **Pattern:** Hexagonal Architecture (Ports and Adapters).
- **Build Tool:** Maven (`./mvnw`).
- **Persistence:** PostgreSQL (via Testcontainers for tests, managed via docker-compose locally) & H2 memory fallback.

## 2. Zero-Tolerance Rules

- **DO NOT** disable Checkstyle/Linting rules.
- **DO NOT** remove or comment out git hook commands.
- **NO INLINE COMMENTS**, doc comments, or line comments explaining WHAT the code does.
- Code MUST be self-documenting through clear variable and function names.
- **ONLY** write comments to explain WHY a non-obvious decision was made.

## 3. Architecture Constraints (Hexagonal)

- **`domain` package:**
  - Pure Java business behavior; JSpecify nullness annotations and SLF4J logging are the only allowed external dependencies.
  - Zero Spring, JPA, Jackson, or web annotations. Swagger metadata belongs to web-adapter DTOs.
  - Rich domain models, state encapsulation.
- **`application` package:**
  - Use cases that orchestrate logic.
  - Defines `port.out` (interfaces for repositories).
  - Can be annotated with `@Service`.
- **`adapter` package:**
  - `in.web` handles HTTP/REST and maps to domain/usecases.
  - `out.persistence` handles DB access mapping JPA Entities back and forth to Domain models.
  - DO NOT leak DB entities (`@Entity`) out of the adapter package.
  - DO NOT leak Web DTOs into the application layer.

## 4. Coding Principles

- Apply SOLID, DRY, KISS, and YAGNI.
- **Law of Demeter:** A method should only call methods on itself, its parameters, objects it creates, or its direct properties. Avoid chains like `a.getB().getC()`.
- **Tell, Don't Ask:** Push behavior into the object that owns the data. Do not ask an object for data to make decisions outside of it.
- **Boy Scout Rule:** Leave code cleaner than you found it.

## 5. Testing Requirements

- **Coverage:** Aim for 90% test coverage.
- **Unit Tests:**
  - Written for domain and application layers using `JUnit 5` and `Mockito`.
  - Follow the **AAA pattern** (Arrange, Act, Assert).
  - Keep tests fast and isolated (no DB, no network).
- **Integration Tests:**
  - Written for adapters layer.
  - Test the actual Database using `Testcontainers`.
  - Do not mock the database for repository integration tests.

## 6. General Guidelines

- Prefer Java `record` for immutable data objects (DTOs, some domain objects).
- When resolving tasks, check the build (`./mvnw clean verify`), verify tests pass, and resolve linting/SonarQube issues before declaring a task done.
- Consult [ARCHITECTURE.md](ARCHITECTURE.md), [architecture decisions](docs/adr/), and the [skill routing index](docs/skills.md) when uncertain about an architectural approach. CI definitions live in `.github/workflows/`.
- Preserve deterministic file edits made by verified git hooks; do not drop them as noise.
- **DO NOT** execute code or file modifications autonomously without first proposing the options/actions to the user and asking which action to take.
