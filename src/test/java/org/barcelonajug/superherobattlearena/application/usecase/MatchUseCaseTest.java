package org.barcelonajug.superherobattlearena.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.barcelonajug.superherobattlearena.application.port.out.MatchEventRepositoryPort;
import org.barcelonajug.superherobattlearena.application.port.out.MatchRepositoryPort;
import org.barcelonajug.superherobattlearena.application.port.out.RoundRepositoryPort;
import org.barcelonajug.superherobattlearena.application.port.out.SubmissionRepositoryPort;
import org.barcelonajug.superherobattlearena.domain.MatchStatus;
import org.barcelonajug.superherobattlearena.domain.Submission;
import org.barcelonajug.superherobattlearena.domain.json.DraftSubmission;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class MatchUseCaseTest {

  private SubmissionRepositoryPort submissionRepository;
  private MatchRepositoryPort matchRepository;
  private MatchEventRepositoryPort matchEventRepository;
  private RoundRepositoryPort roundRepository;
  private BattleEngineUseCase battleEngineUseCase;
  private RosterUseCase rosterUseCase;
  private FatigueUseCase fatigueUseCase;
  private MatchUseCase matchUseCase;

  @BeforeEach
  void setUp() {
    submissionRepository = mock(SubmissionRepositoryPort.class);
    matchRepository = mock(MatchRepositoryPort.class);
    matchEventRepository = mock(MatchEventRepositoryPort.class);
    roundRepository = mock(RoundRepositoryPort.class);
    battleEngineUseCase = mock(BattleEngineUseCase.class);
    rosterUseCase = mock(RosterUseCase.class);
    fatigueUseCase = mock(FatigueUseCase.class);

    matchUseCase =
        new MatchUseCase(
            submissionRepository,
            matchRepository,
            matchEventRepository,
            roundRepository,
            battleEngineUseCase,
            rosterUseCase,
            fatigueUseCase);
  }

  @Test
  void autoMatch_shouldCreateMatchesForAllUnmatchedTeams() {
    // Given: 4 teams with submissions, no existing matches
    UUID sessionId = UUID.randomUUID();
    Integer roundNo = 1;

    List<Submission> submissions = createSubmissions(4, roundNo);
    when(submissionRepository.findByRoundNo(roundNo)).thenReturn(submissions);
    when(matchRepository.findByRoundNoAndSessionId(roundNo, sessionId))
        .thenReturn(new ArrayList<>());
    when(matchRepository.save(any(org.barcelonajug.superherobattlearena.domain.Match.class)))
        .thenAnswer(i -> i.getArgument(0));

    // When: autoMatch is called
    List<UUID> matchIds = matchUseCase.autoMatch(sessionId, roundNo);

    // Then: 2 matches should be created (4 teams / 2)
    assertThat(matchIds).hasSize(2);
    verify(matchRepository, times(2))
        .save(any(org.barcelonajug.superherobattlearena.domain.Match.class));
  }

  @Test
  void autoMatch_shouldBeIdempotent_whenCalledMultipleTimes() {
    // Given: 4 teams with submissions
    UUID sessionId = UUID.randomUUID();
    Integer roundNo = 1;

    List<Submission> submissions = createSubmissions(4, roundNo);
    when(submissionRepository.findByRoundNo(roundNo)).thenReturn(submissions);

    // First call: no existing matches
    when(matchRepository.findByRoundNoAndSessionId(roundNo, sessionId))
        .thenReturn(new ArrayList<>());
    when(matchRepository.save(any(org.barcelonajug.superherobattlearena.domain.Match.class)))
        .thenAnswer(i -> i.getArgument(0));

    // When: autoMatch is called first time
    List<UUID> firstCallMatchIds = matchUseCase.autoMatch(sessionId, roundNo);

    // Then: 2 matches created
    assertThat(firstCallMatchIds).hasSize(2);

    // Capture the matches that were created
    ArgumentCaptor<org.barcelonajug.superherobattlearena.domain.Match> matchCaptor =
        ArgumentCaptor.forClass(org.barcelonajug.superherobattlearena.domain.Match.class);
    verify(matchRepository, times(2)).save(matchCaptor.capture());
    List<org.barcelonajug.superherobattlearena.domain.Match> createdMatches =
        matchCaptor.getAllValues();

    // Second call: existing matches present
    when(matchRepository.findByRoundNoAndSessionId(roundNo, sessionId)).thenReturn(createdMatches);

    // When: autoMatch is called second time
    List<UUID> secondCallMatchIds = matchUseCase.autoMatch(sessionId, roundNo);

    // Then: 0 new matches created (all teams already matched)
    assertThat(secondCallMatchIds).isEmpty();
    // Verify save was only called 2 times total (from first call)
    verify(matchRepository, times(2))
        .save(any(org.barcelonajug.superherobattlearena.domain.Match.class));
  }

  @Test
  void autoMatch_shouldOnlyMatchUnmatchedTeams_whenSomeTeamsAlreadyMatched() {
    // Given: 6 teams with submissions, 2 already matched
    UUID sessionId = UUID.randomUUID();
    Integer roundNo = 1;

    List<Submission> submissions = createSubmissions(6, roundNo);
    when(submissionRepository.findByRoundNo(roundNo)).thenReturn(submissions);

    // Teams 0 and 1 are already matched
    org.barcelonajug.superherobattlearena.domain.Match existingMatch =
        org.barcelonajug.superherobattlearena.domain.Match.builder()
            .matchId(UUID.randomUUID())
            .sessionId(sessionId)
            .teamA(submissions.get(0).getTeamId())
            .teamB(submissions.get(1).getTeamId())
            .roundNo(roundNo)
            .status(MatchStatus.PENDING)
            .build();

    when(matchRepository.findByRoundNoAndSessionId(roundNo, sessionId))
        .thenReturn(List.of(existingMatch));
    when(matchRepository.save(any(org.barcelonajug.superherobattlearena.domain.Match.class)))
        .thenAnswer(i -> i.getArgument(0));

    // When: autoMatch is called
    List<UUID> matchIds = matchUseCase.autoMatch(sessionId, roundNo);

    // Then: 2 new matches created for the 4 unmatched teams
    assertThat(matchIds).hasSize(2);
    verify(matchRepository, times(2))
        .save(any(org.barcelonajug.superherobattlearena.domain.Match.class));

    // Verify the new matches don't include already-matched teams
    ArgumentCaptor<org.barcelonajug.superherobattlearena.domain.Match> matchCaptor =
        ArgumentCaptor.forClass(org.barcelonajug.superherobattlearena.domain.Match.class);
    verify(matchRepository, times(2)).save(matchCaptor.capture());
    List<org.barcelonajug.superherobattlearena.domain.Match> newMatches =
        matchCaptor.getAllValues();

    for (org.barcelonajug.superherobattlearena.domain.Match match : newMatches) {
      assertThat(match.getTeamA()).isNotEqualTo(submissions.get(0).getTeamId());
      assertThat(match.getTeamA()).isNotEqualTo(submissions.get(1).getTeamId());
      assertThat(match.getTeamB()).isNotEqualTo(submissions.get(0).getTeamId());
      assertThat(match.getTeamB()).isNotEqualTo(submissions.get(1).getTeamId());
    }
  }

  @Test
  void autoMatch_shouldHandleOddNumberOfTeams() {
    // Given: 5 teams with submissions
    UUID sessionId = UUID.randomUUID();
    Integer roundNo = 1;

    List<Submission> submissions = createSubmissions(5, roundNo);
    when(submissionRepository.findByRoundNo(roundNo)).thenReturn(submissions);
    when(matchRepository.findByRoundNoAndSessionId(roundNo, sessionId))
        .thenReturn(new ArrayList<>());
    when(matchRepository.save(any(org.barcelonajug.superherobattlearena.domain.Match.class)))
        .thenAnswer(i -> i.getArgument(0));

    // When: autoMatch is called
    List<UUID> matchIds = matchUseCase.autoMatch(sessionId, roundNo);

    // Then: 2 matches created (5 teams / 2 = 2, with 1 team left unmatched)
    assertThat(matchIds).hasSize(2);
    verify(matchRepository, times(2))
        .save(any(org.barcelonajug.superherobattlearena.domain.Match.class));
  }

  // Helper method to create test submissions
  private List<Submission> createSubmissions(int count, Integer roundNo) {
    List<Submission> submissions = new ArrayList<>();
    for (int i = 0; i < count; i++) {
      UUID teamId = UUID.randomUUID();
      DraftSubmission draftSubmission = new DraftSubmission(List.of(1, 2, 3), "Test submission");

      Submission submission = new Submission();
      submission.setTeamId(teamId);
      submission.setRoundNo(roundNo);
      submission.setSubmissionJson(draftSubmission);
      submission.setAccepted(true);

      submissions.add(submission);
    }
    return submissions;
  }
}
