package org.barcelonajug.superherobattlearena.testconfig;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.barcelonajug.superherobattlearena.SuperheroBattleArenaApplication;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.postgresql.PostgreSQLContainer;

@SpringBootTest(
    classes = SuperheroBattleArenaApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"postgres-test", "test"})
@Transactional
public abstract class PostgresTestContainerConfig {

  @Autowired private DataSource dataSource;

  @BeforeEach
  void requirePostgreSqlDatabase() throws SQLException {
    try (Connection connection = dataSource.getConnection()) {
      DatabaseMetaData metadata = connection.getMetaData();

      String databaseProductName = metadata.getDatabaseProductName();

      assertThat(databaseProductName).isEqualTo("PostgreSQL");
    }
  }

  @SuppressWarnings("resource")
  static final PostgreSQLContainer postgres =
      new PostgreSQLContainer("postgres:16")
          .withDatabaseName("superhero_db")
          .withUsername("super_user")
          .withPassword("super_password");

  static {
    postgres.start();
  }

  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
    registry.add(
        "spring.flyway.locations",
        () -> "classpath:db/migration/common,classpath:db/migration/postgresql");
    registry.add("spring.flyway.clean-disabled", () -> "true");
  }
}
