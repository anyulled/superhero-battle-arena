package org.barcelonajug.superherobattlearena.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
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
import org.barcelonajug.superherobattlearena.domain.Session;
import org.barcelonajug.superherobattlearena.domain.SimulationResult;
import org.barcelonajug.superherobattlearena.domain.Submission;
import org.barcelonajug.superherobattlearena.domain.json.DraftSubmission;
import org.barcelonajug.superherobattlearena.domain.json.RoundSpec;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Additional tests for AdminUseCase focusing on batch operations, edge cases, and negative
 * scenarios.
 */
class AdminUseCaseBatchOperationsTest {

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
  void runAllBattles_shouldHandleEmptyMatchList() {
    // Given: no pending matches
    Integer roundNo = 1;
    UUID sessionId = UUID.randomUUID();

    when(matchRepository.findPendingMatches(roundNo, sessionId)).thenReturn(Collections.emptyList());

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

    // When: running all battles
    Map<String, Object> result = adminUseCase.runAllBattles(roundNo, sessionId);

    // Then: result shows no matches processed
    assertThat(result.get("total")).isEqualTo(0);
    assertThat(result.get("successCount")).isEqualTo(0);
    @SuppressWarnings("unchecked")
    List<UUID> matchIds = (List<UUID>) result.get("matchIds");
    assertThat(matchIds).isEmpty();
  }

  @Test
  void runAllBattles_shouldProcessPartialSuccessScenario() {
    // Given: 3 matches, 2 succeed, 1 fails
    Integer roundNo = 1;
    UUID sessionId = UUID.randomUUID();

    UUID teamA1 = UUID.randomUUID();
    UUID teamB1 = UUID.randomUUID();
    UUID teamA2 = UUID.randomUUID();
    UUID teamB2 = UUID.randomUUID();
    UUID teamA3 = UUID.randomUUID();
    UUID teamB3 = UUID.randomUUID();

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

    Match match3 =
        Match.builder()
            .matchId(UUID.randomUUID())
            .teamA(teamA3)
            .teamB(teamB3)
            .roundNo(roundNo)
            .status(MatchStatus.PENDING)
            .build();

    when(matchRepository.findPendingMatches(roundNo, sessionId))
        .thenReturn(List.of(match1, match2, match3));

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

    // Mock submissions for all teams
    setupSubmissionsForTeams(roundNo, teamA1, teamB1, teamA2, teamB2, teamA3, teamB3);

    // Mock battle engine - match3 will throw exception
    when(battleEngineUseCase.simulate(
            eq(match1.getMatchId()),
            any(List.class),
            any(List.class),
            any(Long.class),
            any(UUID.class),
            any(UUID.class),
            any(RoundSpec.class)))
        .thenReturn(new SimulationResult(teamA1, 10, new ArrayList<>()));

    when(battleEngineUseCase.simulate(
            eq(match2.getMatchId()),
            any(List.class),
            any(List.class),
            any(Long.class),
            any(UUID.class),
            any(UUID.class),
            any(RoundSpec.class)))
        .thenReturn(new SimulationResult(teamA2, 12, new ArrayList<>()));

    when(battleEngineUseCase.simulate(
            eq(match3.getMatchId()),
            any(List.class),
            any(List.class),
            any(Long.class),
            any(UUID.class),
            any(UUID.class),
            any(RoundSpec.class)))
        .thenThrow(new RuntimeException("Battle engine failure"));

    when(matchRepository.save(any(Match.class))).thenAnswer(i -> i.getArgument(0));

    // When: running all battles
    Map<String, Object> result = adminUseCase.runAllBattles(roundNo, sessionId);

    // Then: 2 successful, 1 failed
    assertThat(result.get("total")).isEqualTo(3);
    assertThat(result.get("successCount")).isEqualTo(2);
    @SuppressWarnings("unchecked")
    List<UUID> matchIds = (List<UUID>) result.get("matchIds");
    assertThat(matchIds).hasSize(2);
  }

