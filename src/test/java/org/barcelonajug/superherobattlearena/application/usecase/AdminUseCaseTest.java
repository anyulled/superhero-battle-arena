package org.barcelonajug.superherobattlearena.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.barcelonajug.superherobattlearena.application.port.out.HeroUsageRepositoryPort;
import org.barcelonajug.superherobattlearena.application.port.out.MatchEventRepositoryPort;
import org.barcelonajug.superherobattlearena.application.port.out.MatchRepositoryPort;
import org.barcelonajug.superherobattlearena.application.port.out.RoundRepositoryPort;
import org.barcelonajug.superherobattlearena.application.port.out.SessionRepositoryPort;
import org.barcelonajug.superherobattlearena.application.port.out.SubmissionRepositoryPort;
import org.barcelonajug.superherobattlearena.application.port.out.TeamRepositoryPort;
import org.barcelonajug.superherobattlearena.domain.Hero;
import org.barcelonajug.superherobattlearena.domain.Match;
import org.barcelonajug.superherobattlearena.domain.MatchStatus;
import org.barcelonajug.superherobattlearena.domain.Round;
import org.barcelonajug.superherobattlearena.domain.Session;
import org.barcelonajug.superherobattlearena.domain.SimulationResult;
import org.barcelonajug.superherobattlearena.domain.Submission;
import org.barcelonajug.superherobattlearena.domain.json.DraftSubmission;
import org.barcelonajug.superherobattlearena.domain.json.MatchResult;
import org.barcelonajug.superherobattlearena.domain.json.RoundSpec;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

class AdminUseCaseTest {

  private SessionRepositoryPort sessionRepository;
  private RoundRepositoryPort roundRepository;
  private MatchUseCase matchUseCase;
  private MatchRepositoryPort matchRepository;
  private SubmissionRepositoryPort submissionRepository;
  private MatchEventRepositoryPort matchEventRepository;
  private BattleEngineUseCase battleEngineUseCase;
  private RosterUseCase rosterUseCase;
  private FatigueUseCase fatigueUseCase;
  private TeamRepositoryPort teamRepository;
  private HeroUsageRepositoryPort heroUsageRepository;
  private AdminUseCase adminUseCase;

  @BeforeEach
  void setUp() {
    sessionRepository = mock(SessionRepositoryPort.class);
    roundRepository = mock(RoundRepositoryPort.class);
    matchUseCase = mock(MatchUseCase.class);
    matchRepository = mock(MatchRepositoryPort.class);
    submissionRepository = mock(SubmissionRepositoryPort.class);
    matchEventRepository = mock(MatchEventRepositoryPort.class);
    battleEngineUseCase = mock(BattleEngineUseCase.class);
    rosterUseCase = mock(RosterUseCase.class);
    fatigueUseCase = mock(FatigueUseCase.class);
    teamRepository = mock(TeamRepositoryPort.class);
    heroUsageRepository = mock(HeroUsageRepositoryPort.class);

    adminUseCase =
        new AdminUseCase(
            sessionRepository,
            roundRepository,
            matchUseCase,
            matchRepository,
            submissionRepository,
            matchEventRepository,
            battleEngineUseCase,
            rosterUseCase,
            fatigueUseCase,
            teamRepository,
            heroUsageRepository);
  }

  @Test
  void resetDatabase_shouldDeleteAllDataInCorrectOrder() {
    // When
    adminUseCase.resetDatabase();

    // Then
    InOrder inOrder =
        inOrder(
            matchEventRepository,
            matchRepository,
            heroUsageRepository,
            submissionRepository,
            roundRepository,
            teamRepository,
            sessionRepository);

    inOrder.verify(matchEventRepository).deleteAll();
    inOrder.verify(matchRepository).deleteAll();
    inOrder.verify(heroUsageRepository).deleteAll();
    inOrder.verify(submissionRepository).deleteAll();
    inOrder.verify(roundRepository).deleteAll();
    inOrder.verify(teamRepository).deleteAll();
    inOrder.verify(sessionRepository).deleteAll();
  }

