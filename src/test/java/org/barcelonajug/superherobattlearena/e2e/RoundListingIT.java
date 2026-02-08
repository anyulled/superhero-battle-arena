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
import org.barcelonajug.superherobattlearena.adapter.in.web.dto.CreateRoundRequest;
import org.barcelonajug.superherobattlearena.domain.json.RoundSpec;
import org.barcelonajug.superherobattlearena.testconfig.PostgresTestContainerConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/** Integration test for listing rounds by session ID. */
@AutoConfigureMockMvc
class RoundListingIT extends PostgresTestContainerConfig {

  private static final String ADMIN_USER = "admin";
  private static final String ADMIN_PASS = "test";

  @Autowired private MockMvc mockMvc;

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Test
  void shouldListRoundsForSession() throws Exception {
    // Create a session
    UUID sessionId = createSession();
    assertThat(sessionId).isNotNull();

    // Initially, no rounds should exist
    List<Integer> rounds = listRounds(sessionId);
    assertThat(rounds).isEmpty();

    // Create round 1
    int round1 = createRound(sessionId);
    assertThat(round1).isEqualTo(1);

    // Create round 2
    int round2 = createRound(sessionId);
    assertThat(round2).isEqualTo(2);

    // List rounds should return both rounds
    rounds = listRounds(sessionId);
    assertThat(rounds).containsExactlyInAnyOrder(1, 2);
  }

  @Test
  void shouldReturnEmptyListForSessionWithNoRounds() throws Exception {
    // Create a session
    UUID sessionId = createSession();

    // List rounds should return empty list
    List<Integer> rounds = listRounds(sessionId);
    assertThat(rounds).isEmpty();
  }

  @Test
  void shouldOnlyReturnRoundsForSpecificSession() throws Exception {
    // Create two sessions
    UUID sessionId1 = createSession();
    UUID sessionId2 = createSession();

    // Create rounds for session 1 (will be 1, 2)
    createRound(sessionId1);
    createRound(sessionId1);

    // Create rounds for session 2 (will be 1)
    createRound(sessionId2);

    // List rounds for session 1 should only return its rounds
    List<Integer> rounds1 = listRounds(sessionId1);
    assertThat(rounds1).containsExactlyInAnyOrder(1, 2);

    // List rounds for session 2 should only return its rounds
    List<Integer> rounds2 = listRounds(sessionId2);
    assertThat(rounds2).containsExactly(1);
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

  private int createRound(UUID sessionId) throws Exception {
    RoundSpec spec =
        new RoundSpec(
            "Test Round",
            5,
            1000,
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

  private List<Integer> listRounds(UUID sessionId) throws Exception {
    MvcResult result =
        mockMvc
            .perform(get("/api/rounds").param("sessionId", sessionId.toString()))
            .andExpect(status().isOk())
            .andReturn();

    JsonNode rounds = objectMapper.readTree(result.getResponse().getContentAsString());
    return java.util.stream.StreamSupport.stream(rounds.spliterator(), false)
        .map(r -> r.get("roundNo").asInt())
        .toList();
  }
}
