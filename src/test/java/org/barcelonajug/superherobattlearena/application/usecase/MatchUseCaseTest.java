package org.barcelonajug.superherobattlearena.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.barcelonajug.superherobattlearena.application.port.out.EliminationRepositoryPort;
import org.barcelonajug.superherobattlearena.application.port.out.MatchEventRepositoryPort;
import org.barcelonajug.superherobattlearena.application.port.out.MatchRepositoryPort;
import org.barcelonajug.superherobattlearena.application.port.out.RoundRepositoryPort;
import org.barcelonajug.superherobattlearena.application.port.out.SubmissionRepositoryPort;
import org.barcelonajug.superherobattlearena.application.port.out.TeamRepositoryPort;
import org.barcelonajug.superherobattlearena.domain.Elimination;
import org.barcelonajug.superherobattlearena.domain.Hero;
import org.barcelonajug.superherobattlearena.domain.Match;
import org.barcelonajug.superherobattlearena.domain.MatchEvent;
import org.barcelonajug.superherobattlearena.domain.MatchStatus;
import org.barcelonajug.superherobattlearena.domain.MatchType;
import org.barcelonajug.superherobattlearena.domain.Round;
import org.barcelonajug.superherobattlearena.domain.RoundStatus;
import org.barcelonajug.superherobattlearena.domain.SimulationResult;
import org.barcelonajug.superherobattlearena.domain.Submission;
import org.barcelonajug.superherobattlearena.domain.Team;
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
  private TeamRepositoryPort teamRepository;
  private EliminationRepositoryPort eliminationRepository;
  private MatchUseCase matchUseCase;

  /** Creates the match use case with isolated dependencies and a default seeded round. */
  @BeforeEach
  void setUp() {
    submissionRepository = mock(SubmissionRepositoryPort.class);
    matchRepository = mock(MatchRepositoryPort.class);
    matchEventRepository = mock(MatchEventRepositoryPort.class);
    roundRepository = mock(RoundRepositoryPort.class);
    battleEngineUseCase = mock(BattleEngineUseCase.class);
    rosterUseCase = mock(RosterUseCase.class);
    fatigueUseCase = mock(FatigueUseCase.class);
    teamRepository = mock(TeamRepositoryPort.class);
    eliminationRepository = mock(EliminationRepositoryPort.class);

    Round round = new Round();
    round.setRoundNo(1);
    round.setSeed(42L);
    when(roundRepository.findBySessionIdAndRoundNo(any(), any())).thenReturn(Optional.of(round));

    matchUseCase =
        new MatchUseCase(
            submissionRepository,
            matchRepository,
            matchEventRepository,
            roundRepository,
            battleEngineUseCase,
            rosterUseCase,
            fatigueUseCase,
            teamRepository,
            eliminationRepository);
  }

  @Test
  void autoMatch_shouldCreateMatchesForAllUnmatchedTeams() {
    UUID sessionId = UUID.randomUUID();
    Integer roundNo = 1;

    List<Submission> submissions = createSubmissions(4, roundNo);
    when(submissionRepository.findByRoundNo(roundNo)).thenReturn(submissions);
    when(matchRepository.findByRoundNoAndSessionId(roundNo, sessionId))
        .thenReturn(new ArrayList<>());
    when(matchRepository.save(any(Match.class))).thenAnswer(i -> i.getArgument(0));

    List<UUID> matchIds = matchUseCase.autoMatch(sessionId, roundNo);

    assertThat(matchIds).hasSize(2);
    verify(matchRepository).saveAll(anyList());
  }

  /** Verifies that repeated auto-matching returns existing matches without saving duplicates. */
  @Test
  void autoMatch_shouldBeIdempotent_whenCalledMultipleTimes() {
    UUID sessionId = UUID.randomUUID();
    Integer roundNo = 1;

    List<Submission> submissions = createSubmissions(4, roundNo);
    when(submissionRepository.findByRoundNo(roundNo)).thenReturn(submissions);

    when(matchRepository.findByRoundNoAndSessionId(roundNo, sessionId))
        .thenReturn(new ArrayList<>());
    when(matchRepository.save(any(Match.class))).thenAnswer(i -> i.getArgument(0));

    List<UUID> firstCallMatchIds = matchUseCase.autoMatch(sessionId, roundNo);
    assertThat(firstCallMatchIds).hasSize(2);

    ArgumentCaptor<List<Match>> matchCaptor = ArgumentCaptor.forClass(List.class);
    verify(matchRepository).saveAll(matchCaptor.capture());
    List<Match> createdMatches = matchCaptor.getValue();

    when(matchRepository.findByRoundNoAndSessionId(roundNo, sessionId)).thenReturn(createdMatches);

    List<UUID> secondCallMatchIds = matchUseCase.autoMatch(sessionId, roundNo);

    assertThat(secondCallMatchIds).hasSize(2);
    verify(matchRepository, times(1)).saveAll(anyList());
  }

  /** Verifies that a first-round field whose size is not a power of two is rejected. */
  @Test
  void autoMatch_shouldThrow_whenTeamCountIsNotPowerOfTwo() {
    UUID sessionId = UUID.randomUUID();
    Integer roundNo = 1;

    List<Submission> submissions = createSubmissions(5, roundNo);
    when(submissionRepository.findByRoundNo(roundNo)).thenReturn(submissions);
    when(matchRepository.findByRoundNoAndSessionId(roundNo, sessionId))
        .thenReturn(new ArrayList<>());

    assertThatThrownBy(() -> matchUseCase.autoMatch(sessionId, roundNo))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("power of two");
  }

  /** Verifies final and third-place pairings and positions from two completed semifinals. */
  @Test
  void autoMatch_shouldCreateFinalAndThirdPlaceMatches_whenFourTeamsInFinalStage() {
    UUID sessionId = UUID.randomUUID();
    int roundNo = 2;

    UUID team1 = UUID.randomUUID();
    UUID team2 = UUID.randomUUID();
    UUID team3 = UUID.randomUUID();
    UUID team4 = UUID.randomUUID();

    Match sf1 =
        Match.builder()
            .matchId(UUID.randomUUID())
            .sessionId(sessionId)
            .roundNo(1)
            .teamA(team1)
            .teamB(team2)
            .status(MatchStatus.COMPLETED)
            .winnerTeam(team1)
            .bracketPosition(0)
            .build();

    Match sf2 =
        Match.builder()
            .matchId(UUID.randomUUID())
            .sessionId(sessionId)
            .roundNo(1)
            .teamA(team3)
            .teamB(team4)
            .status(MatchStatus.COMPLETED)
            .winnerTeam(team3)
            .bracketPosition(1)
            .build();

    when(matchRepository.findByRoundNoAndSessionId(roundNo, sessionId)).thenReturn(List.of());
    when(matchRepository.findByRoundNoAndSessionId(1, sessionId)).thenReturn(List.of(sf1, sf2));

    List<Submission> submissions =
        List.of(
            Submission.builder().teamId(team1).roundNo(2).build(),
            Submission.builder().teamId(team2).roundNo(2).build(),
            Submission.builder().teamId(team3).roundNo(2).build(),
            Submission.builder().teamId(team4).roundNo(2).build());
    when(submissionRepository.findByRoundNo(roundNo)).thenReturn(submissions);

    List<UUID> matchIds = matchUseCase.autoMatch(sessionId, roundNo);

    assertThat(matchIds).hasSize(2);
    ArgumentCaptor<List<Match>> captor = ArgumentCaptor.forClass(List.class);
    verify(matchRepository).saveAll(captor.capture());
    List<Match> created = captor.getValue();

    Match grandFinal =
        created.stream().filter(m -> m.getMatchType() == MatchType.FINAL).findFirst().orElseThrow();
    assertThat(grandFinal.getTeamA()).isEqualTo(team1);
    assertThat(grandFinal.getTeamB()).isEqualTo(team3);
    assertThat(grandFinal.getBracketPosition()).isEqualTo(0);

    Match thirdPlace =
        created.stream()
            .filter(m -> m.getMatchType() == MatchType.THIRD_PLACE)
            .findFirst()
            .orElseThrow();
    assertThat(thirdPlace.getTeamA()).isEqualTo(team2);
    assertThat(thirdPlace.getTeamB()).isEqualTo(team4);
    assertThat(thirdPlace.getBracketPosition()).isEqualTo(1);
  }

  /** Creates four completed matches with ordered bracket positions and the supplied winners. */
  private List<Match> createFourCompletedMatches(UUID sessionId, int roundNo, List<UUID> winners) {
    List<Match> matches = new ArrayList<>();
    for (int i = 0; i < 4; i++) {
      matches.add(
          Match.builder()
              .matchId(UUID.randomUUID())
              .sessionId(sessionId)
              .roundNo(roundNo)
              .teamA(winners.get(i))
              .teamB(UUID.randomUUID())
              .status(MatchStatus.COMPLETED)
              .winnerTeam(winners.get(i))
              .bracketPosition(i)
              .build());
    }
    return matches;
  }

  /** Verifies that adjacent winners advance to pending knockout matches in bracket order. */
  @Test
  void autoMatch_round2_knockout_with8Teams_shouldPairWinners() {
    UUID sessionId = UUID.randomUUID();
    int roundNo = 2;

    UUID w0 = UUID.randomUUID();
    UUID w1 = UUID.randomUUID();
    UUID w2 = UUID.randomUUID();
    UUID w3 = UUID.randomUUID();
    List<UUID> winners = List.of(w0, w1, w2, w3);

    when(matchRepository.findByRoundNoAndSessionId(roundNo, sessionId)).thenReturn(List.of());
    when(matchRepository.findByRoundNoAndSessionId(1, sessionId))
        .thenReturn(createFourCompletedMatches(sessionId, 1, winners));

    List<Submission> submissions =
        List.of(
            Submission.builder().teamId(w0).roundNo(2).build(),
            Submission.builder().teamId(w1).roundNo(2).build(),
            Submission.builder().teamId(w2).roundNo(2).build(),
            Submission.builder().teamId(w3).roundNo(2).build());
    when(submissionRepository.findByRoundNo(roundNo)).thenReturn(submissions);

    List<UUID> matchIds = matchUseCase.autoMatch(sessionId, roundNo);

    assertThat(matchIds).hasSize(2);
    ArgumentCaptor<List<Match>> captor = ArgumentCaptor.forClass(List.class);
    verify(matchRepository).saveAll(captor.capture());
    List<Match> created = captor.getValue();

    Match k0 = created.get(0);
    assertThat(k0.getTeamA()).isEqualTo(w0);
    assertThat(k0.getTeamB()).isEqualTo(w1);
    assertThat(k0.getMatchType()).isEqualTo(MatchType.KNOCKOUT);
    assertThat(k0.getBracketPosition()).isEqualTo(0);
    assertThat(k0.getStatus()).isEqualTo(MatchStatus.PENDING);

    Match k1 = created.get(1);
    assertThat(k1.getTeamA()).isEqualTo(w2);
    assertThat(k1.getTeamB()).isEqualTo(w3);
    assertThat(k1.getMatchType()).isEqualTo(MatchType.KNOCKOUT);
    assertThat(k1.getBracketPosition()).isEqualTo(1);
    assertThat(k1.getStatus()).isEqualTo(MatchStatus.PENDING);
  }

  /** Verifies that missing submissions yield walkover winners and elimination records. */
  @Test
  void autoMatch_round2_walkovers_shouldHandleMissingSubmissions() {
    UUID sessionId = UUID.randomUUID();
    int roundNo = 2;

    UUID w0 = UUID.randomUUID();
    UUID w1 = UUID.randomUUID();
    UUID w2 = UUID.randomUUID();
    UUID w3 = UUID.randomUUID();
    List<UUID> winners = List.of(w0, w1, w2, w3);

    when(matchRepository.findByRoundNoAndSessionId(roundNo, sessionId)).thenReturn(List.of());
    when(matchRepository.findByRoundNoAndSessionId(1, sessionId))
        .thenReturn(createFourCompletedMatches(sessionId, 1, winners));

    List<Submission> submissions =
        List.of(
            Submission.builder().teamId(w0).roundNo(2).build(),
            Submission.builder().teamId(w3).roundNo(2).build());
    when(submissionRepository.findByRoundNo(roundNo)).thenReturn(submissions);

    List<UUID> matchIds = matchUseCase.autoMatch(sessionId, roundNo);

    assertThat(matchIds).hasSize(2);
    ArgumentCaptor<List<Match>> captor = ArgumentCaptor.forClass(List.class);
    verify(matchRepository).saveAll(captor.capture());
    List<Match> created = captor.getValue();

    Match k0 = created.get(0);
    assertThat(k0.getTeamA()).isEqualTo(w0);
    assertThat(k0.getTeamB()).isEqualTo(w1);
    assertThat(k0.getStatus()).isEqualTo(MatchStatus.COMPLETED);
    assertThat(k0.getWinnerTeam()).isEqualTo(w0);

    Match k1 = created.get(1);
    assertThat(k1.getTeamA()).isEqualTo(w2);
    assertThat(k1.getTeamB()).isEqualTo(w3);
    assertThat(k1.getStatus()).isEqualTo(MatchStatus.COMPLETED);
    assertThat(k1.getWinnerTeam()).isEqualTo(w3);

    verify(eliminationRepository, times(2)).save(any(Elimination.class));
  }

  /** Verifies that matches are cancelled when neither advancing team submitted a squad. */
  @Test
  void autoMatch_round2_walkover_bothMissingSubmissions_shouldCancelMatch() {
    UUID sessionId = UUID.randomUUID();
    int roundNo = 2;

    UUID w0 = UUID.randomUUID();
    UUID w1 = UUID.randomUUID();
    UUID w2 = UUID.randomUUID();
    UUID w3 = UUID.randomUUID();
    List<UUID> winners = List.of(w0, w1, w2, w3);

    when(matchRepository.findByRoundNoAndSessionId(roundNo, sessionId)).thenReturn(List.of());
    when(matchRepository.findByRoundNoAndSessionId(1, sessionId))
        .thenReturn(createFourCompletedMatches(sessionId, 1, winners));
    when(submissionRepository.findByRoundNo(roundNo)).thenReturn(List.of());

    List<UUID> matchIds = matchUseCase.autoMatch(sessionId, roundNo);

    assertThat(matchIds).hasSize(2);
    ArgumentCaptor<List<Match>> captor = ArgumentCaptor.forClass(List.class);
    verify(matchRepository).saveAll(captor.capture());
    List<Match> created = captor.getValue();

    assertThat(created.get(0).getStatus()).isEqualTo(MatchStatus.CANCELLED);
    assertThat(created.get(1).getStatus()).isEqualTo(MatchStatus.CANCELLED);
  }

  /** Verifies that auto-matching rejects a missing previous round. */
  @Test
  void autoMatch_round2_shouldThrow_whenPreviousMatchesEmpty() {
    UUID sessionId = UUID.randomUUID();
    int roundNo = 2;

    when(matchRepository.findByRoundNoAndSessionId(roundNo, sessionId)).thenReturn(List.of());
    when(matchRepository.findByRoundNoAndSessionId(1, sessionId)).thenReturn(List.of());

    assertThatThrownBy(() -> matchUseCase.autoMatch(sessionId, roundNo))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("Previous round matches not found for round 1");
  }

  /** Verifies that auto-matching rejects unfinished previous matches. */
  @Test
  void autoMatch_round2_shouldThrow_whenPreviousMatchesNotAllCompleted() {
    UUID sessionId = UUID.randomUUID();
    int roundNo = 2;

    Match m0 =
        Match.builder()
            .matchId(UUID.randomUUID())
            .sessionId(sessionId)
            .roundNo(1)
            .teamA(UUID.randomUUID())
            .teamB(UUID.randomUUID())
            .status(MatchStatus.PENDING)
            .build();

    when(matchRepository.findByRoundNoAndSessionId(roundNo, sessionId)).thenReturn(List.of());
    when(matchRepository.findByRoundNoAndSessionId(1, sessionId)).thenReturn(List.of(m0));

    assertThatThrownBy(() -> matchUseCase.autoMatch(sessionId, roundNo))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("Previous round matches must be completed");
  }

  /** Verifies that auto-matching rejects progression after a single completed final. */
  @Test
  void autoMatch_round3_shouldThrow_whenTournamentAlreadyCompleted() {
    UUID sessionId = UUID.randomUUID();
    int roundNo = 3;

    Match m0 =
        Match.builder()
            .matchId(UUID.randomUUID())
            .sessionId(sessionId)
            .roundNo(2)
            .teamA(UUID.randomUUID())
            .teamB(UUID.randomUUID())
            .status(MatchStatus.COMPLETED)
            .winnerTeam(UUID.randomUUID())
            .build();

    when(matchRepository.findByRoundNoAndSessionId(roundNo, sessionId)).thenReturn(List.of());
    when(matchRepository.findByRoundNoAndSessionId(2, sessionId)).thenReturn(List.of(m0));

    assertThatThrownBy(() -> matchUseCase.autoMatch(sessionId, roundNo))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("Tournament already completed");
  }

  /** Verifies that an odd first-round team count is rejected. */
  @Test
  void autoMatch_round1_withOddOrZeroTeams_shouldThrow() {
    UUID sessionId = UUID.randomUUID();
    int roundNo = 1;

    when(matchRepository.findByRoundNoAndSessionId(roundNo, sessionId)).thenReturn(List.of());
    Team t1 =
        new Team(
            UUID.randomUUID(),
            sessionId,
            "T1",
            OffsetDateTime.now(ZoneOffset.UTC),
            List.of("1", "2"));
    Team t2 =
        new Team(
            UUID.randomUUID(),
            sessionId,
            "T2",
            OffsetDateTime.now(ZoneOffset.UTC),
            List.of("3", "4"));
    Team t3 =
        new Team(
            UUID.randomUUID(),
            sessionId,
            "T3",
            OffsetDateTime.now(ZoneOffset.UTC),
            List.of("5", "6"));
    when(teamRepository.findBySessionId(sessionId)).thenReturn(List.of(t1, t2, t3));

    assertThatThrownBy(() -> matchUseCase.autoMatch(sessionId, roundNo))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("power of two");
  }

  /** Verifies that two teams with no round seed produce a final resolved by walkover. */
  @Test
  void autoMatch_round1_withTwoTeams_andNullSeed_shouldCreateFinalMatch() {
    UUID sessionId = UUID.randomUUID();
    int roundNo = 1;

    when(matchRepository.findByRoundNoAndSessionId(roundNo, sessionId)).thenReturn(List.of());
    UUID t1Id = UUID.randomUUID();
    UUID t2Id = UUID.randomUUID();
    Team t1 =
        new Team(t1Id, sessionId, "T1", OffsetDateTime.now(ZoneOffset.UTC), List.of("1", "2"));
    Team t2 =
        new Team(t2Id, sessionId, "T2", OffsetDateTime.now(ZoneOffset.UTC), List.of("3", "4"));
    when(teamRepository.findBySessionId(sessionId)).thenReturn(List.of(t1, t2));

    Round round = new Round();
    round.setSeed(null);
    when(roundRepository.findBySessionIdAndRoundNo(sessionId, roundNo))
        .thenReturn(Optional.of(round));

    Submission sub1 = createSubmission(t1Id, roundNo);
    when(submissionRepository.findByRoundNo(roundNo)).thenReturn(List.of(sub1));

    List<UUID> matchIds = matchUseCase.autoMatch(sessionId, roundNo);

    assertThat(matchIds).hasSize(1);
    ArgumentCaptor<List<Match>> captor = ArgumentCaptor.forClass(List.class);
    verify(matchRepository).saveAll(captor.capture());
    List<Match> created = captor.getValue();
    assertThat(created.get(0).getMatchType()).isEqualTo(MatchType.FINAL);
    assertThat(created.get(0).getBracketPosition()).isEqualTo(0);
    assertThat(created.get(0).getStatus()).isEqualTo(MatchStatus.COMPLETED);
    assertThat(created.get(0).getWinnerTeam()).isEqualTo(t1Id);
    verify(eliminationRepository).save(any(Elimination.class));
  }

  @Test
  void createMatch_shouldSaveNewMatch() {
    UUID teamA = UUID.randomUUID();
    UUID teamB = UUID.randomUUID();

    matchUseCase.createMatch(teamA, teamB, 1, UUID.randomUUID());

    verify(matchRepository).save(any(Match.class));
  }

  /** Verifies that running a match persists its simulation result and events. */
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
    when(matchRepository.findByRoundNoAndSessionId(roundNo, sessionId)).thenReturn(List.of(match));

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

  /** Verifies that running a semifinal preserves the losing team for third place. */
  @Test
  void runMatch_shouldNotEliminateLoser_whenSemifinalMatch() {
    UUID matchId = UUID.randomUUID();
    UUID sessionId = UUID.randomUUID();
    UUID teamA = UUID.randomUUID();
    UUID teamB = UUID.randomUUID();
    Integer roundNo = 1;

    Match match =
        MatchMother.aMatch(matchId, sessionId, teamA, teamB, roundNo, MatchStatus.PENDING);
    match.setMatchType(MatchType.KNOCKOUT);

    Match match2 =
        MatchMother.aMatch(
            UUID.randomUUID(),
            sessionId,
            UUID.randomUUID(),
            UUID.randomUUID(),
            roundNo,
            MatchStatus.PENDING);
    match2.setMatchType(MatchType.KNOCKOUT);

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
    when(matchRepository.findByRoundNoAndSessionId(roundNo, sessionId))
        .thenReturn(List.of(match, match2));

    SimulationResult simResult =
        new SimulationResult(teamA, 10, List.of(mock(MatchEventSnapshot.class)));
    when(battleEngineUseCase.simulate(any(), any(), any(), anyLong(), any(), any(), any()))
        .thenReturn(simResult);

    Hero mockHero = mock(Hero.class);
    when(rosterUseCase.getHeroes(any())).thenReturn(List.of(mockHero));
    when(mockHero.id()).thenReturn(1);
    when(fatigueUseCase.applyFatigue(any(UUID.class), anyList(), anyInt()))
        .thenReturn(List.of(mockHero));
    when(matchRepository.findPendingMatches(roundNo, sessionId)).thenReturn(List.of(match2));

    String result = matchUseCase.runMatch(matchId);

    assertThat(result).isEqualTo(teamA.toString());
    verify(eliminationRepository, times(0)).save(any(Elimination.class));
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

  @Test
  void runMatch_shouldThrowException_whenMatchNotFound_ensureLambda() {
    UUID matchId = UUID.randomUUID();
    when(matchRepository.findById(matchId)).thenReturn(Optional.empty());

    try {
      matchUseCase.runMatch(matchId);
      fail("expected exception");
    } catch (IllegalArgumentException e) {
      assertThat(e.getMessage()).isEqualTo("Match not found");
    }
  }

  @Test
  void getMatchEvents_shouldReturnSnapshots() {
    UUID matchId = UUID.randomUUID();
    MatchEvent event = mock(MatchEvent.class);

    when(matchEventRepository.findByMatchId(matchId)).thenReturn(List.of(event));
    List<MatchEventSnapshot> result = matchUseCase.getMatchEvents(matchId);
    assertThat(result).isNotNull();
  }

  @Test
  void autoMatch_subsequentRound_shouldThrowException_whenTournamentAlreadyFinishedWithFinal() {
    UUID sessionId = UUID.randomUUID();
    int roundNo = 3;
    Match finalMatch =
        Match.builder()
            .matchId(UUID.randomUUID())
            .sessionId(sessionId)
            .roundNo(2)
            .teamA(UUID.randomUUID())
            .teamB(UUID.randomUUID())
            .matchType(MatchType.FINAL)
            .status(MatchStatus.COMPLETED)
            .winnerTeam(UUID.randomUUID())
            .build();
    Match thirdPlace =
        Match.builder()
            .matchId(UUID.randomUUID())
            .sessionId(sessionId)
            .roundNo(2)
            .teamA(UUID.randomUUID())
            .teamB(UUID.randomUUID())
            .matchType(MatchType.THIRD_PLACE)
            .status(MatchStatus.COMPLETED)
            .winnerTeam(UUID.randomUUID())
            .build();

    when(matchRepository.findByRoundNoAndSessionId(roundNo, sessionId)).thenReturn(List.of());
    when(matchRepository.findByRoundNoAndSessionId(2, sessionId))
        .thenReturn(List.of(finalMatch, thirdPlace));

    assertThatThrownBy(() -> matchUseCase.autoMatch(sessionId, roundNo))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("Tournament already completed at round 2");
  }

  @Test
  void autoMatch_subsequentRound_shouldSucceed_whenPreviousRoundHasCancelledMatch() {
    UUID sessionId = UUID.randomUUID();
    int roundNo = 2;
    UUID team1 = UUID.randomUUID();
    UUID team2 = UUID.randomUUID();
    UUID team3 = UUID.randomUUID();
    UUID team4 = UUID.randomUUID();

    Match m1 =
        Match.builder()
            .matchId(UUID.randomUUID())
            .sessionId(sessionId)
            .roundNo(1)
            .teamA(team1)
            .teamB(team2)
            .matchType(MatchType.KNOCKOUT)
            .status(MatchStatus.COMPLETED)
            .winnerTeam(team1)
            .bracketPosition(0)
            .build();

    Match m2 =
        Match.builder()
            .matchId(UUID.randomUUID())
            .sessionId(sessionId)
            .roundNo(1)
            .teamA(team3)
            .teamB(team4)
            .matchType(MatchType.KNOCKOUT)
            .status(MatchStatus.CANCELLED)
            .bracketPosition(1)
            .build();

    when(matchRepository.findByRoundNoAndSessionId(roundNo, sessionId)).thenReturn(List.of());
    when(matchRepository.findByRoundNoAndSessionId(1, sessionId)).thenReturn(List.of(m1, m2));
    when(submissionRepository.findByRoundNo(roundNo)).thenReturn(List.of());

    List<UUID> createdIds = matchUseCase.autoMatch(sessionId, roundNo);

    assertThat(createdIds).hasSize(2);
  }
}
