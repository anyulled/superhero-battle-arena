package org.barcelonajug.superherobattlearena.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.barcelonajug.superherobattlearena.testfixtures.MatchEntityMother.completedMatch;
import static org.barcelonajug.superherobattlearena.testfixtures.MatchEntityMother.pendingMatch;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.barcelonajug.superherobattlearena.adapter.out.persistence.entity.MatchEntity;
import org.barcelonajug.superherobattlearena.adapter.out.persistence.repository.SpringDataMatchRepository;
import org.barcelonajug.superherobattlearena.domain.MatchStatus;
import org.barcelonajug.superherobattlearena.testconfig.PostgresTestContainerConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Integration test verifying repository operations against real PostgreSQL. Tests database queries,
 * constraints, and data persistence using Testcontainers.
 */
class MatchRepositoryIntegrationIT extends PostgresTestContainerConfig {

  @Autowired private SpringDataMatchRepository matchRepository;

  @Test
  void shouldSaveAndRetrieveMatch() {
    // Arrange
    UUID matchId = UUID.randomUUID();
    UUID sessionId = UUID.randomUUID();
    MatchEntity match = pendingMatch(matchId, sessionId, 1);

    // Act
    matchRepository.save(match);
    var found = matchRepository.findById(matchId);

    // Assert
    assertThat(found).isPresent();
    assertThat(found.get().getStatus()).isEqualTo(MatchStatus.PENDING);
    assertThat(found.get().getSessionId()).isEqualTo(sessionId);
  }

  @Test
  void shouldFindPendingMatchesByRoundAndSession() {
    // Arrange
    UUID sessionId = UUID.randomUUID();
    int roundNo = 1;

    // Create 5 pending matches for target session/round
    for (int i = 0; i < 5; i++) {
      matchRepository.save(pendingMatch(sessionId, roundNo));
    }

    // Create noise data (different session)
    for (int i = 0; i < 3; i++) {
      matchRepository.save(pendingMatch(UUID.randomUUID(), roundNo));
    }

    // Act
    List<MatchEntity> pending =
        matchRepository.findByRoundNoAndStatusAndSessionId(roundNo, MatchStatus.PENDING, sessionId);

    // Assert
    assertThat(pending)
        .hasSize(5)
        .allMatch(m -> Objects.requireNonNull(m.getSessionId()).equals(sessionId));
  }

  @Test
  void shouldFilterByStatus() {
    // Arrange
    UUID sessionId = UUID.randomUUID();
    int roundNo = 1;

    // Create 2 pending and 3 completed matches
    for (int i = 0; i < 2; i++) {
      matchRepository.save(pendingMatch(sessionId, roundNo));
    }

    for (int i = 0; i < 3; i++) {
      matchRepository.save(completedMatch(sessionId, roundNo, UUID.randomUUID()));
    }

    // Act
    List<MatchEntity> pending =
        matchRepository.findByRoundNoAndStatusAndSessionId(roundNo, MatchStatus.PENDING, sessionId);
    List<MatchEntity> completed =
        matchRepository.findByRoundNoAndStatusAndSessionId(
            roundNo, MatchStatus.COMPLETED, sessionId);

    // Assert
    assertThat(pending).hasSize(2);
    assertThat(completed).hasSize(3);
  }
}
