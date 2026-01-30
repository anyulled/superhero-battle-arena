package org.barcelonajug.superherobattlearena.e2e;

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

    // Persist Session (needed for controller validation if any, though run-all checks filtering)
    // Actually AdminController checks session validation in other methods but run-all takes
    // optional sessionId.
    // However, if we pass sessionId, let's make sure it exists just in case.
    // SessionRepositoryPort interface is likely simple.
    // Let's assume we can save a dummy session.
    // Checking SessionRepositoryPort interface:
    // It's not fully visible but based on usage in AdminController:
    // sessionRepository.save(session);
    // Session constructor: public Session(UUID sessionId, OffsetDateTime created, boolean active)

    // We'll skip session persistence for now unless it fails, as the filter logic in controller
    // doesn't seem to validate session existence for run-all.
    // Wait, the run-all logic:
    /*
     Round round = roundRepository.findById(roundNo)
            .orElseThrow(() -> new IllegalArgumentException("Round not found: " + roundNo));
    */
    // So we MUST persist a round.
    Round round = new Round();
    round.setRoundNo(roundNo);
    round.setSessionId(sessionId);
    round.setStatus(org.barcelonajug.superherobattlearena.domain.RoundStatus.OPEN);
    // SpecJson is needed if simulation runs, but we plan to fail fast or just measure filtering.
    // Ideally we want to avoid simulation crash.
    // But simulation happens INSIDE the loop over pending matches.
    // If we have pending matches, simulation is attempted.
    // If we provide matches with random team IDs and no submissions, `buildBattleTeam` or
    // `submissionRepository.findByTeamIdAndRoundNo` will fail or return empty.
    // Code:
    /*
        Optional<Submission> subA = submissionRepository.findByTeamIdAndRoundNo(match.getTeamA(), match.getRoundNo());
        if (subA.isEmpty() || subB.isEmpty()) { continue; }
    */
    // Perfect. If no submission, it continues. This is the fastest path and measures loop overhead
    // + fetching overhead.

    roundRepository.save(round);

    // 2. Create Noise Data
    // 2000 matches that should NOT be selected.
    // Mix of:
    // - Different Round No
    // - Different Session ID (if filtering by session)
    // - Status != PENDING
    log.info("Generating noise data...");
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

    // 3. Create Target Data
    // 20 matches that SHOULD be selected.
    log.info("Generating target data...");
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
  }
}
