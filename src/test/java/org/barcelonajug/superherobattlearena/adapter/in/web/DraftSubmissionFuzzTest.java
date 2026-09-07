package org.barcelonajug.superherobattlearena.adapter.in.web;

import static org.hamcrest.Matchers.lessThan;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.code_intelligence.jazzer.api.FuzzedDataProvider;
import com.code_intelligence.jazzer.junit.FuzzTest;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.barcelonajug.superherobattlearena.application.port.out.TeamRepositoryPort;
import org.barcelonajug.superherobattlearena.application.usecase.AdminUseCase;
import org.barcelonajug.superherobattlearena.application.usecase.SessionUseCase;
import org.barcelonajug.superherobattlearena.domain.Team;
import org.barcelonajug.superherobattlearena.domain.json.RoundSpec;
import org.barcelonajug.superherobattlearena.testconfig.PostgresTestContainerConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
@Tag("fuzz")
class DraftSubmissionFuzzTest extends PostgresTestContainerConfig {

  private static final UUID TEAM_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
  private static final UUID SESSION_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");

  @Autowired private MockMvc mockMvc;
  @Autowired private SessionUseCase sessionUseCase;
  @Autowired private AdminUseCase adminUseCase;
  @Autowired private TeamRepositoryPort teamRepository;

  @BeforeEach
  void arrangeSubmissionContext() {
    sessionUseCase.startSession(SESSION_ID);
    teamRepository.save(
        new Team(
            TEAM_ID,
            SESSION_ID,
            "Fuzz draft team",
            OffsetDateTime.parse("2026-01-01T00:00:00Z"),
            List.of("Captain", "Storm")));
    var spec =
        new RoundSpec(
            "Fuzz draft",
            5,
            200,
            Map.of(),
            Map.of(),
            List.of(),
            Map.of(),
            "NEUTRAL",
            List.of(),
            List.of(),
            List.of(),
            List.of(),
            List.of());
    adminUseCase.createRound(SESSION_ID, spec);
  }

  @FuzzTest
  void fuzzDraftSubmissionEndpoint(FuzzedDataProvider data) throws Exception {
    String body = data.consumeRemainingAsString();

    mockMvc
        .perform(
            post("/api/rounds/1/submit")
                .param("teamId", TEAM_ID.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
        .andExpect(status().is(lessThan(500)));
  }
}
