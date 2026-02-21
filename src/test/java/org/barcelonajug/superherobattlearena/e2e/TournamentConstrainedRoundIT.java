package org.barcelonajug.superherobattlearena.e2e;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.datafaker.Faker;
import org.barcelonajug.superherobattlearena.adapter.in.web.dto.CreateRoundRequest;
import org.barcelonajug.superherobattlearena.domain.json.DraftSubmission;
import org.barcelonajug.superherobattlearena.domain.json.RoundSpec;
import org.barcelonajug.superherobattlearena.testconfig.PostgresTestContainerConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@AutoConfigureMockMvc
class TournamentConstrainedRoundIT extends PostgresTestContainerConfig {

  private static final String ADMIN_USER = "admin";
  private static final String ADMIN_PASS = "test";

  @Autowired private MockMvc mockMvc;

  private final ObjectMapper objectMapper = new ObjectMapper();
  private final Faker faker = new Faker();

  @Test
  void shouldCompleteTournamentWithConstrainedRoundSpec() throws Exception {
    UUID sessionId = createSession();
    assertThat(sessionId).isNotNull();

    UUID teamAId =
        registerTeam(
            faker.esports().team() + " " + UUID.randomUUID(),
            List.of(faker.name().firstName(), faker.name().firstName()),
            sessionId);
    UUID teamBId =
        registerTeam(
            faker.esports().team() + " " + UUID.randomUUID(),
            List.of(faker.name().firstName(), faker.name().firstName()),
            sessionId);
    assertThat(teamAId).isNotNull();
    assertThat(teamBId).isNotNull();

    int roundNo = createConstrainedRound(sessionId);
    assertThat(roundNo).isEqualTo(1);

    // Team A: Agent Bob (10) + Alfred Pennyworth (17) + Bushido (25) = 62 ≤
    // budgetCap 80
    submitSquad(teamAId, roundNo, List.of(10, 17, 144), "BALANCED");
    // Team B: Ben 10 (27) + Captain Cold (24) + Big Daddy (29) = 80 = budgetCap 80
    submitSquad(teamBId, roundNo, List.of(78, 152, 82), "DEFENSIVE");

    verifySubmissionExists(teamAId, roundNo);
    verifySubmissionExists(teamBId, roundNo);

    List<UUID> matchIds = autoMatchTeams(sessionId, roundNo);
    assertThat(matchIds).isNotEmpty();

    JsonNode battleResult = runAllBattles(sessionId, roundNo);
    int totalMatches = battleResult.get("totalMatches").asInt();
    int successfulSimulations = battleResult.get("successfulSimulations").asInt();

    assertThat(totalMatches).isGreaterThanOrEqualTo(1);
    assertThat(successfulSimulations).isEqualTo(totalMatches);

    List<UUID> completedMatchIds = extractMatchIds(battleResult);
    assertThat(completedMatchIds).isNotEmpty();

    for (UUID matchId : completedMatchIds) {
      JsonNode match = getMatch(matchId);
      assertThat(match.get("status").asText()).isEqualTo("COMPLETED");
      assertThat(match.has("winnerTeam")).isTrue();
    }

    JsonNode rounds = getRounds(sessionId);
    boolean foundClosedRound = false;
    for (JsonNode roundNode : rounds) {
      if (roundNode.get("roundNo").asInt() == roundNo) {
        assertThat(roundNode.get("status").asText()).isEqualTo("CLOSED");
        foundClosedRound = true;
      }
    }
    assertThat(foundClosedRound).isTrue();
  }

  // ==================== Helper Methods ====================