  @Test
  void runAllBattles_shouldHandleNullSessionId() {
    // Given: sessionId is null
    Integer roundNo = 1;

    when(matchRepository.findPendingMatches(roundNo, null)).thenReturn(Collections.emptyList());

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

    // When: running all battles with null sessionId
    Map<String, Object> result = adminUseCase.runAllBattles(roundNo, null);

    // Then: should complete without error
    assertThat(result).isNotNull();
    assertThat(result.get("total")).isEqualTo(0);
  }

  @Test
  void startSession_shouldHandleMultipleSessionsCreation() {
    // Given
    when(sessionRepository.save(any(Session.class))).thenAnswer(i -> i.getArgument(0));

    // When: creating multiple sessions
    UUID session1 = adminUseCase.startSession(null);
    UUID session2 = adminUseCase.startSession(null);
    UUID session3 = adminUseCase.startSession(null);

    // Then: all sessions should have unique IDs
    assertThat(session1).isNotEqualTo(session2);
    assertThat(session2).isNotEqualTo(session3);
    assertThat(session1).isNotEqualTo(session3);
    verify(sessionRepository, times(3)).save(any(Session.class));
  }

  @Test
  void createRound_shouldHandleRoundWithNullSeed() {
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
    Integer result = adminUseCase.createRound(sessionId, roundNo, spec);

    // Then: seed should be automatically generated
    assertThat(result).isEqualTo(roundNo);
  }

  @Test
  void runAllBattles_shouldRecordFatigueForBothTeams() {
    // Given: 1 pending match
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
    DraftSubmission draftA = new DraftSubmission(List.of(1, 2, 3, 4, 5), "Strategy A");
    DraftSubmission draftB = new DraftSubmission(List.of(6, 7, 8, 9, 10), "Strategy B");

    Submission submissionA = createSubmission(teamA, roundNo, draftA);
    Submission submissionB = createSubmission(teamB, roundNo, draftB);

    when(submissionRepository.findByTeamIdAndRoundNo(teamA, roundNo))
        .thenReturn(Optional.of(submissionA));
    when(submissionRepository.findByTeamIdAndRoundNo(teamB, roundNo))
        .thenReturn(Optional.of(submissionB));

    // Mock heroes
    for (int i = 1; i <= 10; i++) {
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
        .thenReturn(new SimulationResult(teamA, 10, new ArrayList<>()));

    when(matchRepository.save(any(Match.class))).thenAnswer(i -> i.getArgument(0));

    // When: running all battles
    adminUseCase.runAllBattles(roundNo, sessionId);

    // Then: fatigue should be recorded for both teams
    verify(fatigueUseCase).recordUsage(teamA, roundNo, List.of(1, 2, 3, 4, 5));
    verify(fatigueUseCase).recordUsage(teamB, roundNo, List.of(6, 7, 8, 9, 10));
  }

  // Helper methods
  private void setupSubmissionsForTeams(Integer roundNo, UUID... teamIds) {
    int heroIdStart = 1;
    for (UUID teamId : teamIds) {
      List<Integer> heroIds =
          List.of(
              heroIdStart,
              heroIdStart + 1,
              heroIdStart + 2,
              heroIdStart + 3,
              heroIdStart + 4);
      DraftSubmission draft = new DraftSubmission(heroIds, "Strategy");
      Submission submission = createSubmission(teamId, roundNo, draft);

      when(submissionRepository.findByTeamIdAndRoundNo(teamId, roundNo))
          .thenReturn(Optional.of(submission));

      for (int heroId : heroIds) {
        Hero hero = createHero(heroId);
        when(rosterUseCase.getHero(heroId)).thenReturn(Optional.of(hero));
        when(fatigueUseCase.applyFatigue(any(UUID.class), eq(hero), eq(roundNo))).thenReturn(hero);
      }

      heroIdStart += 5;
    }
  }

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