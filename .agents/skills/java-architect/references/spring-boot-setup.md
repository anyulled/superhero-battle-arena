# Project Spring Boot Setup

Read this reference for project configuration or package placement. The existing [POM](../../../../pom.xml) is authoritative: Java 25, Spring Boot 4.1.1, Maven wrapper, no preview features. Use the existing mapper infrastructure and dependency configuration. Do not substitute a generic starter POM or introduce another application or persistence stack as part of routine work.

## Package ownership

| Package | Owns | Dependencies |
| --- | --- | --- |
| `domain` | Business behavior and immutable domain values | Pure Java domain code |
| `application.usecase` | Orchestration and application commands | Domain and outbound ports |
| `application.port.out` | Repository interfaces using domain types | Domain |
| `adapter.in.web` | Controllers, web DTOs and web mapping | Use cases and domain |
| `adapter.out.persistence` | JPA entities, Spring Data repositories and port implementations | Outbound ports and domain |
| `config` | Spring wiring and infrastructure configuration | Components being wired |

Controllers translate requests before invoking use cases. Persistence adapters translate JPA entities before returning domain values. Adapters must not access one another. Follow the [architecture ADR](../../../../docs/adr/0002-hexagonal-architecture.md) and [architecture tests](../../../../src/test/java/org/barcelonajug/superherobattlearena/ArchitectureTest.java).

## Build and configuration

Use `./mvnw` from the repository root. Run focused tests while iterating; the completion check is `./mvnw clean verify -Ppostgres-tests` with Docker available. Keep Checkstyle, Spotless, Error Prone, coverage and hooks enabled.

Read the existing `src/main/resources/application*.properties` and `docker-compose.yml` before changing profiles, database settings or public endpoints. Preserve H2 compatibility and PostgreSQL configuration. Ask before changing security, architecture or public contracts.

## Current test APIs

Use the test starters already declared in the POM. Spring Boot 4 separates test support into modules; a generic `spring-boot-starter-test` example does not imply that every slice is available.

- MVC tests: `org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest` and `AutoConfigureMockMvc`.
- Spring bean replacement: `org.springframework.test.context.bean.override.mockito.MockitoBean`.
- PostgreSQL container: `org.testcontainers.postgresql.PostgreSQLContainer`, without a generic parameter, from `testcontainers-postgresql`.
- Plain unit tests: JUnit Jupiter and Mockito, without a Spring context.

See [testing patterns](testing-patterns.md) for project examples. Verify additional APIs against the dependency versions in the POM before introducing them.
