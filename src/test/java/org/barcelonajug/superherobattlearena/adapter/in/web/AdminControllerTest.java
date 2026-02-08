package org.barcelonajug.superherobattlearena.adapter.in.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.barcelonajug.superherobattlearena.adapter.in.web.dto.CreateRoundRequest;
import org.barcelonajug.superherobattlearena.application.usecase.AdminUseCase;
import org.barcelonajug.superherobattlearena.domain.Session;
import org.barcelonajug.superherobattlearena.domain.json.RoundSpec;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AdminController.class)
class AdminControllerTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @MockitoBean private AdminUseCase adminUseCase;

  private static final String ADMIN_USER = "admin";
  private static final String ADMIN_PASS = "1234";

  @Test
  void startSession_shouldReturnSessionId_whenSuccessful() throws Exception {
    // Given
    UUID sessionId = UUID.randomUUID();
    when(adminUseCase.startSession(null)).thenReturn(sessionId);

    // When/Then
    mockMvc
        .perform(
            post("/api/admin/sessions/start")
                .with(httpBasic(ADMIN_USER, ADMIN_PASS))
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").value(sessionId.toString()));
  }

  @Test
  void startSession_shouldAcceptCustomSessionId() throws Exception {
    // Given
    UUID customSessionId = UUID.randomUUID();
    when(adminUseCase.startSession(customSessionId)).thenReturn(customSessionId);

    // When/Then
    mockMvc
        .perform(
            post("/api/admin/sessions/start")
                .with(httpBasic(ADMIN_USER, ADMIN_PASS))
                .param("sessionId", customSessionId.toString())
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").value(customSessionId.toString()));
  }

  @Test
  void startSession_shouldRequireAuthentication() throws Exception {
    // When/Then: No authentication provided
    mockMvc
        .perform(post("/api/admin/sessions/start").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void listSessions_shouldReturnAllSessions() throws Exception {
    // Given
    UUID sessionId1 = UUID.randomUUID();
    UUID sessionId2 = UUID.randomUUID();
    List<Session> sessions =
        List.of(
            new Session(sessionId1, OffsetDateTime.now(), true),
            new Session(sessionId2, OffsetDateTime.now(), false));
    when(adminUseCase.listSessions()).thenReturn(sessions);

    // When/Then
    mockMvc
        .perform(get("/api/admin/sessions").with(httpBasic(ADMIN_USER, ADMIN_PASS)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[0].sessionId").value(sessionId1.toString()))
        .andExpect(jsonPath("$[1].sessionId").value(sessionId2.toString()));
  }

  @Test
  void listSessions_shouldRequireAuthentication() throws Exception {
    // When/Then: No authentication provided
    mockMvc.perform(get("/api/admin/sessions")).andExpect(status().isUnauthorized());
  }

  @Test
  void createRound_shouldReturnRoundNumber_whenSuccessful() throws Exception {
    // Given
    UUID sessionId = UUID.randomUUID();
    Integer roundNo = 1;
    RoundSpec spec =
        new RoundSpec(
            "Test Round",
            5,
            100,
            Collections.emptyMap(),
            Collections.emptyMap(),
            Collections.emptyList(),
            Collections.emptyMap(),
            "ARENA_1");
    CreateRoundRequest request = new CreateRoundRequest(sessionId, roundNo, spec);

    when(adminUseCase.createRound(sessionId, roundNo, spec)).thenReturn(roundNo);

    // When/Then
    mockMvc
        .perform(
            post("/api/admin/rounds/create")
                .with(httpBasic(ADMIN_USER, ADMIN_PASS))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").value(roundNo));
  }

  @Test
  void createRound_shouldRequireAuthentication() throws Exception {
    // Given
    UUID sessionId = UUID.randomUUID();
    Integer roundNo = 1;
    RoundSpec spec =
        new RoundSpec(
            "Test Round",
            5,
            100,
            Collections.emptyMap(),
            Collections.emptyMap(),
            Collections.emptyList(),
            Collections.emptyMap(),
            "ARENA_1");
    CreateRoundRequest request = new CreateRoundRequest(sessionId, roundNo, spec);

    // When/Then: No authentication provided
    mockMvc
        .perform(
            post("/api/admin/rounds/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void autoMatch_shouldReturnMatchIds_whenSuccessful() throws Exception {
    // Given
    UUID sessionId = UUID.randomUUID();
    Integer roundNo = 1;
    List<UUID> matchIds = List.of(UUID.randomUUID(), UUID.randomUUID());

    when(adminUseCase.autoMatch(sessionId, roundNo)).thenReturn(matchIds);

    // When/Then
    mockMvc
        .perform(
            post("/api/admin/matches/auto-match")
                .with(httpBasic(ADMIN_USER, ADMIN_PASS))
                .param("sessionId", sessionId.toString())
                .param("roundNo", String.valueOf(roundNo))
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(2));
  }

  @Test
  void autoMatch_shouldRequireAuthentication() throws Exception {
    // When/Then: No authentication provided
    mockMvc
        .perform(
            post("/api/admin/matches/auto-match")
                .param("sessionId", UUID.randomUUID().toString())
                .param("roundNo", "1")
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void createMatch_shouldReturnMatchId_whenSuccessful() throws Exception {
    // Given
    UUID teamA = UUID.randomUUID();
    UUID teamB = UUID.randomUUID();
    Integer roundNo = 1;
    UUID sessionId = UUID.randomUUID();
    UUID matchId = UUID.randomUUID();

    when(adminUseCase.createMatch(teamA, teamB, roundNo, sessionId)).thenReturn(matchId);

    // When/Then
    mockMvc
        .perform(
            post("/api/admin/matches/create")
                .with(httpBasic(ADMIN_USER, ADMIN_PASS))
                .param("teamA", teamA.toString())
                .param("teamB", teamB.toString())
                .param("roundNo", String.valueOf(roundNo))
                .param("sessionId", sessionId.toString())
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").value(matchId.toString()));
  }

  @Test
  void createMatch_shouldUseDefaultRoundNo_whenNotProvided() throws Exception {
    // Given
    UUID teamA = UUID.randomUUID();
    UUID teamB = UUID.randomUUID();
    UUID matchId = UUID.randomUUID();

    when(adminUseCase.createMatch(eq(teamA), eq(teamB), eq(1), any())).thenReturn(matchId);

    // When/Then
    mockMvc
        .perform(
            post("/api/admin/matches/create")
                .with(httpBasic(ADMIN_USER, ADMIN_PASS))
                .param("teamA", teamA.toString())
                .param("teamB", teamB.toString())
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").value(matchId.toString()));
  }

  @Test
  void runMatch_shouldReturnWinner_whenSuccessful() throws Exception {
    // Given
    UUID matchId = UUID.randomUUID();
    UUID winnerId = UUID.randomUUID();

    when(adminUseCase.runMatch(matchId)).thenReturn(winnerId.toString());

    // When/Then
    mockMvc
        .perform(
            post("/api/admin/matches/{matchId}/run", matchId)
                .with(httpBasic(ADMIN_USER, ADMIN_PASS))
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").value("Match completed. Winner: " + winnerId));
  }

  @Test
  void runAllBattles_shouldReturnBatchResult_whenSuccessful() throws Exception {
    // Given
    Integer roundNo = 1;
    UUID sessionId = UUID.randomUUID();

    Map<String, Object> result = new HashMap<>();
    result.put("matchIds", List.of(UUID.randomUUID(), UUID.randomUUID()));
    result.put("winners", Map.of(UUID.randomUUID(), UUID.randomUUID()));
    result.put("total", 2);
    result.put("successCount", 2);

    when(adminUseCase.runAllBattles(roundNo, sessionId)).thenReturn(result);

    // When/Then
    mockMvc
        .perform(
            post("/api/admin/matches/run-all")
                .with(httpBasic(ADMIN_USER, ADMIN_PASS))
                .param("roundNo", String.valueOf(roundNo))
                .param("sessionId", sessionId.toString())
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.matchIds").isArray())
        .andExpect(jsonPath("$.matchIds.length()").value(2))
        .andExpect(jsonPath("$.total").value(2))
        .andExpect(jsonPath("$.successfulSimulations").value(2));
  }

  @Test
  void runAllBattles_shouldWorkWithoutSessionId() throws Exception {
    // Given
    Integer roundNo = 1;

    Map<String, Object> result = new HashMap<>();
    result.put("matchIds", List.of(UUID.randomUUID()));
    result.put("winners", Map.of(UUID.randomUUID(), UUID.randomUUID()));
    result.put("total", 1);
    result.put("successCount", 1);

    when(adminUseCase.runAllBattles(eq(roundNo), eq(null))).thenReturn(result);

    // When/Then
    mockMvc
        .perform(
            post("/api/admin/matches/run-all")
                .with(httpBasic(ADMIN_USER, ADMIN_PASS))
                .param("roundNo", String.valueOf(roundNo))
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.total").value(1));
  }

  @Test
  void runAllBattles_shouldRequireAuthentication() throws Exception {
    // When/Then: No authentication provided
    mockMvc
        .perform(
            post("/api/admin/matches/run-all")
                .param("roundNo", "1")
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized());
  }
}