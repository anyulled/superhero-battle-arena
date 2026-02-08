package org.barcelonajug.superherobattlearena.adapter.in.web;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.barcelonajug.superherobattlearena.application.usecase.RoundUseCase;
import org.barcelonajug.superherobattlearena.domain.Round;
import org.barcelonajug.superherobattlearena.domain.RoundStatus;
import org.barcelonajug.superherobattlearena.domain.json.DraftSubmission;
import org.barcelonajug.superherobattlearena.domain.json.RoundSpec;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(RoundController.class)
class RoundControllerTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @MockitoBean private RoundUseCase roundUseCase;

  @Test
  void listRounds_shouldReturnAllRoundsForSession() throws Exception {
    // Given
    UUID sessionId = UUID.randomUUID();

    Round round1 = new Round();
    round1.setRoundNo(1);
    round1.setSessionId(sessionId);
    round1.setStatus(RoundStatus.OPEN);

    Round round2 = new Round();
    round2.setRoundNo(2);
    round2.setSessionId(sessionId);
    round2.setStatus(RoundStatus.CLOSED);

    when(roundUseCase.listRounds(sessionId)).thenReturn(List.of(round1, round2));

    // When/Then
    mockMvc
        .perform(get("/api/rounds").param("sessionId", sessionId.toString()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[0].roundNo").value(1))
        .andExpect(jsonPath("$[1].roundNo").value(2));
  }

  @Test
  void listRounds_shouldReturnEmptyList_whenNoRoundsExist() throws Exception {
    // Given
    UUID sessionId = UUID.randomUUID();
    when(roundUseCase.listRounds(sessionId)).thenReturn(Collections.emptyList());

    // When/Then
    mockMvc
        .perform(get("/api/rounds").param("sessionId", sessionId.toString()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(0));
  }

  @Test
  void getRound_shouldReturnRoundSpec_whenRoundExists() throws Exception {
    // Given
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

    when(roundUseCase.getRoundSpec(roundNo)).thenReturn(Optional.of(spec));

    // When/Then
    mockMvc
        .perform(get("/api/rounds/{roundNo}", roundNo))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.description").value("Test Round"))
        .andExpect(jsonPath("$.teamSize").value(5))
        .andExpect(jsonPath("$.budgetCap").value(100))
        .andExpect(jsonPath("$.mapType").value("ARENA_1"));
  }

  @Test
  void getRound_shouldReturn404_whenRoundDoesNotExist() throws Exception {
    // Given
    Integer roundNo = 1;
    when(roundUseCase.getRoundSpec(roundNo)).thenReturn(Optional.empty());

    // When/Then
    mockMvc.perform(get("/api/rounds/{roundNo}", roundNo)).andExpect(status().isNotFound());
  }

  @Test
  void submitTeam_shouldReturn200_whenSubmissionIsSuccessful() throws Exception {
    // Given
    Integer roundNo = 1;
    UUID teamId = UUID.randomUUID();
    DraftSubmission submission = new DraftSubmission(List.of(1, 2, 3, 4, 5), "Aggressive");

    doNothing().when(roundUseCase).submitTeam(roundNo, teamId, submission);

    // When/Then
    mockMvc
        .perform(
            post("/api/rounds/{roundNo}/submit", roundNo)
                .param("teamId", teamId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(submission)))
        .andExpect(status().isOk());
  }

  @Test
  void submitTeam_shouldReturn400_whenInvalidSubmissionData() throws Exception {
    // Given
    Integer roundNo = 1;
    UUID teamId = UUID.randomUUID();
    DraftSubmission invalidSubmission = new DraftSubmission(List.of(1, 2, 3), "Too few heroes");

    when(roundUseCase.submitTeam(roundNo, teamId, invalidSubmission))
        .thenThrow(new IllegalArgumentException("Team must have exactly 5 heroes"));

    // When/Then
    mockMvc
        .perform(
            post("/api/rounds/{roundNo}/submit", roundNo)
                .param("teamId", teamId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidSubmission)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void getSubmission_shouldReturnDraftSubmission_whenSubmissionExists() throws Exception {
    // Given
    Integer roundNo = 1;
    UUID teamId = UUID.randomUUID();
    DraftSubmission submission = new DraftSubmission(List.of(1, 2, 3, 4, 5), "Aggressive");

    when(roundUseCase.getSubmission(roundNo, teamId)).thenReturn(Optional.of(submission));

    // When/Then
    mockMvc
        .perform(
            get("/api/rounds/{roundNo}/submission", roundNo).param("teamId", teamId.toString()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.heroIds").isArray())
        .andExpect(jsonPath("$.heroIds.length()").value(5))
        .andExpect(jsonPath("$.strategy").value("Aggressive"));
  }

  @Test
  void getSubmission_shouldReturn404_whenSubmissionDoesNotExist() throws Exception {
    // Given
    Integer roundNo = 1;
    UUID teamId = UUID.randomUUID();

    when(roundUseCase.getSubmission(roundNo, teamId)).thenReturn(Optional.empty());

    // When/Then
    mockMvc
        .perform(
            get("/api/rounds/{roundNo}/submission", roundNo).param("teamId", teamId.toString()))
        .andExpect(status().isNotFound());
  }

  @Test
  void getSubmissions_shouldReturnAllSubmissionsForRound() throws Exception {
    // Given
    Integer roundNo = 1;
    UUID sessionId = UUID.randomUUID();

    DraftSubmission submission1 = new DraftSubmission(List.of(1, 2, 3, 4, 5), "Strategy 1");
    DraftSubmission submission2 = new DraftSubmission(List.of(6, 7, 8, 9, 10), "Strategy 2");

    when(roundUseCase.getSubmissions(roundNo, sessionId))
        .thenReturn(List.of(submission1, submission2));

    // When/Then
    mockMvc
        .perform(
            get("/api/rounds/{roundNo}/submissions", roundNo)
                .param("sessionId", sessionId.toString()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[0].strategy").value("Strategy 1"))
        .andExpect(jsonPath("$[1].strategy").value("Strategy 2"));
  }

  @Test
  void getSubmissions_shouldWorkWithoutSessionId() throws Exception {
    // Given
    Integer roundNo = 1;

    DraftSubmission submission1 = new DraftSubmission(List.of(1, 2, 3, 4, 5), "Strategy 1");

    when(roundUseCase.getSubmissions(roundNo, null)).thenReturn(List.of(submission1));

    // When/Then
    mockMvc
        .perform(get("/api/rounds/{roundNo}/submissions", roundNo))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(1));
  }

  @Test
  void getSubmissions_shouldReturnEmptyList_whenNoSubmissionsExist() throws Exception {
    // Given
    Integer roundNo = 1;
    UUID sessionId = UUID.randomUUID();

    when(roundUseCase.getSubmissions(roundNo, sessionId)).thenReturn(Collections.emptyList());

    // When/Then
    mockMvc
        .perform(
            get("/api/rounds/{roundNo}/submissions", roundNo)
                .param("sessionId", sessionId.toString()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(0));
  }

  @Test
  void submitTeam_shouldHandleComplexRoundSpec() throws Exception {
    // Given
    Integer roundNo = 1;
    UUID teamId = UUID.randomUUID();
    DraftSubmission submission =
        new DraftSubmission(
            List.of(1, 2, 3, 4, 5), "Complex strategy with multiple hero synergies");

    doNothing().when(roundUseCase).submitTeam(roundNo, teamId, submission);

    // When/Then
    mockMvc
        .perform(
            post("/api/rounds/{roundNo}/submit", roundNo)
                .param("teamId", teamId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(submission)))
        .andExpect(status().isOk());
  }

  @Test
  void getRound_shouldReturnRoundWithComplexSpec() throws Exception {
    // Given
    Integer roundNo = 1;
    RoundSpec spec =
        new RoundSpec(
            "Complex Round",
            5,
            150,
            Map.of("TANK", 1, "HEALER", 1),
            Map.of("DPS", 3),
            List.of("FLYING", "TECH"),
            Map.of("MAGIC", 1.2, "TECH", 0.8),
            "ARENA_2");

    when(roundUseCase.getRoundSpec(roundNo)).thenReturn(Optional.of(spec));

    // When/Then
    mockMvc
        .perform(get("/api/rounds/{roundNo}", roundNo))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.description").value("Complex Round"))
        .andExpect(jsonPath("$.teamSize").value(5))
        .andExpect(jsonPath("$.budgetCap").value(150))
        .andExpect(jsonPath("$.requiredRoles.TANK").value(1))
        .andExpect(jsonPath("$.maxSameRole.DPS").value(3))
        .andExpect(jsonPath("$.bannedTags").isArray())
        .andExpect(jsonPath("$.tagModifiers.MAGIC").value(1.2));
  }
}