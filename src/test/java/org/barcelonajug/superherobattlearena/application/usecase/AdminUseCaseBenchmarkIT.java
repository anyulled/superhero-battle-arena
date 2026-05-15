package org.barcelonajug.superherobattlearena.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.barcelonajug.superherobattlearena.application.port.out.MatchRepositoryPort;
import org.barcelonajug.superherobattlearena.domain.Match;
import org.barcelonajug.superherobattlearena.domain.MatchStatus;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles({"h2", "test"})
@Transactional
class AdminUseCaseBenchmarkIT {

  private static final Logger log = LoggerFactory.getLogger(AdminUseCaseBenchmarkIT.class);

  @Autowired private MatchRepositoryPort matchRepository;

  @Test
  void benchmarkFindPendingMatches() {
    UUID targetSessionId = UUID.randomUUID();
    int targetRoundNo = 1;

    int totalMatches = 5000;
    int targetPendingMatches = 50;

    List<Match> events = new ArrayList<>();

    // Insert filler matches
    for (int i = 0; i < totalMatches; i++) {
      Match m = Match.builder()
        .matchId(UUID.randomUUID())
        .sessionId(UUID.randomUUID())
        .roundNo(targetRoundNo)
        .status(MatchStatus.COMPLETED)
        .teamA(UUID.randomUUID())
        .teamB(UUID.randomUUID())
        .build();
      events.add(m);
    }

    // Insert target matches
    for (int i = 0; i < targetPendingMatches; i++) {
      Match m = Match.builder()
          .matchId(UUID.randomUUID())
          .sessionId(targetSessionId)
          .roundNo(targetRoundNo)
          .status(MatchStatus.PENDING)
          .teamA(UUID.randomUUID())
          .teamB(UUID.randomUUID())
          .build();
      events.add(m);
    }

    matchRepository.saveAll(events);

    // Warmup
    matchRepository.findAll();

    // Benchmark Old Approach
    long startOld = System.nanoTime();
    List<Match> oldApproach = matchRepository.findAll().stream()
        .filter(m -> m.getRoundNo().equals(targetRoundNo))
        .filter(m -> m.getStatus() == MatchStatus.PENDING)
        .filter(m -> targetSessionId.equals(m.getSessionId()))
        .toList();
    long endOld = System.nanoTime();
    long durationOld = (endOld - startOld) / 1_000_000; // ms

    // Benchmark New Approach
    long startNew = System.nanoTime();
    List<Match> newApproach = matchRepository.findPendingMatches(targetRoundNo, targetSessionId);
    long endNew = System.nanoTime();
    long durationNew = (endNew - startNew) / 1_000_000; // ms

    log.info("Benchmark - Old in-memory filtering took {} ms", durationOld);
    log.info("Benchmark - New database filtering took {} ms", durationNew);

    assertThat(oldApproach).hasSize(targetPendingMatches);
    assertThat(newApproach).hasSize(targetPendingMatches);
  }
}