  private UUID createSession() throws Exception {
    MvcResult result =
        mockMvc
            .perform(
                post("/api/admin/sessions/start")
                    .with(httpBasic(ADMIN_USER, ADMIN_PASS))
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();

    String body = result.getResponse().getContentAsString();
    return UUID.fromString(body.replace("\"", ""));
  }

  private UUID registerTeam(String name, List<String> members, UUID sessionId) throws Exception {
    String membersParam = String.join(",", members);
    MvcResult result =
        mockMvc
            .perform(
                post("/api/teams/register")
                    .param("name", name)
                    .param("members", membersParam)
                    .param("sessionId", sessionId.toString())
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();

    String body = result.getResponse().getContentAsString();
    return UUID.fromString(body.replace("\"", ""));
  }

  private int createConstrainedRound(UUID sessionId) throws Exception {
    RoundSpec spec =
        new RoundSpec(
            "Constrained Round",
            3,
            80,
            Map.of("Fighter", 1),
            Map.of("Fighter", 3),
            List.of("FLYING"),
            Map.of("MAGIC", 1.2),
            "ARENA_2");

    CreateRoundRequest request = new CreateRoundRequest(sessionId, spec);

    MvcResult result =
        mockMvc
            .perform(
                post("/api/admin/rounds/create")
                    .with(httpBasic(ADMIN_USER, ADMIN_PASS))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andReturn();

    return Integer.parseInt(result.getResponse().getContentAsString());
  }

  private void submitSquad(UUID teamId, int roundNo, List<Integer> heroIds, String strategy)
      throws Exception {
    DraftSubmission submission = new DraftSubmission(heroIds, strategy);

    mockMvc
        .perform(
            post("/api/rounds/{roundNo}/submit", roundNo)
                .param("teamId", teamId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(submission)))
        .andExpect(status().isOk());
  }

  private void verifySubmissionExists(UUID teamId, int roundNo) throws Exception {
    mockMvc
        .perform(
            get("/api/rounds/{roundNo}/submission", roundNo).param("teamId", teamId.toString()))
        .andExpect(status().isOk());
  }

  private List<UUID> autoMatchTeams(UUID sessionId, int roundNo) throws Exception {
    MvcResult result =
        mockMvc
            .perform(
                post("/api/admin/matches/auto-match")
                    .with(httpBasic(ADMIN_USER, ADMIN_PASS))
                    .param("sessionId", sessionId.toString())
                    .param("roundNo", String.valueOf(roundNo))
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();

    JsonNode matchIds = objectMapper.readTree(result.getResponse().getContentAsString());
    return extractUuidsFromArray(matchIds);
  }

  private JsonNode runAllBattles(UUID sessionId, int roundNo) throws Exception {
    MvcResult result =
        mockMvc
            .perform(
                post("/api/admin/matches/run-all")
                    .with(httpBasic(ADMIN_USER, ADMIN_PASS))
                    .param("roundNo", String.valueOf(roundNo))
                    .param("sessionId", sessionId.toString())
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();

    return objectMapper.readTree(result.getResponse().getContentAsString());
  }

  private JsonNode getMatch(UUID matchId) throws Exception {
    MvcResult result =
        mockMvc
            .perform(get("/api/matches/{matchId}", matchId))
            .andExpect(status().isOk())
            .andReturn();

    return objectMapper.readTree(result.getResponse().getContentAsString());
  }

  private List<UUID> extractMatchIds(JsonNode battleResult) {
    JsonNode matchIdsNode = battleResult.get("matchIds");
    return extractUuidsFromArray(matchIdsNode);
  }

  private List<UUID> extractUuidsFromArray(JsonNode arrayNode) {
    if (arrayNode == null || !arrayNode.isArray()) {
      return Collections.emptyList();
    }
    return java.util.stream.StreamSupport.stream(arrayNode.spliterator(), false)
        .map(JsonNode::asText)
        .map(UUID::fromString)
        .toList();
  }

  private JsonNode getRounds(UUID sessionId) throws Exception {
    MvcResult result =
        mockMvc
            .perform(get("/api/rounds").param("sessionId", sessionId.toString()))
            .andExpect(status().isOk())
            .andReturn();

    return objectMapper.readTree(result.getResponse().getContentAsString());
  }
}
