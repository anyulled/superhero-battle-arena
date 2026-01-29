package org.barcelonajug.superherobattlearena.application.usecase;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.barcelonajug.superherobattlearena.application.port.out.TeamRepositoryPort;
import org.barcelonajug.superherobattlearena.domain.Team;
import org.springframework.stereotype.Service;

@Service
public class TeamUseCase {

  private final TeamRepositoryPort teamRepository;
  private final SessionUseCase sessionUseCase;

  public TeamUseCase(TeamRepositoryPort teamRepository, SessionUseCase sessionUseCase) {
    this.teamRepository = teamRepository;
    this.sessionUseCase = sessionUseCase;
  }

  public List<Team> getTeams(UUID sessionId) {
    if (sessionId != null) {
      return teamRepository.findBySessionId(sessionId);
    }

    return sessionUseCase
        .getActiveSession()
        .map(session -> teamRepository.findBySessionId(session.getSessionId()))
        .orElse(List.of());
  }

  public UUID registerTeam(String name, List<String> members, UUID sessionId) {
    if (teamRepository.existsByName(name)) {
      throw new IllegalArgumentException("Team name '" + name + "' already exists");
    }

    UUID targetSessionId = sessionId;
    if (targetSessionId == null) {
      var activeSession = sessionUseCase.getActiveSession();
      if (activeSession.isEmpty()) {
        throw new IllegalStateException("No active session found");
      }
      targetSessionId = activeSession.get().getSessionId();
    }

    Team team = new Team(UUID.randomUUID(), targetSessionId, name, OffsetDateTime.now(), members);
    teamRepository.save(team);

    return team.teamId();
  }
}
