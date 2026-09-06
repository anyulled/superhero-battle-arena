# Project Testing Patterns

Load this reference when implementing tests. Use Java 25 without preview features, JUnit Jupiter and the project Maven wrapper.

## Unit tests

Domain tests exercise behavior with plain objects. Application tests mock `application.port.out` interfaces; they must not import JPA repositories, entities or web DTOs. Arrange inputs, execute one behavior, then assert its observable result. Separate phases with blank lines; do not add comments explaining what each phase does.

Use [TeamUseCaseTest](../../../../src/test/java/org/barcelonajug/superherobattlearena/application/usecase/TeamUseCaseTest.java) and [HeroSearchCriteriaTest](../../../../src/test/java/org/barcelonajug/superherobattlearena/domain/HeroSearchCriteriaTest.java) as local examples. Preserve the repository's domain constructors and port interfaces instead of inventing alternate service layers.

## MVC tests

For an isolated MVC slice, use current imports:

```java
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
```

Replace a controller's application use-case bean with `@MockitoBean`. Keep security behavior under test. Map incoming web DTOs into application inputs before invoking a use case. Do not disable filters to make failing security tests pass.

For full HTTP integration through MockMvc, use `org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc`. Follow [TournamentHappyPathIT](../../../../src/test/java/org/barcelonajug/superherobattlearena/e2e/TournamentHappyPathIT.java) for the existing project flow and test configuration.

## Persistence integration tests

Use the actual PostgreSQL database through [PostgresTestContainerConfig](../../../../src/test/java/org/barcelonajug/superherobattlearena/testconfig/PostgresTestContainerConfig.java). Keep tests under `adapter.out.persistence`, with `IT` suffixes for Failsafe. Do not mock the database or silently substitute H2 when Docker is unavailable.

Testcontainers 2 uses a non-generic class:

```java
import org.testcontainers.postgresql.PostgreSQLContainer;

static final PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16");
```

The existing shared configuration owns container lifecycle and dynamic datasource properties; reuse it instead of starting an additional unmanaged container. Follow [MatchRepositoryIntegrationIT](../../../../src/test/java/org/barcelonajug/superherobattlearena/adapter/out/persistence/MatchRepositoryIntegrationIT.java) for adapter round-trip assertions.

If a JPA slice is specifically needed, Spring Boot 4 uses `org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest` and `org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase`. Verify the matching test module is declared before adopting a slice.

## Validation and coverage

- Focused unit/architecture tests: `./mvnw -Dtest=ArchitectureTest test` or the relevant test class.
- Full completion check: `./mvnw clean verify -Ppostgres-tests` with Docker available.
- Coverage target: 90%. The POM defines metric thresholds and exclusions; report the measured scope rather than calling it whole-application coverage.

The architecture suite enforces dependency direction. Property tests and fuzz regressions complement examples; they do not replace domain or persistence assertions.

## API references

- [Spring Boot MVC test package](https://docs.spring.io/spring-boot/api/java/org/springframework/boot/webmvc/test/autoconfigure/package-summary.html)
- [Spring MockitoBean support](https://docs.spring.io/spring-framework/reference/testing/annotations/integration-spring/annotation-mockitobean.html)
- [Testcontainers PostgreSQL module](https://java.testcontainers.org/modules/databases/postgres/)
