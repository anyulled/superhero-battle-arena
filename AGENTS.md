# Agent Instructions

This project adheres to strict architectural and coding guidelines. As an AI assistant, you must continuously adhere to the following constraints when generating, refactoring, or reviewing code.

## 1. Project Context & Stack

- **Language:** Java 21+ (Java 25 with preview features recommended).
- **Framework:** Spring Boot 3.x.
- **Pattern:** Hexagonal Architecture (Ports and Adapters).
- **Build Tool:** Maven (`./mvnw`).
- **Persistence:** PostgreSQL (via Testcontainers for tests, managed via docker-compose locally) & H2 memory fallback.

## 2. Zero-Tolerance Rules

- **DO NOT** disable ESLint or Checkstyle/Linting rules.
- **DO NOT** remove or comment out git hook commands.
- **NO INLINE COMMENTS**, doc comments, or line comments explaining WHAT the code does.
- Code MUST be self-documenting through clear variable and function names.
- **ONLY** write comments to explain WHY a non-obvious decision was made.

## 3. Architecture Constraints (Hexagonal)

- **`domain` package:**
  - Pure Java. Zero Spring, JPA, or web annotations.
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
- Always check the `/workflows` or `/skills` reference documentation if you're uncertain about a particular architectural approach.
