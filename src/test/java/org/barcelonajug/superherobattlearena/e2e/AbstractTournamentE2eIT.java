package org.barcelonajug.superherobattlearena.e2e;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.UUID;
import java.util.stream.StreamSupport;
import org.barcelonajug.superherobattlearena.adapter.in.web.dto.CreateRoundRequest;
import org.barcelonajug.superherobattlearena.adapter.in.web.dto.RoundSpecDto;
import org.barcelonajug.superherobattlearena.domain.json.DraftSubmission;
import org.barcelonajug.superherobattlearena.domain.json.RoundSpec;
import org.barcelonajug.superherobattlearena.testconfig.PostgresTestContainerConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@AutoConfigureMockMvc
abstract class AbstractTournamentE2eIT extends PostgresTestContainerConfig {

  protected static final String ADMIN_USER = "admin";
  protected static final String ADMIN_PASS = "test";

  @Autowired protected MockMvc mockMvc;

  protected final ObjectMapper objectMapper = new ObjectMapper();

  /** Starts a session through the admin API and returns its identifier. */
  protected UUID createSession() throws Exception {
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

  /** Registers a named team and its members in a session and returns its identifier. */
  protected UUID registerTeam(String name, List<String> members, UUID sessionId) throws Exception {
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

  /** Creates a standard test round through the admin API and returns its round number. */
  protected int createRound(UUID sessionId) throws Exception {
    RoundSpec spec =
        new RoundSpec(
            "Test Round",
            5,
            1000,
            emptyMap(),
            emptyMap(),
            emptyList(),
            emptyMap(),
            "ARENA_1",
            emptyList(),
            emptyList(),
            emptyList(),
            emptyList(),
            emptyList());

    CreateRoundRequest request = new CreateRoundRequest(sessionId, RoundSpecDto.from(spec));

    MvcResult result =
        mockMvc
            .perform(
                post("/api/admin/rounds/create")
                    .with(httpBasic(ADMIN_USER, ADMIN_PASS))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andReturn();

    return Integer.parseInt(result.getResponse().getContentAsString().trim());
  }

  /** Submits the specified squad and strategy, asserting that the API accepts them. */
  protected void submitSquad(UUID teamId, int roundNo, List<Integer> heroIds, String strategy)
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

  /** Asserts that the API can retrieve the team submission for the specified round. */
  protected void verifySubmissionExists(UUID teamId, int roundNo) throws Exception {
    mockMvc
        .perform(
            get("/api/rounds/{roundNo}/submission", roundNo).param("teamId", teamId.toString()))
        .andExpect(status().isOk());
  }

  /** Generates round matches through the admin API and returns their identifiers. */
  protected List<UUID> autoMatchTeams(UUID sessionId, int roundNo) throws Exception {
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

  /** Runs pending round battles through the admin API and returns the JSON result. */
  protected JsonNode runAllBattles(UUID sessionId, int roundNo) throws Exception {
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

  /** Fetches a match through the public API, asserting a successful response. */
  protected JsonNode getMatch(UUID matchId) throws Exception {
    MvcResult result =
        mockMvc
            .perform(get("/api/matches/{matchId}", matchId))
            .andExpect(status().isOk())
            .andReturn();

    return objectMapper.readTree(result.getResponse().getContentAsString());
  }

  /** Extracts the match identifier list from a batch battle response. */
  protected List<UUID> extractMatchIds(JsonNode battleResult) {
    JsonNode matchIdsNode = battleResult.get("matchIds");
    return extractUuidsFromArray(matchIdsNode);
  }

  /** Parses a JSON array of UUID strings, returning an empty list for absent or non-array input. */
  protected List<UUID> extractUuidsFromArray(JsonNode arrayNode) {
    if (arrayNode == null || !arrayNode.isArray()) {
      return emptyList();
    }
    return StreamSupport.stream(arrayNode.spliterator(), false)
        .map(JsonNode::asText)
        .map(UUID::fromString)
        .toList();
  }

  /** Fetches the session rounds through the public API, asserting a successful response. */
  protected JsonNode getRounds(UUID sessionId) throws Exception {
    MvcResult result =
        mockMvc
            .perform(get("/api/rounds").param("sessionId", sessionId.toString()))
            .andExpect(status().isOk())
            .andReturn();

    return objectMapper.readTree(result.getResponse().getContentAsString());
  }

  /** Fetches the session round specification, asserting a successful response. */
  protected JsonNode getRoundSpec(UUID sessionId, int roundNo) throws Exception {
    MvcResult result =
        mockMvc
            .perform(get("/api/rounds/{roundNo}", roundNo).param("sessionId", sessionId.toString()))
            .andExpect(status().isOk())
            .andReturn();

    return objectMapper.readTree(result.getResponse().getContentAsString());
  }
}