  @Test
  void startSession_shouldSaveNewSession() {
    UUID sessionId = UUID.randomUUID();
    adminUseCase.startSession(sessionId);
    verify(sessionRepository).save(any(Session.class));
  }

  @Test
  @SuppressWarnings("NullAway")
  void startSession_shouldGenerateIdWhenNull() {
    adminUseCase.startSession(null);
    verify(sessionRepository).save(any(Session.class));
  }

  @Test
  void listSessions_shouldReturnAllSessions() {
    adminUseCase.listSessions();
    verify(sessionRepository).findAll();
  }

  @Test
  void createRound_shouldSaveNewRoundWithCalculatedNumber() {
    UUID sessionId = UUID.randomUUID();
    RoundSpec spec =
        new RoundSpec("Desc", 5, 100, Map.of(), Map.of(), List.of(), Map.of(), "ARENA");
    when(roundRepository.findMaxRoundNo(sessionId)).thenReturn(Optional.of(1));
    when(sessionRepository.findById(sessionId))
        .thenReturn(Optional.of(new Session(sessionId, OffsetDateTime.now(ZoneOffset.UTC), true)));

    adminUseCase.createRound(sessionId, spec);

    ArgumentCaptor<Round> captor = ArgumentCaptor.forClass(Round.class);
    verify(roundRepository).save(captor.capture());
    assertThat(captor.getValue().getRoundNo()).isEqualTo(2);
  }

  @Test
  void createRound_shouldThrowException_whenSessionNotFound() {
    UUID sessionId = UUID.randomUUID();
    when(sessionRepository.findById(sessionId)).thenReturn(Optional.empty());
    assertThatThrownBy(() -> adminUseCase.createRound(sessionId, mock(RoundSpec.class)))
        .isInstanceOf(IllegalStateException.class);
  }

  @Test
  void autoMatch_shouldDelegateToMatchUseCase() {
    adminUseCase.autoMatch(UUID.randomUUID(), 1);
    verify(matchUseCase).autoMatch(any(), anyInt());
  }

  @Test
  void createMatch_shouldDelegateToMatchUseCase() {
    adminUseCase.createMatch(UUID.randomUUID(), UUID.randomUUID(), 1, UUID.randomUUID());
    verify(matchUseCase).createMatch(any(), any(), anyInt(), any());
  }

  @Test
  void runMatch_shouldDelegateToMatchUseCase() {
    adminUseCase.runMatch(UUID.randomUUID());
    verify(matchUseCase).runMatch(any());
  }

  @Test
  void runAllBattles_shouldReturnEmpty_whenNoSessionFound() {
    Map<String, Object> result = adminUseCase.runAllBattles(1, null);
    assertThat(result).containsEntry("total", 0);
  }

  @Test
  void runAllBattles_shouldExecutePendingMatches() {
    UUID sessionId = UUID.randomUUID();
    UUID teamA = UUID.randomUUID();
    UUID teamB = UUID.randomUUID();
    UUID matchId = UUID.randomUUID();
    Match match =
        Match.builder()
            .matchId(matchId)
            .sessionId(sessionId)
            .teamA(teamA)
            .teamB(teamB)
            .roundNo(1)
            .status(MatchStatus.PENDING)
            .build();

    when(matchRepository.findAll()).thenReturn(List.of(match));
    Round round = new Round();
    round.setSpecJson(
        new RoundSpec("Desc", 5, 100, Map.of(), Map.of(), List.of(), Map.of(), "ARENA"));
    when(roundRepository.findBySessionIdAndRoundNo(sessionId, 1)).thenReturn(Optional.of(round));

    Submission subA =
        Submission.builder().submissionJson(new DraftSubmission(List.of(1), "Sub")).build();
    Submission subB =
        Submission.builder().submissionJson(new DraftSubmission(List.of(2), "Sub")).build();
    when(submissionRepository.findByTeamIdAndRoundNo(teamA, 1)).thenReturn(Optional.of(subA));
    when(submissionRepository.findByTeamIdAndRoundNo(teamB, 1)).thenReturn(Optional.of(subB));

    Hero heroA = Hero.builder().id(1).name("A").slug("a").powerstats(Hero.PowerStats.builder().build()).role("F").build();
    Hero heroB = Hero.builder().id(2).name("B").slug("b").powerstats(Hero.PowerStats.builder().build()).role("F").build();
    when(rosterUseCase.getHeroes(List.of(1))).thenReturn(List.of(heroA));
    when(rosterUseCase.getHeroes(List.of(2))).thenReturn(List.of(heroB));
    when(fatigueUseCase.applyFatigue(any(UUID.class), anyList(), anyInt()))
        .thenAnswer(i -> i.getArgument(1));

    SimulationResult simResult = new SimulationResult(teamA, 10, List.of());
    when(battleEngineUseCase.simulate(any(), any(), any(), anyLong(), any(), any(), any()))
        .thenReturn(simResult);

    adminUseCase.runAllBattles(1, sessionId);

    verify(matchRepository).save(match);
    assertThat(match.getStatus()).isEqualTo(MatchStatus.COMPLETED);
  }

