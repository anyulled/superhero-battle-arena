package org.barcelonajug.superherobattlearena.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.barcelonajug.superherobattlearena.application.port.out.RoundRepositoryPort;
import org.barcelonajug.superherobattlearena.application.port.out.SubmissionRepositoryPort;
import org.barcelonajug.superherobattlearena.domain.Round;
import org.barcelonajug.superherobattlearena.domain.RoundStatus;
import org.barcelonajug.superherobattlearena.domain.Submission;
import org.barcelonajug.superherobattlearena.domain.json.DraftSubmission;
import org.barcelonajug.superherobattlearena.domain.json.RoundSpec;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class RoundUseCaseTest {

  private RoundRepositoryPort roundRepository;
  private SubmissionRepositoryPort submissionRepository;
  private RoundUseCase roundUseCase;

  @BeforeEach
  void setUp() {
    roundRepository = mock(RoundRepositoryPort.class);
    submissionRepository = mock(SubmissionRepositoryPort.class);
    roundUseCase = new RoundUseCase(roundRepository, submissionRepository);
  }

  @Test
  void createRound_shouldCreateRoundWithDefaultSpec() {
    // Given
    UUID sessionId = UUID.randomUUID();
    Integer roundNo = 1;
    when(roundRepository.save(any(Round.class))).thenAnswer(i -> i.getArgument(0));

    // When
    Integer resultRoundNo = roundUseCase.createRound(sessionId, roundNo);

    // Then
    assertThat(resultRoundNo).isEqualTo(roundNo);
    ArgumentCaptor<Round> roundCaptor = ArgumentCaptor.forClass(Round.class);
    verify(roundRepository).save(roundCaptor.capture());

    Round savedRound = roundCaptor.getValue();
    assertThat(savedRound.getRoundNo()).isEqualTo(roundNo);
    assertThat(savedRound.getSessionId()).isEqualTo(sessionId);
    assertThat(savedRound.getStatus()).isEqualTo(RoundStatus.OPEN);
    assertThat(savedRound.getSeed()).isNotNull();
    assertThat(savedRound.getSpecJson()).isNotNull();
    assertThat(savedRound.getSpecJson().description()).isEqualTo("Default Round");
    assertThat(savedRound.getSpecJson().teamSize()).isEqualTo(5);
    assertThat(savedRound.getSpecJson().budgetCap()).isEqualTo(100);
  }

  @Test
  void getRoundSpec_shouldReturnSpecWhenRoundExists() {
    // Given
    Integer roundNo = 1;
    RoundSpec expectedSpec =
        new RoundSpec(
            "Test Round",
            5,
            100,
            Collections.emptyMap(),
            Collections.emptyMap(),
            Collections.emptyList(),
            Collections.emptyMap(),
            "ARENA_1");

    Round round = new Round();
    round.setRoundNo(roundNo);
    round.setSpecJson(expectedSpec);

    when(roundRepository.findById(roundNo)).thenReturn(Optional.of(round));

    // When
    Optional<RoundSpec> result = roundUseCase.getRoundSpec(roundNo);

    // Then
    assertThat(result).isPresent();
    assertThat(result.get()).isEqualTo(expectedSpec);
  }

  @Test
  void getRoundSpec_shouldReturnEmpty_whenRoundDoesNotExist() {
    // Given
    Integer roundNo = 1;
    when(roundRepository.findById(roundNo)).thenReturn(Optional.empty());

    // When
    Optional<RoundSpec> result = roundUseCase.getRoundSpec(roundNo);

    // Then
    assertThat(result).isEmpty();
  }

  @Test
  void submitTeam_shouldSaveSubmissionSuccessfully_whenValidTeamSize() {
    // Given
    UUID teamId = UUID.randomUUID();
    Integer roundNo = 1;
    DraftSubmission draft = new DraftSubmission(List.of(1, 2, 3, 4, 5), "Test Strategy");

    when(submissionRepository.findByTeamIdAndRoundNo(teamId, roundNo))
        .thenReturn(Optional.empty());
    when(submissionRepository.save(any(Submission.class))).thenAnswer(i -> i.getArgument(0));

    // When
    roundUseCase.submitTeam(roundNo, teamId, draft);

    // Then
    ArgumentCaptor<Submission> submissionCaptor = ArgumentCaptor.forClass(Submission.class);
    verify(submissionRepository).save(submissionCaptor.capture());

    Submission savedSubmission = submissionCaptor.getValue();
    assertThat(savedSubmission.getTeamId()).isEqualTo(teamId);
    assertThat(savedSubmission.getRoundNo()).isEqualTo(roundNo);
    assertThat(savedSubmission.getSubmissionJson()).isEqualTo(draft);
    assertThat(savedSubmission.getAccepted()).isTrue();
    assertThat(savedSubmission.getSubmittedAt()).isNotNull();
  }

  @Test
  void submitTeam_shouldThrowException_whenTeamAlreadySubmitted() {
    // Given
    UUID teamId = UUID.randomUUID();
    Integer roundNo = 1;
    DraftSubmission draft = new DraftSubmission(List.of(1, 2, 3, 4, 5), "Test Strategy");

    Submission existingSubmission = new Submission();
    existingSubmission.setTeamId(teamId);
    existingSubmission.setRoundNo(roundNo);

    when(submissionRepository.findByTeamIdAndRoundNo(teamId, roundNo))
        .thenReturn(Optional.of(existingSubmission));

    // When/Then
    assertThatThrownBy(() -> roundUseCase.submitTeam(roundNo, teamId, draft))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("already submitted");

    verify(submissionRepository, never()).save(any(Submission.class));
  }

  @Test
  void submitTeam_shouldThrowException_whenTeamSizeIsInvalid() {
    // Given: team with 4 heroes (invalid)
    UUID teamId = UUID.randomUUID();
    Integer roundNo = 1;
    DraftSubmission draft = new DraftSubmission(List.of(1, 2, 3, 4), "Test Strategy");

    when(submissionRepository.findByTeamIdAndRoundNo(teamId, roundNo))
        .thenReturn(Optional.empty());

    // When/Then
    assertThatThrownBy(() -> roundUseCase.submitTeam(roundNo, teamId, draft))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("must have exactly 5 heroes");

    verify(submissionRepository, never()).save(any(Submission.class));
  }

  @Test
  void submitTeam_shouldThrowException_whenTeamSizeIsTooLarge() {
    // Given: team with 6 heroes (invalid)
    UUID teamId = UUID.randomUUID();
    Integer roundNo = 1;
    DraftSubmission draft = new DraftSubmission(List.of(1, 2, 3, 4, 5, 6), "Test Strategy");

    when(submissionRepository.findByTeamIdAndRoundNo(teamId, roundNo))
        .thenReturn(Optional.empty());

    // When/Then
    assertThatThrownBy(() -> roundUseCase.submitTeam(roundNo, teamId, draft))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("must have exactly 5 heroes");

    verify(submissionRepository, never()).save(any(Submission.class));
  }

  @Test
  void getSubmission_shouldReturnDraftSubmission_whenSubmissionExists() {
    // Given
    UUID teamId = UUID.randomUUID();
    Integer roundNo = 1;
    DraftSubmission expectedDraft = new DraftSubmission(List.of(1, 2, 3, 4, 5), "Test Strategy");

    Submission submission = new Submission();
    submission.setTeamId(teamId);
    submission.setRoundNo(roundNo);
    submission.setSubmissionJson(expectedDraft);

    when(submissionRepository.findByTeamIdAndRoundNo(teamId, roundNo))
        .thenReturn(Optional.of(submission));

    // When
    Optional<DraftSubmission> result = roundUseCase.getSubmission(roundNo, teamId);

    // Then
    assertThat(result).isPresent();
    assertThat(result.get()).isEqualTo(expectedDraft);
  }

  @Test
  void getSubmission_shouldReturnEmpty_whenSubmissionDoesNotExist() {
    // Given
    UUID teamId = UUID.randomUUID();
    Integer roundNo = 1;
    when(submissionRepository.findByTeamIdAndRoundNo(teamId, roundNo))
        .thenReturn(Optional.empty());

    // When
    Optional<DraftSubmission> result = roundUseCase.getSubmission(roundNo, teamId);

    // Then
    assertThat(result).isEmpty();
  }

  @Test
  void getSubmissions_shouldReturnAllSubmissionsForRound() {
    // Given
    Integer roundNo = 1;
    UUID sessionId = UUID.randomUUID();

    DraftSubmission draft1 = new DraftSubmission(List.of(1, 2, 3, 4, 5), "Strategy 1");
    DraftSubmission draft2 = new DraftSubmission(List.of(6, 7, 8, 9, 10), "Strategy 2");

    Submission submission1 = new Submission();
    submission1.setTeamId(UUID.randomUUID());
    submission1.setRoundNo(roundNo);
    submission1.setSubmissionJson(draft1);

    Submission submission2 = new Submission();
    submission2.setTeamId(UUID.randomUUID());
    submission2.setRoundNo(roundNo);
    submission2.setSubmissionJson(draft2);

    Round round = new Round();
    round.setRoundNo(roundNo);
    round.setSessionId(sessionId);

    when(roundRepository.findById(roundNo)).thenReturn(Optional.of(round));
    when(submissionRepository.findByRoundNo(roundNo))
        .thenReturn(List.of(submission1, submission2));

    // When
    List<DraftSubmission> result = roundUseCase.getSubmissions(roundNo, sessionId);

    // Then
    assertThat(result).hasSize(2);
    assertThat(result).contains(draft1, draft2);
  }

  @Test
  void getSubmissions_shouldReturnEmptyList_whenSessionIdMismatch() {
    // Given
    Integer roundNo = 1;
    UUID correctSessionId = UUID.randomUUID();
    UUID wrongSessionId = UUID.randomUUID();

    Round round = new Round();
    round.setRoundNo(roundNo);
    round.setSessionId(correctSessionId);

    when(roundRepository.findById(roundNo)).thenReturn(Optional.of(round));

    // When
    List<DraftSubmission> result = roundUseCase.getSubmissions(roundNo, wrongSessionId);

    // Then
    assertThat(result).isEmpty();
    verify(submissionRepository, never()).findByRoundNo(roundNo);
  }

  @Test
  void getSubmissions_shouldReturnSubmissions_whenSessionIdIsNull() {
    // Given
    Integer roundNo = 1;

    DraftSubmission draft1 = new DraftSubmission(List.of(1, 2, 3, 4, 5), "Strategy 1");

    Submission submission1 = new Submission();
    submission1.setTeamId(UUID.randomUUID());
    submission1.setRoundNo(roundNo);
    submission1.setSubmissionJson(draft1);

    Round round = new Round();
    round.setRoundNo(roundNo);
    round.setSessionId(UUID.randomUUID());

    when(roundRepository.findById(roundNo)).thenReturn(Optional.of(round));
    when(submissionRepository.findByRoundNo(roundNo)).thenReturn(List.of(submission1));

    // When: sessionId is null
    List<DraftSubmission> result = roundUseCase.getSubmissions(roundNo, null);

    // Then: submissions are returned
    assertThat(result).hasSize(1);
    assertThat(result).contains(draft1);
  }

  @Test
  void getSubmissions_shouldReturnSubmissions_whenRoundDoesNotExist() {
    // Given
    Integer roundNo = 1;
    UUID sessionId = UUID.randomUUID();

    DraftSubmission draft1 = new DraftSubmission(List.of(1, 2, 3, 4, 5), "Strategy 1");

    Submission submission1 = new Submission();
    submission1.setTeamId(UUID.randomUUID());
    submission1.setRoundNo(roundNo);
    submission1.setSubmissionJson(draft1);

    when(roundRepository.findById(roundNo)).thenReturn(Optional.empty());
    when(submissionRepository.findByRoundNo(roundNo)).thenReturn(List.of(submission1));

    // When
    List<DraftSubmission> result = roundUseCase.getSubmissions(roundNo, sessionId);

    // Then: submissions are returned even if round doesn't exist
    assertThat(result).hasSize(1);
    assertThat(result).contains(draft1);
  }

  @Test
  void listRounds_shouldReturnAllRoundsForSession() {
    // Given
    UUID sessionId = UUID.randomUUID();

    Round round1 = new Round();
    round1.setRoundNo(1);
    round1.setSessionId(sessionId);

    Round round2 = new Round();
    round2.setRoundNo(2);
    round2.setSessionId(sessionId);

    when(roundRepository.findBySessionId(sessionId)).thenReturn(List.of(round1, round2));

    // When
    List<Round> result = roundUseCase.listRounds(sessionId);

    // Then
    assertThat(result).hasSize(2);
    assertThat(result).contains(round1, round2);
    verify(roundRepository).findBySessionId(sessionId);
  }

  @Test
  void listRounds_shouldReturnEmptyList_whenNoRoundsExist() {
    // Given
    UUID sessionId = UUID.randomUUID();
    when(roundRepository.findBySessionId(sessionId)).thenReturn(Collections.emptyList());

    // When
    List<Round> result = roundUseCase.listRounds(sessionId);

    // Then
    assertThat(result).isEmpty();
    verify(roundRepository).findBySessionId(sessionId);
  }
}