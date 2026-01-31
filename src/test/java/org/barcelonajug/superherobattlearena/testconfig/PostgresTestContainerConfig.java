package org.barcelonajug.superherobattlearena.testconfig;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Shared PostgreSQL container configuration for integration tests. Extend this class to use a real
 * PostgreSQL database in tests.
 *
 * <p>The container uses PostgreSQL 16 (matching production docker-compose.yml) and is configured
 * with container reuse enabled for faster test execution.
 */
@Testcontainers
public abstract class PostgresTestContainerConfig {

  @Container @ServiceConnection
  static final PostgreSQLContainer<?> postgres =
      new PostgreSQLContainer<>("postgres:16")
          .withDatabaseName("superhero_db")
          .withUsername("super_user")
          .withPassword("super_password")
          .withReuse(true);

  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
    // Use PostgreSQL-specific Flyway migrations
    registry.add(
        "spring.flyway.locations",
        () -> "classpath:db/migration/common,classpath:db/migration/postgresql");
  }
}
