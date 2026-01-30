package org.barcelonajug.superherobattlearena.e2e;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;
import org.barcelonajug.superherobattlearena.application.port.out.MatchRepositoryPort;
import org.barcelonajug.superherobattlearena.application.port.out.RoundRepositoryPort;
import org.barcelonajug.superherobattlearena.application.port.out.SessionRepositoryPort;
import org.barcelonajug.superherobattlearena.domain.Match;
import org.barcelonajug.superherobattlearena.domain.MatchStatus;
import org.barcelonajug.superherobattlearena.domain.Round;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({"h2", "test"})
public class AdminControllerPerformanceIT {

  private static final Logger log = LoggerFactory.getLogger(AdminControllerPerformanceIT.class);
  private static final String ADMIN_USER = "admin";
  private static final String ADMIN_PASS = "test";

  @Autowired private MockMvc mockMvc;
  @Autowired private MatchRepositoryPort matchRepository;
  @Autowired private SessionRepositoryPort sessionRepository;
  @Autowired private RoundRepositoryPort roundRepository;

  @Test
  void measureRunAllBattlesPerformance() throws Exception {
    // 1. Setup
    UUID sessionId = UUID.randomUUID();
    Integer roundNo = 1;

    createRound(roundNo, sessionId);
    generateNoiseData(roundNo, sessionId);
    generateTargetData(roundNo, sessionId);

    // 4. Measure
    long startTime = System.nanoTime();

    mockMvc
        .perform(
            post("/api/admin/matches/run-all")
                .param("roundNo", roundNo.toString())
                .param("sessionId", sessionId.toString())
                .with(httpBasic(ADMIN_USER, ADMIN_PASS)))
        .andExpect(status().isOk());

    long endTime = System.nanoTime();
    long durationMs = (endTime - startTime) / 1_000_000;

    log.info("Execution time for run-all matches: {} ms", durationMs);
    assertThat(durationMs).isLessThan(2000L);
  }

  private void createRound(Integer roundNo, UUID sessionId) {
    Round round = new Round();
    round.setRoundNo(roundNo);
    round.setSessionId(sessionId);
    round.setStatus(org.barcelonajug.superherobattlearena.domain.RoundStatus.OPEN);
    roundRepository.save(round);
  }

  private void generateNoiseData(Integer roundNo, UUID sessionId) {
    log.info("Generating noise data...");
    // 2000 matches that should NOT be selected.
    for (int i = 0; i < 2000; i++) {
      Match match =
          Match.builder()
              .matchId(UUID.randomUUID())
              .sessionId(UUID.randomUUID()) // Random session
              .roundNo(roundNo + 1) // Different round
              .teamA(UUID.randomUUID())
              .teamB(UUID.randomUUID())
              .status(MatchStatus.COMPLETED)
              .build();
      matchRepository.save(match);
    }

    // Add some matches that are in the same round but WRONG status
    for (int i = 0; i < 500; i++) {
      Match match =
          Match.builder()
              .matchId(UUID.randomUUID())
              .sessionId(sessionId)
              .roundNo(roundNo)
              .teamA(UUID.randomUUID())
              .teamB(UUID.randomUUID())
              .status(MatchStatus.COMPLETED)
              .build();
      matchRepository.save(match);
    }
  }

  private void generateTargetData(Integer roundNo, UUID sessionId) {
    log.info("Generating target data...");
    // 20 matches that SHOULD be selected.
    for (int i = 0; i < 20; i++) {
      Match match =
          Match.builder()
              .matchId(UUID.randomUUID())
              .sessionId(sessionId)
              .roundNo(roundNo)
              .teamA(UUID.randomUUID())
              .teamB(UUID.randomUUID())
              .status(MatchStatus.PENDING)
              .build();
      matchRepository.save(match);
    }
  }
}
