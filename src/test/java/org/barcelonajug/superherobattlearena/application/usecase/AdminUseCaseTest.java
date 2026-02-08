package org.barcelonajug.superherobattlearena.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.barcelonajug.superherobattlearena.application.port.out.MatchEventRepositoryPort;
import org.barcelonajug.superherobattlearena.application.port.out.MatchRepositoryPort;
import org.barcelonajug.superherobattlearena.application.port.out.RoundRepositoryPort;
import org.barcelonajug.superherobattlearena.application.port.out.SessionRepositoryPort;
import org.barcelonajug.superherobattlearena.application.port.out.SubmissionRepositoryPort;
import org.barcelonajug.superherobattlearena.domain.Hero;
import org.barcelonajug.superherobattlearena.domain.Match;
import org.barcelonajug.superherobattlearena.domain.MatchStatus;
import org.barcelonajug.superherobattlearena.domain.Round;
import org.barcelonajug.superherobattlearena.domain.RoundStatus;
import org.barcelonajug.superherobattlearena.domain.Session;
import org.barcelonajug.superherobattlearena.domain.SimulationResult;
import org.barcelonajug.superherobattlearena.domain.Submission;
import org.barcelonajug.superherobattlearena.domain.json.DraftSubmission;
import org.barcelonajug.superherobattlearena.domain.json.RoundSpec;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

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
            fatigueUseCase);
  }

  @Test
  void startSession_shouldCreateNewSessionWithRandomUUID_whenSessionIdIsNull() {
    // Given: no specific session ID provided
    when(sessionRepository.save(any(Session.class))).thenAnswer(i -> i.getArgument(0));

    // When: starting a session with null ID
    UUID resultId = adminUseCase.startSession(null);

    // Then: a new UUID is generated and session is saved
    assertThat(resultId).isNotNull();
    ArgumentCaptor<Session> sessionCaptor = ArgumentCaptor.forClass(Session.class);
    verify(sessionRepository).save(sessionCaptor.capture());
    Session savedSession = sessionCaptor.getValue();
    assertThat(savedSession.getSessionId()).isEqualTo(resultId);
    assertThat(savedSession.isActive()).isTrue();
    assertThat(savedSession.getCreatedAt()).isNotNull();
  }

  @Test
  void startSession_shouldCreateNewSessionWithProvidedUUID_whenSessionIdIsProvided() {
    // Given: a specific session ID
    UUID providedId = UUID.randomUUID();
    when(sessionRepository.save(any(Session.class))).thenAnswer(i -> i.getArgument(0));

    // When: starting a session with provided ID
    UUID resultId = adminUseCase.startSession(providedId);

    // Then: the provided UUID is used
    assertThat(resultId).isEqualTo(providedId);
    ArgumentCaptor<Session> sessionCaptor = ArgumentCaptor.forClass(Session.class);
    verify(sessionRepository).save(sessionCaptor.capture());
    Session savedSession = sessionCaptor.getValue();
    assertThat(savedSession.getSessionId()).isEqualTo(providedId);
    assertThat(savedSession.isActive()).isTrue();
  }

  @Test
  void listSessions_shouldReturnAllSessions() {
    // Given: multiple sessions exist
    List<Session> expectedSessions =
        List.of(
            new Session(UUID.randomUUID(), OffsetDateTime.now(), true),
            new Session(UUID.randomUUID(), OffsetDateTime.now(), false));
    when(sessionRepository.findAll()).thenReturn(expectedSessions);

    // When: listing all sessions
    List<Session> result = adminUseCase.listSessions();

    // Then: all sessions are returned
    assertThat(result).isEqualTo(expectedSessions);
    verify(sessionRepository).findAll();
  }

  @Test
  void createRound_shouldCreateRoundSuccessfully_whenSessionIsActiveAndValid() {
    // Given: an active session exists
    UUID sessionId = UUID.randomUUID();
    Session activeSession = new Session(sessionId, OffsetDateTime.now(), true);
    when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(activeSession));
    when(roundRepository.save(any(Round.class))).thenAnswer(i -> i.getArgument(0));

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

    // When: creating a round
    Integer resultRoundNo = adminUseCase.createRound(sessionId, roundNo, spec);

    // Then: round is created with correct properties
    assertThat(resultRoundNo).isEqualTo(roundNo);
    ArgumentCaptor<Round> roundCaptor = ArgumentCaptor.forClass(Round.class);
    verify(roundRepository).save(roundCaptor.capture());
    Round savedRound = roundCaptor.getValue();
    assertThat(savedRound.getRoundNo()).isEqualTo(roundNo);
    assertThat(savedRound.getSessionId()).isEqualTo(sessionId);
    assertThat(savedRound.getStatus()).isEqualTo(RoundStatus.OPEN);
    assertThat(savedRound.getSpecJson()).isEqualTo(spec);
    assertThat(savedRound.getSeed()).isNotNull();
  }

  @Test
  void createRound_shouldThrowException_whenSessionDoesNotExist() {
    // Given: session does not exist
    UUID sessionId = UUID.randomUUID();
    when(sessionRepository.findById(sessionId)).thenReturn(Optional.empty());

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

    // When/Then: creating a round throws exception
    assertThatThrownBy(() -> adminUseCase.createRound(sessionId, roundNo, spec))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("does not exist or is not active");

    verify(roundRepository, never()).save(any(Round.class));
  }

  @Test
  void createRound_shouldThrowException_whenSessionIsNotActive() {
    // Given: session exists but is not active
    UUID sessionId = UUID.randomUUID();
    Session inactiveSession = new Session(sessionId, OffsetDateTime.now(), false);
    when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(inactiveSession));

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

    // When/Then: creating a round throws exception
    assertThatThrownBy(() -> adminUseCase.createRound(sessionId, roundNo, spec))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("does not exist or is not active");

    verify(roundRepository, never()).save(any(Round.class));
  }

  @Test
  void autoMatch_shouldDelegateToMatchUseCase() {
    // Given
    UUID sessionId = UUID.randomUUID();
    Integer roundNo = 1;
    List<UUID> expectedMatchIds = List.of(UUID.randomUUID(), UUID.randomUUID());
    when(matchUseCase.autoMatch(sessionId, roundNo)).thenReturn(expectedMatchIds);

    // When
    List<UUID> result = adminUseCase.autoMatch(sessionId, roundNo);

    // Then
    assertThat(result).isEqualTo(expectedMatchIds);
    verify(matchUseCase).autoMatch(sessionId, roundNo);
  }

  @Test
  void createMatch_shouldDelegateToMatchUseCase() {
    // Given
    UUID teamA = UUID.randomUUID();
    UUID teamB = UUID.randomUUID();
    Integer roundNo = 1;
    UUID sessionId = UUID.randomUUID();
    UUID expectedMatchId = UUID.randomUUID();
    when(matchUseCase.createMatch(teamA, teamB, roundNo, sessionId)).thenReturn(expectedMatchId);

    // When
    UUID result = adminUseCase.createMatch(teamA, teamB, roundNo, sessionId);

    // Then
    assertThat(result).isEqualTo(expectedMatchId);
    verify(matchUseCase).createMatch(teamA, teamB, roundNo, sessionId);
  }

  @Test
  void runMatch_shouldDelegateToMatchUseCase() {
    // Given
    UUID matchId = UUID.randomUUID();
    String expectedResult = UUID.randomUUID().toString();
    when(matchUseCase.runMatch(matchId)).thenReturn(expectedResult);

    // When
    String result = adminUseCase.runMatch(matchId);

    // Then
    assertThat(result).isEqualTo(expectedResult);
    verify(matchUseCase).runMatch(matchId);
  }

  @Test
  void runAllBattles_shouldSimulateAllPendingMatches() {
    // Given: 2 pending matches with submissions
    Integer roundNo = 1;
    UUID sessionId = UUID.randomUUID();

    UUID teamA1 = UUID.randomUUID();
    UUID teamB1 = UUID.randomUUID();
    UUID teamA2 = UUID.randomUUID();
    UUID teamB2 = UUID.randomUUID();

    Match match1 =
        Match.builder()
            .matchId(UUID.randomUUID())
            .teamA(teamA1)
            .teamB(teamB1)
            .roundNo(roundNo)
            .status(MatchStatus.PENDING)
            .build();

    Match match2 =
        Match.builder()
            .matchId(UUID.randomUUID())
            .teamA(teamA2)
            .teamB(teamB2)
            .roundNo(roundNo)
            .status(MatchStatus.PENDING)
            .build();

    when(matchRepository.findPendingMatches(roundNo, sessionId))
        .thenReturn(List.of(match1, match2));

    Round round = new Round();
    round.setRoundNo(roundNo);
    round.setSeed(12345L);
    round.setSpecJson(
        new RoundSpec(
            "Test Round",
            5,
            100,
            Collections.emptyMap(),
            Collections.emptyMap(),
            Collections.emptyList(),
            Collections.emptyMap(),
            "ARENA_1"));
    when(roundRepository.findById(roundNo)).thenReturn(Optional.of(round));

    // Mock submissions
    DraftSubmission draft1 = new DraftSubmission(List.of(1, 2, 3, 4, 5), "Strategy A");
    DraftSubmission draft2 = new DraftSubmission(List.of(6, 7, 8, 9, 10), "Strategy B");
    DraftSubmission draft3 = new DraftSubmission(List.of(11, 12, 13, 14, 15), "Strategy C");
    DraftSubmission draft4 = new DraftSubmission(List.of(16, 17, 18, 19, 20), "Strategy D");

    Submission submission1 = createSubmission(teamA1, roundNo, draft1);
    Submission submission2 = createSubmission(teamB1, roundNo, draft2);
    Submission submission3 = createSubmission(teamA2, roundNo, draft3);
    Submission submission4 = createSubmission(teamB2, roundNo, draft4);

    when(submissionRepository.findByTeamIdAndRoundNo(teamA1, roundNo))
        .thenReturn(Optional.of(submission1));
    when(submissionRepository.findByTeamIdAndRoundNo(teamB1, roundNo))
        .thenReturn(Optional.of(submission2));
    when(submissionRepository.findByTeamIdAndRoundNo(teamA2, roundNo))
        .thenReturn(Optional.of(submission3));
    when(submissionRepository.findByTeamIdAndRoundNo(teamB2, roundNo))
        .thenReturn(Optional.of(submission4));

    // Mock heroes
    for (int i = 1; i <= 20; i++) {
      Hero hero = createHero(i);
      when(rosterUseCase.getHero(i)).thenReturn(Optional.of(hero));
      when(fatigueUseCase.applyFatigue(any(UUID.class), eq(hero), eq(roundNo))).thenReturn(hero);
    }

    // Mock battle simulation
    when(battleEngineUseCase.simulate(
            any(UUID.class),
            any(List.class),
            any(List.class),
            any(Long.class),
            any(UUID.class),
            any(UUID.class),
            any(RoundSpec.class)))
        .thenReturn(new SimulationResult(teamA1, 10, new ArrayList<>()));

    when(matchRepository.save(any(Match.class))).thenAnswer(i -> i.getArgument(0));

    // When: running all battles
    Map<String, Object> result = adminUseCase.runAllBattles(roundNo, sessionId);

    // Then: both matches are simulated
    assertThat(result).containsKeys("matchIds", "winners", "total", "successCount");
    @SuppressWarnings("unchecked")
    List<UUID> matchIds = (List<UUID>) result.get("matchIds");
    assertThat(matchIds).hasSize(2);
    assertThat(result.get("total")).isEqualTo(2);
    assertThat(result.get("successCount")).isEqualTo(2);

    verify(matchRepository, times(2)).save(any(Match.class));
    verify(battleEngineUseCase, times(2))
        .simulate(
            any(UUID.class),
            any(List.class),
            any(List.class),
            any(Long.class),
            any(UUID.class),
            any(UUID.class),
            any(RoundSpec.class));
  }

  @Test
  void runAllBattles_shouldSkipMatchesWithMissingSubmissions() {
    // Given: 1 pending match with only one submission
    Integer roundNo = 1;
    UUID sessionId = UUID.randomUUID();

    UUID teamA = UUID.randomUUID();
    UUID teamB = UUID.randomUUID();

    Match match =
        Match.builder()
            .matchId(UUID.randomUUID())
            .teamA(teamA)
            .teamB(teamB)
            .roundNo(roundNo)
            .status(MatchStatus.PENDING)
            .build();

    when(matchRepository.findPendingMatches(roundNo, sessionId)).thenReturn(List.of(match));

    Round round = new Round();
    round.setRoundNo(roundNo);
    round.setSeed(12345L);
    round.setSpecJson(
        new RoundSpec(
            "Test Round",
            5,
            100,
            Collections.emptyMap(),
            Collections.emptyMap(),
            Collections.emptyList(),
            Collections.emptyMap(),
            "ARENA_1"));
    when(roundRepository.findById(roundNo)).thenReturn(Optional.of(round));

    // Only teamA has submission
    DraftSubmission draft = new DraftSubmission(List.of(1, 2, 3, 4, 5), "Strategy A");
    Submission submission = createSubmission(teamA, roundNo, draft);
    when(submissionRepository.findByTeamIdAndRoundNo(teamA, roundNo))
        .thenReturn(Optional.of(submission));
    when(submissionRepository.findByTeamIdAndRoundNo(teamB, roundNo))
        .thenReturn(Optional.empty());

    // When: running all battles
    Map<String, Object> result = adminUseCase.runAllBattles(roundNo, sessionId);

    // Then: match is skipped
    assertThat(result.get("total")).isEqualTo(1);
    assertThat(result.get("successCount")).isEqualTo(0);

    verify(matchRepository, never()).save(any(Match.class));
    verify(battleEngineUseCase, never())
        .simulate(
            any(UUID.class),
            any(List.class),
            any(List.class),
            any(Long.class),
            any(UUID.class),
            any(UUID.class),
            any(RoundSpec.class));
  }

  @Test
  void runAllBattles_shouldHandleSimulationErrors() {
    // Given: 1 pending match that will fail during simulation
    Integer roundNo = 1;
    UUID sessionId = UUID.randomUUID();

    UUID teamA = UUID.randomUUID();
    UUID teamB = UUID.randomUUID();

    Match match =
        Match.builder()
            .matchId(UUID.randomUUID())
            .teamA(teamA)
            .teamB(teamB)
            .roundNo(roundNo)
            .status(MatchStatus.PENDING)
            .build();

    when(matchRepository.findPendingMatches(roundNo, sessionId)).thenReturn(List.of(match));

    Round round = new Round();
    round.setRoundNo(roundNo);
    round.setSeed(12345L);
    round.setSpecJson(
        new RoundSpec(
            "Test Round",
            5,
            100,
            Collections.emptyMap(),
            Collections.emptyMap(),
            Collections.emptyList(),
            Collections.emptyMap(),
            "ARENA_1"));
    when(roundRepository.findById(roundNo)).thenReturn(Optional.of(round));

    // Mock submissions
    DraftSubmission draft1 = new DraftSubmission(List.of(1, 2, 3, 4, 5), "Strategy A");
    DraftSubmission draft2 = new DraftSubmission(List.of(6, 7, 8, 9, 10), "Strategy B");

    Submission submission1 = createSubmission(teamA, roundNo, draft1);
    Submission submission2 = createSubmission(teamB, roundNo, draft2);

    when(submissionRepository.findByTeamIdAndRoundNo(teamA, roundNo))
        .thenReturn(Optional.of(submission1));
    when(submissionRepository.findByTeamIdAndRoundNo(teamB, roundNo))
        .thenReturn(Optional.of(submission2));

    // Mock heroes
    for (int i = 1; i <= 10; i++) {
      Hero hero = createHero(i);
      when(rosterUseCase.getHero(i)).thenReturn(Optional.of(hero));
      when(fatigueUseCase.applyFatigue(any(UUID.class), eq(hero), eq(roundNo))).thenReturn(hero);
    }

    // Battle engine throws exception
    when(battleEngineUseCase.simulate(
            any(UUID.class),
            any(List.class),
            any(List.class),
            any(Long.class),
            any(UUID.class),
            any(UUID.class),
            any(RoundSpec.class)))
        .thenThrow(new RuntimeException("Simulation failed"));

    // When: running all battles
    Map<String, Object> result = adminUseCase.runAllBattles(roundNo, sessionId);

    // Then: error is handled gracefully
    assertThat(result.get("total")).isEqualTo(1);
    assertThat(result.get("successCount")).isEqualTo(0);
  }

  @Test
  void runAllBattles_shouldThrowException_whenRoundNotFound() {
    // Given: round does not exist
    Integer roundNo = 1;
    UUID sessionId = UUID.randomUUID();

    when(matchRepository.findPendingMatches(roundNo, sessionId)).thenReturn(new ArrayList<>());
    when(roundRepository.findById(roundNo)).thenReturn(Optional.empty());

    // When/Then: throws exception
    assertThatThrownBy(() -> adminUseCase.runAllBattles(roundNo, sessionId))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Round not found");
  }

  // Helper methods
  private Submission createSubmission(UUID teamId, Integer roundNo, DraftSubmission draft) {
    Submission submission = new Submission();
    submission.setTeamId(teamId);
    submission.setRoundNo(roundNo);
    submission.setSubmissionJson(draft);
    submission.setAccepted(true);
    return submission;
  }

  private Hero createHero(int id) {
    return Hero.builder()
        .id(id)
        .name("Hero " + id)
        .slug("hero-" + id)
        .powerstats(
            Hero.PowerStats.builder()
                .durability(50)
                .strength(100)
                .power(100)
                .speed(75)
                .intelligence(80)
                .combat(90)
                .build())
        .role("Fighter")
        .cost(10)
        .build();
  }
}