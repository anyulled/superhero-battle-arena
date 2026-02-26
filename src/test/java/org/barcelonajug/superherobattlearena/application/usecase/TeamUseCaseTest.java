package org.barcelonajug.superherobattlearena.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.barcelonajug.superherobattlearena.application.port.out.TeamRepositoryPort;
import org.barcelonajug.superherobattlearena.domain.Session;
import org.barcelonajug.superherobattlearena.domain.Team;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TeamUseCaseTest {

  private TeamRepositoryPort teamRepository;
  private SessionUseCase sessionUseCase;
  private TeamUseCase teamUseCase;

  @BeforeEach
  void setUp() {
    teamRepository = mock(TeamRepositoryPort.class);
    sessionUseCase = mock(SessionUseCase.class);
    teamUseCase = new TeamUseCase(teamRepository, sessionUseCase);
  }

  @Test
  void shouldGetTeamsByExplicitSessionId() {
    UUID sessionId = UUID.randomUUID();
    List<Team> teams = List.of(createTeam(sessionId));
    when(teamRepository.findBySessionId(sessionId)).thenReturn(teams);

    List<Team> result = teamUseCase.getTeams(sessionId);

    assertThat(result).isEqualTo(teams);
  }

  @Test
  void shouldGetTeamsByActiveSessionWhenIdIsNull() {
    UUID sessionId = UUID.randomUUID();
    Session session = new Session(sessionId, OffsetDateTime.now(ZoneOffset.UTC), true);
    List<Team> teams = List.of(createTeam(sessionId));

    when(sessionUseCase.getActiveSession()).thenReturn(Optional.of(session));
    when(teamRepository.findBySessionId(sessionId)).thenReturn(teams);

    List<Team> result = teamUseCase.getTeams(null);

    assertThat(result).isEqualTo(teams);
  }

  @Test
  void shouldReturnEmptyListWhenNoActiveSession() {
    when(sessionUseCase.getActiveSession()).thenReturn(Optional.empty());

    List<Team> result = teamUseCase.getTeams(null);

    assertThat(result).isEmpty();
  }

  @Test
  void shouldRegisterTeamWithExplicitSessionId() {
    UUID sessionId = UUID.randomUUID();
    String name = "New Team";
    List<String> members = List.of("1", "2", "3");

    when(teamRepository.existsByName(name)).thenReturn(false);

    UUID teamId = teamUseCase.registerTeam(name, members, sessionId);

    assertThat(teamId).isNotNull();
    verify(teamRepository).save(any(Team.class));
  }

  @Test
  void shouldRegisterTeamWithActiveSessionWhenIdIsNull() {
    UUID sessionId = UUID.randomUUID();
    Session session = new Session(sessionId, OffsetDateTime.now(ZoneOffset.UTC), true);
    String name = "New Team";
    List<String> members = List.of("1", "2", "3");

    when(teamRepository.existsByName(name)).thenReturn(false);
    when(sessionUseCase.getActiveSession()).thenReturn(Optional.of(session));

    UUID teamId = teamUseCase.registerTeam(name, members, null);

    assertThat(teamId).isNotNull();
    verify(teamRepository).save(any(Team.class));
  }

  @Test
  void shouldThrowExceptionWhenRegisteringWithExistingName() {
    String name = "Existing Team";
    List<String> emptyList = List.of();
    UUID sessionId = UUID.randomUUID();
    when(teamRepository.existsByName(name)).thenReturn(true);

    assertThatThrownBy(() -> teamUseCase.registerTeam(name, emptyList, sessionId))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("already exists");
  }

  @Test
  void shouldThrowExceptionWhenNoActiveSessionDuringRegistration() {
    String name = "New Team";
    List<String> emptyList = List.of();
    when(teamRepository.existsByName(any())).thenReturn(false);
    when(sessionUseCase.getActiveSession()).thenReturn(Optional.empty());

    assertThatThrownBy(() -> teamUseCase.registerTeam(name, emptyList, null))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("No active session found");
  }

  private Team createTeam(UUID sessionId) {
    return new Team(
        UUID.randomUUID(),
        sessionId,
        "Team Name",
        OffsetDateTime.now(ZoneId.systemDefault()),
        List.of("1", "2"));
  }
}
