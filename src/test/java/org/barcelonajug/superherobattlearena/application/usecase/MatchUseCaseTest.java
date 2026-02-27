package org.barcelonajug.superherobattlearena.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.barcelonajug.superherobattlearena.application.port.out.MatchEventRepositoryPort;
import org.barcelonajug.superherobattlearena.application.port.out.MatchRepositoryPort;
import org.barcelonajug.superherobattlearena.application.port.out.RoundRepositoryPort;
import org.barcelonajug.superherobattlearena.application.port.out.SubmissionRepositoryPort;
import org.barcelonajug.superherobattlearena.domain.Hero;
import org.barcelonajug.superherobattlearena.domain.Match;
import org.barcelonajug.superherobattlearena.domain.MatchStatus;
import org.barcelonajug.superherobattlearena.domain.Round;
import org.barcelonajug.superherobattlearena.domain.RoundStatus;
import org.barcelonajug.superherobattlearena.domain.SimulationResult;
import org.barcelonajug.superherobattlearena.domain.Submission;
import org.barcelonajug.superherobattlearena.domain.json.DraftSubmission;
import org.barcelonajug.superherobattlearena.domain.json.MatchEventSnapshot;
import org.barcelonajug.superherobattlearena.domain.mother.MatchMother;
import org.barcelonajug.superherobattlearena.domain.mother.RoundSpecMother;
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
    when(matchRepository.save(any(Match.class))).thenAnswer(i -> i.getArgument(0));

    // When: autoMatch is called
    List<UUID> matchIds = matchUseCase.autoMatch(sessionId, roundNo);

    // Then: 2 matches should be created (4 teams / 2)
    assertThat(matchIds).hasSize(2);
    verify(matchRepository, times(2)).save(any(Match.class));
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
    when(matchRepository.save(any(Match.class))).thenAnswer(i -> i.getArgument(0));

    // When: autoMatch is called first time
    List<UUID> firstCallMatchIds = matchUseCase.autoMatch(sessionId, roundNo);

    // Then: 2 matches created
    assertThat(firstCallMatchIds).hasSize(2);

    // Capture the matches that were created
    ArgumentCaptor<Match> matchCaptor = ArgumentCaptor.forClass(Match.class);
    verify(matchRepository, times(2)).save(matchCaptor.capture());
    List<Match> createdMatches = matchCaptor.getAllValues();

    // Second call: existing matches present
    when(matchRepository.findByRoundNoAndSessionId(roundNo, sessionId)).thenReturn(createdMatches);

    // When: autoMatch is called second time
    List<UUID> secondCallMatchIds = matchUseCase.autoMatch(sessionId, roundNo);

    // Then: 0 new matches created (all teams already matched)
    assertThat(secondCallMatchIds).isEmpty();
    // Verify save was only called 2 times total (from first call)
    verify(matchRepository, times(2)).save(any(Match.class));
  }

  @Test
  void autoMatch_shouldOnlyMatchUnmatchedTeams_whenSomeTeamsAlreadyMatched() {
    // Given: 6 teams with submissions, 2 already matched
    UUID sessionId = UUID.randomUUID();
    Integer roundNo = 1;

    List<Submission> submissions = createSubmissions(6, roundNo);
    when(submissionRepository.findByRoundNo(roundNo)).thenReturn(submissions);

    // Teams 0 and 1 are already matched
    Match existingMatch =
        MatchMother.aMatch(
            UUID.randomUUID(),
            sessionId,
            submissions.getFirst().getTeamId(),
            submissions.get(1).getTeamId(),
            roundNo,
            MatchStatus.PENDING);

    when(matchRepository.findByRoundNoAndSessionId(roundNo, sessionId))
        .thenReturn(List.of(existingMatch));
    when(matchRepository.save(any(Match.class))).thenAnswer(i -> i.getArgument(0));

    // When: autoMatch is called
    List<UUID> matchIds = matchUseCase.autoMatch(sessionId, roundNo);

    // Then: 2 new matches created for the 4 unmatched teams
    assertThat(matchIds).hasSize(2);
    verify(matchRepository, times(2)).save(any(Match.class));

    // Verify the new matches don't include already-matched teams
    ArgumentCaptor<Match> matchCaptor = ArgumentCaptor.forClass(Match.class);
    verify(matchRepository, times(2)).save(matchCaptor.capture());
    List<Match> newMatches = matchCaptor.getAllValues();

    for (Match match : newMatches) {
      assertThat(match.getTeamA()).isNotEqualTo(submissions.getFirst().getTeamId());
      assertThat(match.getTeamA()).isNotEqualTo(submissions.get(1).getTeamId());
      assertThat(match.getTeamB()).isNotEqualTo(submissions.getFirst().getTeamId());
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
    when(matchRepository.save(any(Match.class))).thenAnswer(i -> i.getArgument(0));

    // When: autoMatch is called
    List<UUID> matchIds = matchUseCase.autoMatch(sessionId, roundNo);

    // Then: 2 matches created (5 teams / 2 = 2, with 1 team left unmatched)
    assertThat(matchIds).hasSize(2);
    verify(matchRepository, times(2)).save(any(Match.class));
  }

  @Test
  void createMatch_shouldSaveNewMatch() {
    UUID teamA = UUID.randomUUID();
    UUID teamB = UUID.randomUUID();
    UUID sessionId = UUID.randomUUID();
    Integer roundNo = 1;

    UUID matchId = matchUseCase.createMatch(teamA, teamB, roundNo, sessionId);

    assertThat(matchId).isNotNull();
    verify(matchRepository).save(any(Match.class));
  }

  @Test
  void runMatch_shouldExecuteSimulationAndSaveResult() {
    // Given
    UUID matchId = UUID.randomUUID();
    UUID sessionId = UUID.randomUUID();
    UUID teamA = UUID.randomUUID();
    UUID teamB = UUID.randomUUID();
    Integer roundNo = 1;

    Match match =
        MatchMother.aMatch(matchId, sessionId, teamA, teamB, roundNo, MatchStatus.PENDING);

    when(matchRepository.findById(matchId)).thenReturn(Optional.of(match));

    Submission subA = createSubmission(teamA, roundNo);
    Submission subB = createSubmission(teamB, roundNo);
    when(submissionRepository.findByTeamIdAndRoundNo(teamA, roundNo)).thenReturn(Optional.of(subA));
    when(submissionRepository.findByTeamIdAndRoundNo(teamB, roundNo)).thenReturn(Optional.of(subB));

    Round round = new Round();
    round.setSeed(12345L);
    round.setSpecJson(RoundSpecMother.aStandardRoundSpec());
    when(roundRepository.findBySessionIdAndRoundNo(sessionId, roundNo))
        .thenReturn(Optional.of(round));

    SimulationResult simResult =
        new SimulationResult(teamA, 10, List.of(mock(MatchEventSnapshot.class)));
    when(battleEngineUseCase.simulate(any(), any(), any(), anyLong(), any(), any(), any()))
        .thenReturn(simResult);

    Hero mockHero = mock(Hero.class);
    when(rosterUseCase.getHeroes(any())).thenReturn(List.of(mockHero));
    when(mockHero.id()).thenReturn(1);
    when(fatigueUseCase.applyFatigue(any(UUID.class), anyList(), anyInt()))
        .thenReturn(List.of(mockHero));

    // Initial call for pending matches returns empty to trigger round closing
    when(matchRepository.findPendingMatches(roundNo, sessionId)).thenReturn(List.of());

    // When
    String result = matchUseCase.runMatch(matchId);

    // Then
    assertThat(result).isEqualTo(teamA.toString());
    verify(matchRepository).save(match);
    verify(matchEventRepository).saveAll(any());
    verify(fatigueUseCase, times(2)).recordUsage(any(), anyInt(), any());
    assertThat(match.getStatus()).isEqualTo(MatchStatus.COMPLETED);
    verify(roundRepository).save(round);
    assertThat(round.getStatus()).isEqualTo(RoundStatus.CLOSED);
  }

  @Test
  void runMatch_shouldHandleDraw() {
    // Given
    UUID matchId = UUID.randomUUID();
    UUID sessionId = UUID.randomUUID();
    UUID teamA = UUID.randomUUID();
    UUID teamB = UUID.randomUUID();
    Integer roundNo = 1;

    Match match =
        MatchMother.aMatch(matchId, sessionId, teamA, teamB, roundNo, MatchStatus.PENDING);

    when(matchRepository.findById(matchId)).thenReturn(Optional.of(match));
    when(submissionRepository.findByTeamIdAndRoundNo(teamA, roundNo))
        .thenReturn(Optional.of(createSubmission(teamA, roundNo)));
    when(submissionRepository.findByTeamIdAndRoundNo(teamB, roundNo))
        .thenReturn(Optional.of(createSubmission(teamB, roundNo)));

    Round round = new Round();
    round.setSpecJson(RoundSpecMother.aStandardRoundSpec());
    when(roundRepository.findBySessionIdAndRoundNo(sessionId, roundNo))
        .thenReturn(Optional.of(round));

    // Simulation result with null winnerTeamId = DRAW
    SimulationResult simResult = new SimulationResult(null, 10, List.of());
    when(battleEngineUseCase.simulate(any(), any(), any(), anyLong(), any(), any(), any()))
        .thenReturn(simResult);

    // Mock heroes for buildBattleTeam
    Hero mockHero = mock(Hero.class);
    when(rosterUseCase.getHeroes(any())).thenReturn(List.of(mockHero));
    when(mockHero.id()).thenReturn(1);
    when(fatigueUseCase.applyFatigue(any(UUID.class), anyList(), anyInt()))
        .thenReturn(List.of(mockHero));

    // When
    String result = matchUseCase.runMatch(matchId);

    // Then
    assertThat(result).isEqualTo("DRAW");
  }

  @Test
  void runMatch_shouldThrowException_whenSubmissionsMissing() {
    UUID matchId = UUID.randomUUID();
    Match match =
        MatchMother.aMatch(
            matchId,
            UUID.randomUUID(),
            UUID.randomUUID(),
            UUID.randomUUID(),
            1,
            MatchStatus.PENDING);

    when(matchRepository.findById(matchId)).thenReturn(Optional.of(match));
    when(submissionRepository.findByTeamIdAndRoundNo(any(), anyInt())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> matchUseCase.runMatch(matchId))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("Submissions missing");
  }

  @Test
  void runMatch_shouldNotCloseRound_whenMatchesStillPending() {
    // Given
    UUID matchId = UUID.randomUUID();
    UUID sessionId = UUID.randomUUID();
    UUID teamA = UUID.randomUUID();
    UUID teamB = UUID.randomUUID();
    Integer roundNo = 1;

    Match match =
        MatchMother.aMatch(matchId, sessionId, teamA, teamB, roundNo, MatchStatus.PENDING);

    when(matchRepository.findById(matchId)).thenReturn(Optional.of(match));
    when(submissionRepository.findByTeamIdAndRoundNo(any(), anyInt()))
        .thenReturn(Optional.of(createSubmission(teamA, roundNo)));

    Round round = new Round();
    round.setStatus(RoundStatus.OPEN);
    round.setSpecJson(RoundSpecMother.aStandardRoundSpec());
    when(roundRepository.findBySessionIdAndRoundNo(sessionId, roundNo))
        .thenReturn(Optional.of(round));

    SimulationResult simResult = new SimulationResult(teamA, 10, List.of());
    when(battleEngineUseCase.simulate(any(), any(), any(), anyLong(), any(), any(), any()))
        .thenReturn(simResult);

    Hero mockHero = mock(Hero.class);
    when(rosterUseCase.getHeroes(any())).thenReturn(List.of(mockHero));
    when(mockHero.id()).thenReturn(1);
    when(fatigueUseCase.applyFatigue(any(UUID.class), anyList(), anyInt()))
        .thenReturn(List.of(mockHero));

    // One more pending match exists
    when(matchRepository.findPendingMatches(roundNo, sessionId)).thenReturn(List.of(new Match()));

    // When
    matchUseCase.runMatch(matchId);

    // Then
    assertThat(round.getStatus()).isEqualTo(RoundStatus.OPEN);
  }

  @Test
  void runMatch_shouldThrowException_whenMatchNotFound() {
    UUID matchId = UUID.randomUUID();
    when(matchRepository.findById(matchId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> matchUseCase.runMatch(matchId))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Match not found");
  }

  @Test
  void runMatch_shouldThrowException_whenMatchAlreadyProcessed() {
    UUID matchId = UUID.randomUUID();
    Match match =
        MatchMother.aMatch(
            matchId,
            UUID.randomUUID(),
            UUID.randomUUID(),
            UUID.randomUUID(),
            1,
            MatchStatus.COMPLETED);
    when(matchRepository.findById(matchId)).thenReturn(Optional.of(match));

    assertThatThrownBy(() -> matchUseCase.runMatch(matchId))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("already run");
  }

  @Test
  void getAllMatches_shouldDelegateToRepository() {
    when(matchRepository.findAll()).thenReturn(List.of());
    matchUseCase.getAllMatches();
    verify(matchRepository).findAll();
  }

  @Test
  void getMatch_shouldDelegateToRepository() {
    UUID matchId = UUID.randomUUID();
    when(matchRepository.findById(matchId)).thenReturn(Optional.empty());
    matchUseCase.getMatch(matchId);
    verify(matchRepository).findById(matchId);
  }

  @Test
  void getMatchEventEntities_shouldDelegateToRepository() {
    UUID matchId = UUID.randomUUID();
    when(matchEventRepository.findByMatchId(matchId)).thenReturn(List.of());
    matchUseCase.getMatchEventEntities(matchId);
    verify(matchEventRepository).findByMatchId(matchId);
  }

  @Test
  void runMatchResult_shouldDelegateToRunMatch() {
    UUID matchId = UUID.randomUUID();
    Match match =
        MatchMother.aMatch(
            matchId,
            UUID.randomUUID(),
            UUID.randomUUID(),
            UUID.randomUUID(),
            1,
            MatchStatus.COMPLETED);
    when(matchRepository.findById(matchId)).thenReturn(Optional.of(match));

    // This will throw IllegalStateException because it's already COMPLETED, but
    // proves delegation
    assertThatThrownBy(() -> matchUseCase.runMatchResult(match))
        .isInstanceOf(IllegalStateException.class);
  }

  @Test
  void getSubmission_shouldDelegateToRepository() {
    UUID teamId = UUID.randomUUID();
    matchUseCase.getSubmission(teamId, 1);
    verify(submissionRepository).findByTeamIdAndRoundNo(teamId, 1);
  }

  @Test
  void getBattleTeam_shouldDelegateToBuildBattleTeam() {
    UUID teamId = UUID.randomUUID();
    DraftSubmission sub = new DraftSubmission(List.of(1), "Sub");
    Hero mockHero = mock(Hero.class);
    when(mockHero.id()).thenReturn(1);
    when(rosterUseCase.getHeroes(any())).thenReturn(List.of(mockHero));
    when(fatigueUseCase.applyFatigue(any(UUID.class), anyList(), anyInt()))
        .thenReturn(List.of(mockHero));

    List<Hero> result = matchUseCase.getBattleTeam(teamId, sub, 1);

    assertThat(result).hasSize(1);
  }

  @Test
  void updateUsage_shouldDelegateToFatigueUseCase() {
    UUID teamId = UUID.randomUUID();
    List<Integer> heroIds = List.of(1, 2);
    matchUseCase.updateUsage(teamId, 1, heroIds);
    verify(fatigueUseCase).recordUsage(teamId, 1, heroIds);
  }

  @Test
  void getPendingMatches_shouldDelegateToRepository() {
    Integer roundNo = 1;
    UUID sessionId = UUID.randomUUID();
    when(matchRepository.findPendingMatches(roundNo, sessionId)).thenReturn(List.of());
    matchUseCase.getPendingMatches(roundNo, sessionId);
    verify(matchRepository).findPendingMatches(roundNo, sessionId);
  }

  @Test
  void runMatch_shouldThrowException_whenSubBIsMissing() {
    UUID matchId = UUID.randomUUID();
    UUID teamA = UUID.randomUUID();
    UUID teamB = UUID.randomUUID();
    Match match =
        MatchMother.aMatch(matchId, UUID.randomUUID(), teamA, teamB, 1, MatchStatus.PENDING);

    when(matchRepository.findById(matchId)).thenReturn(Optional.of(match));
    when(submissionRepository.findByTeamIdAndRoundNo(teamA, 1))
        .thenReturn(Optional.of(createSubmission(teamA, 1)));
    when(submissionRepository.findByTeamIdAndRoundNo(teamB, 1)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> matchUseCase.runMatch(matchId))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("Submissions missing");
  }

  @Test
  void buildBattleTeam_shouldThrowExceptionWhenHeroNotFoundInRoster() {
    UUID teamId = UUID.randomUUID();
    DraftSubmission sub = new DraftSubmission(List.of(1, 2), "Sub");
    Hero mockHero1 = mock(Hero.class);
    when(mockHero1.id()).thenReturn(1);
    // Only hero 1 is found, hero 2 is missing
    when(rosterUseCase.getHeroes(any())).thenReturn(List.of(mockHero1));

    assertThatThrownBy(() -> matchUseCase.getBattleTeam(teamId, sub, 1))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Hero not found in roster: 2");
  }

  private Submission createSubmission(UUID teamId, Integer roundNo) {
    Submission sub = new Submission();
    sub.setTeamId(teamId);
    sub.setRoundNo(roundNo);
    sub.setSubmissionJson(new DraftSubmission(List.of(1), "Sub"));
    return sub;
  }

  // Helper method to create test submissions
  private List<Submission> createSubmissions(int count, Integer roundNo) {
    List<Submission> submissions = new ArrayList<>();
    for (int i = 0; i < count; i++) {
      submissions.add(createSubmission(UUID.randomUUID(), roundNo));
    }
    return submissions;
  }
}
