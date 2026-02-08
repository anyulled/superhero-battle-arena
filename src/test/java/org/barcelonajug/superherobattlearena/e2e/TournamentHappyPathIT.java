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

/**
 * End-to-end integration test for the complete tournament happy path. Tests the full workflow from
 * session creation to battle completion using PostgreSQL via Testcontainers.
 */
@AutoConfigureMockMvc
class TournamentHappyPathIT extends PostgresTestContainerConfig {

  private static final String ADMIN_USER = "admin";
  private static final String ADMIN_PASS = "test";

  @Autowired private MockMvc mockMvc;

  private final ObjectMapper objectMapper = new ObjectMapper();
  private final Faker faker = new Faker();

  @Test
  void shouldCompleteFullTournamentHappyPath() throws Exception {
    // Step 1: Admin creates a new session
    UUID sessionId = createSession();
    assertThat(sessionId).isNotNull();

    // Step 2: Users register teams
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

    // Step 3: Admin creates round 1 (no constraints)
    int roundNo = createRound(sessionId);
    assertThat(roundNo).isEqualTo(1);

    // Step 4: Teams submit their hero squads (5 heroes each)
    // Using hero IDs that exist in seed data (IDs 2 and 9 are skipped in source)
    submitSquad(teamAId, roundNo, List.of(1, 3, 4, 5, 6), "AGGRESSIVE");
    submitSquad(teamBId, roundNo, List.of(7, 8, 10, 11, 12), "DEFENSIVE");

    // Step 5: Validate submissions exist
    verifySubmissionExists(teamAId, roundNo);
    verifySubmissionExists(teamBId, roundNo);

    // Step 6: Admin auto-matches teams
    List<UUID> matchIds = autoMatchTeams(sessionId, roundNo);
    assertThat(matchIds).isNotEmpty();

    // Step 7: Admin runs all battles
    JsonNode battleResult = runAllBattles(sessionId, roundNo);
    int successfulSimulations = battleResult.get("successfulSimulations").asInt();
    int totalMatches = battleResult.get("totalMatches").asInt();

    // Verify battles were simulated successfully
    assertThat(totalMatches).isGreaterThanOrEqualTo(1);
    assertThat(successfulSimulations).isEqualTo(totalMatches);

    // Step 8: Verify match has a winner
    List<UUID> completedMatchIds = extractMatchIds(battleResult);
    assertThat(completedMatchIds).isNotEmpty();

    for (UUID matchId : completedMatchIds) {
      JsonNode match = getMatch(matchId);
      assertThat(match.get("status").asText()).isEqualTo("COMPLETED");
      assertThat(match.has("winnerTeam")).isTrue();
    }
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

  private int createRound(UUID sessionId) throws Exception {
    RoundSpec spec =
        new RoundSpec(
            "Test Round",
            5,
            1000, // High budget to avoid constraints
            Collections.emptyMap(),
            Collections.emptyMap(),
            Collections.emptyList(),
            Collections.emptyMap(),
            "ARENA_1");

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
}
