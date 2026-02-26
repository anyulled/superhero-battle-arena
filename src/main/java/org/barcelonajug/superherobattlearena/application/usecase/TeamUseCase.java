package org.barcelonajug.superherobattlearena.application.usecase;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

import org.barcelonajug.superherobattlearena.application.port.out.TeamRepositoryPort;
import org.barcelonajug.superherobattlearena.domain.Team;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

@Service
public class TeamUseCase {

  private static final Logger log = LoggerFactory.getLogger(TeamUseCase.class);

  private final TeamRepositoryPort teamRepository;
  private final SessionUseCase sessionUseCase;

  public TeamUseCase(TeamRepositoryPort teamRepository, SessionUseCase sessionUseCase) {
    this.teamRepository = teamRepository;
    this.sessionUseCase = sessionUseCase;
  }

  public List<Team> getTeams(@Nullable UUID sessionId) {
    if (sessionId != null) {
      return teamRepository.findBySessionId(sessionId);
    }

    return sessionUseCase
        .getActiveSession()
        .map(session -> teamRepository.findBySessionId(session.getSessionId()))
        .orElse(List.of());
  }

  public UUID registerTeam(String name, List<String> members, @Nullable UUID sessionId) {
    MDC.put("sessionId", sessionId != null ? sessionId.toString() : "auto");

    try {
      log.info("Registering team - name='{}', members={}", name, members.size());

      if (teamRepository.existsByName(name)) {
        log.warn("Team registration failed - name already exists: '{}'", name);
        throw new IllegalArgumentException("Team name '" + name + "' already exists");
      }

      UUID targetSessionId = sessionId;
      if (targetSessionId == null) {
        var activeSession = sessionUseCase.getActiveSession();
        if (activeSession.isEmpty()) {
          log.error("Team registration failed - no active session found");
          throw new IllegalStateException("No active session found");
        }
        targetSessionId = activeSession.get().getSessionId();
        log.debug("Using active session - sessionId={}", targetSessionId);
      }

      Team team = new Team(UUID.randomUUID(), targetSessionId, name,
          OffsetDateTime.now(ZoneId.systemDefault()), members);
      teamRepository.save(team);

      MDC.put("teamId", team.teamId().toString());
      log.info("Team registered successfully - teamId={}, name='{}'", team.teamId(), name);

      return team.teamId();
    } catch (Exception e) {
      log.error("Team registration failed - name='{}'", name, e);
      throw e;
    } finally {
      MDC.remove("sessionId");
      MDC.remove("teamId");
    }
  }
}