  @Test
  void runAllBattles_shouldHandleMissingSubmissions() {
    UUID sessionId = UUID.randomUUID();
    Match match =
        Match.builder().sessionId(sessionId).roundNo(1).status(MatchStatus.PENDING).build();

    when(matchRepository.findAll()).thenReturn(List.of(match));
    Round round = new Round();
    round.setSpecJson(
        new RoundSpec("Desc", 5, 100, Map.of(), Map.of(), List.of(), Map.of(), "ARENA"));
    when(roundRepository.findBySessionIdAndRoundNo(sessionId, 1)).thenReturn(Optional.of(round));
    when(submissionRepository.findByTeamIdAndRoundNo(any(), anyInt())).thenReturn(Optional.empty());

    Map<String, Object> result = adminUseCase.runAllBattles(1, sessionId);
    assertThat(result).containsEntry("successCount", 0);
  }

  @Test
  void runAllBattles_shouldHandleExceptionDuringSimulation() {
    UUID sessionId = UUID.randomUUID();
    Match match =
        Match.builder().sessionId(sessionId).roundNo(1).status(MatchStatus.PENDING).build();

    when(matchRepository.findAll()).thenReturn(List.of(match));
    // Internal Exception in the loop should be swallowed
    Round round = new Round();
    round.setSpecJson(
        new RoundSpec("Desc", 5, 100, Map.of(), Map.of(), List.of(), Map.of(), "ARENA"));
    when(roundRepository.findBySessionIdAndRoundNo(any(), anyInt())).thenReturn(Optional.of(round));
    when(submissionRepository.findByTeamIdAndRoundNo(any(), anyInt()))
        .thenThrow(new RuntimeException("Sim Error"));

    Map<String, Object> result = adminUseCase.runAllBattles(1, sessionId);
    assertThat(result).containsEntry("successCount", 0);
  }

  @Test
  void runAllBattles_shouldHandleDraw() {
    UUID sessionId = UUID.randomUUID();
    UUID teamA = UUID.randomUUID();
    UUID teamB = UUID.randomUUID();
    UUID matchId = UUID.randomUUID();
    Match match =
        Match.builder()
            .matchId(matchId)
            .sessionId(sessionId)
            .teamA(teamA)
            .teamB(teamB)
            .roundNo(1)
            .status(MatchStatus.PENDING)
            .build();

    when(matchRepository.findAll()).thenReturn(List.of(match));
    Round round = new Round();
    round.setSpecJson(
        new RoundSpec("Desc", 5, 100, Map.of(), Map.of(), List.of(), Map.of(), "ARENA"));
    when(roundRepository.findBySessionIdAndRoundNo(sessionId, 1)).thenReturn(Optional.of(round));

    Submission sub =
        Submission.builder().submissionJson(new DraftSubmission(List.of(1), "Sub")).build();
    when(submissionRepository.findByTeamIdAndRoundNo(any(), anyInt())).thenReturn(Optional.of(sub));
    Hero hero = Hero.builder().id(1).name("H").slug("h").powerstats(Hero.PowerStats.builder().build()).role("F").build();
    when(rosterUseCase.getHeroes(anyList())).thenReturn(List.of(hero));
    when(fatigueUseCase.applyFatigue(any(UUID.class), anyList(), anyInt()))
        .thenAnswer(i -> i.getArgument(1));

    SimulationResult simResult = new SimulationResult(null, 10, List.of());
    when(battleEngineUseCase.simulate(any(), any(), any(), anyLong(), any(), any(), any()))
        .thenReturn(simResult);

    adminUseCase.runAllBattles(1, sessionId);

    MatchResult resultJson = Objects.requireNonNull(match.getResultJson());
    assertThat(resultJson.winner()).isEqualTo("DRAW");
  }

