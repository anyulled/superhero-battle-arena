package org.barcelonajug.superherobattlearena.application.usecase;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;

import org.barcelonajug.superherobattlearena.application.port.out.HeroUsageRepositoryPort;
import org.barcelonajug.superherobattlearena.application.port.out.MatchEventRepositoryPort;
import org.barcelonajug.superherobattlearena.application.port.out.MatchRepositoryPort;
import org.barcelonajug.superherobattlearena.application.port.out.RoundRepositoryPort;
import org.barcelonajug.superherobattlearena.application.port.out.SessionRepositoryPort;
import org.barcelonajug.superherobattlearena.application.port.out.SubmissionRepositoryPort;
import org.barcelonajug.superherobattlearena.application.port.out.TeamRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
}
