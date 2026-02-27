package org.barcelonajug.superherobattlearena.testconfig;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Shared PostgreSQL container configuration for integration tests. Extend this class to use a real
 * PostgreSQL database in tests.
 *
 * <p>The container uses PostgreSQL 16 (matching production docker-compose.yml) and is configured
 * with container reuse enabled for faster test execution.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"postgres-test", "test"})
@Transactional
public abstract class PostgresTestContainerConfig {

  @SuppressWarnings("resource")
  static final PostgreSQLContainer<?> postgres =
      new PostgreSQLContainer<>("postgres:16")
          .withDatabaseName("superhero_db")
          .withUsername("super_user")
          .withPassword("super_password")
          .withReuse(true);

  static {
    postgres.start();
  }

  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
    // Use PostgreSQL-specific Flyway migrations
    registry.add(
        "spring.flyway.locations",
        () -> "classpath:db/migration/common,classpath:db/migration/postgresql");
    // Fix checksum mismatch by cleaning on error
    registry.add("spring.flyway.clean-on-validation-error", () -> "true");
    registry.add("spring.flyway.clean-disabled", () -> "false");
  }
}