  @Test
  void createRound_shouldThrowException_whenSessionInactive() {
    UUID sessionId = UUID.randomUUID();
    when(sessionRepository.findById(sessionId))
        .thenReturn(Optional.of(new Session(sessionId, OffsetDateTime.now(ZoneOffset.UTC), false)));
    assertThatThrownBy(() -> adminUseCase.createRound(sessionId, mock(RoundSpec.class)))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("not active");
  }

  @Test
  void runAllBattles_shouldInferSessionFromPendingMatches_whenSessionIdIsNull() {
    UUID sessionId = UUID.randomUUID();
    Match match =
        Match.builder().sessionId(sessionId).roundNo(1).status(MatchStatus.PENDING).build();
    when(matchRepository.findAll()).thenReturn(List.of(match));
    // Should find sessionId from match

    Round round = new Round();
    round.setSpecJson(
        new RoundSpec("Desc", 5, 100, Map.of(), Map.of(), List.of(), Map.of(), "ARENA"));
    when(roundRepository.findBySessionIdAndRoundNo(sessionId, 1)).thenReturn(Optional.of(round));

    adminUseCase.runAllBattles(1, null);

    verify(roundRepository).findBySessionIdAndRoundNo(sessionId, 1);
  }

  @Test
  void runAllBattles_shouldThrowException_whenRoundNotFound() {
    UUID sessionId = UUID.randomUUID();
    Match match =
        Match.builder().sessionId(sessionId).roundNo(1).status(MatchStatus.PENDING).build();
    when(matchRepository.findAll()).thenReturn(List.of(match));
    when(roundRepository.findBySessionIdAndRoundNo(sessionId, 1)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> adminUseCase.runAllBattles(1, sessionId))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Round not found");
  }

  @Test
  void runAllBattles_shouldHandleException_whenHeroNotFound() {
    UUID sessionId = UUID.randomUUID();
    UUID teamA = UUID.randomUUID();
    UUID teamB = UUID.randomUUID();
    Match match =
        Match.builder()
            .sessionId(sessionId)
            .teamA(teamA)
            .teamB(teamB)
            .roundNo(1)
            .status(MatchStatus.PENDING)
            .build();
    when(matchRepository.findAll()).thenReturn(List.of(match));

    Round round = new Round();
    round.setSpecJson(
        new RoundSpec("Desc", 5, 100, Map.of(), Map.of(), List.of(), Map.of(), "ARENA"));
    when(roundRepository.findBySessionIdAndRoundNo(sessionId, 1)).thenReturn(Optional.of(round));

    Submission sub =
        Submission.builder().submissionJson(new DraftSubmission(List.of(1), "Sub")).build();
    when(submissionRepository.findByTeamIdAndRoundNo(any(), anyInt())).thenReturn(Optional.of(sub));
    when(rosterUseCase.getHeroes(anyList())).thenReturn(List.of());

    // Should be swallowed by the loop catch block and return 0 success
    Map<String, Object> result = adminUseCase.runAllBattles(1, sessionId);
    assertThat(result).containsEntry("successCount", 0);
  }
}
