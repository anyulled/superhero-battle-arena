package org.barcelonajug.superherobattlearena.adapter.in.web;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.ProblemDetail.forStatusAndDetail;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.barcelonajug.superherobattlearena.application.port.out.TeamRepositoryPort;
import org.barcelonajug.superherobattlearena.application.usecase.RosterService;
import org.barcelonajug.superherobattlearena.application.usecase.SessionService;
import org.barcelonajug.superherobattlearena.domain.Hero;
import org.barcelonajug.superherobattlearena.domain.Team;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/teams")
public class TeamController {

  private final TeamRepositoryPort teamRepository;
  private final RosterService rosterService;
  private final SessionService sessionService;

  public TeamController(
      TeamRepositoryPort teamRepository,
      RosterService rosterService,
      SessionService sessionService) {
    this.teamRepository = teamRepository;
    this.rosterService = rosterService;
    this.sessionService = sessionService;
  }

  @GetMapping("/heroes")
  public List<Hero> getHeroes() {
    return rosterService.getAllHeroes();
  }

  @GetMapping
  public ResponseEntity<List<Team>> getTeams(@RequestParam(required = false) UUID sessionId) {
    if (sessionId != null) {
      return ResponseEntity.ok(teamRepository.findBySessionId(sessionId));
    }

    return sessionService
        .getActiveSession()
        .map(session -> ResponseEntity.ok(teamRepository.findBySessionId(session.getSessionId())))
        .orElse(ResponseEntity.ok(List.of()));
  }

  @PostMapping("/register")
  public ResponseEntity<UUID> registerTeam(
      @RequestParam String name,
      @RequestParam List<String> members,
      @RequestParam(required = false) UUID sessionId) {
    if (teamRepository.existsByName(name)) {
      throw new ErrorResponseException(
          BAD_REQUEST,
          forStatusAndDetail(BAD_REQUEST, "Team name '" + name + "' already exists"),
          null);
    }

    UUID targetSessionId = sessionId;
    if (targetSessionId == null) {
      var activeSession = sessionService.getActiveSession();
      if (activeSession.isEmpty()) {
        throw new ErrorResponseException(
            BAD_REQUEST, forStatusAndDetail(BAD_REQUEST, "No active session found"), null);
      }
      targetSessionId = activeSession.get().getSessionId();
    }

    Team team = new Team(UUID.randomUUID(), targetSessionId, name, OffsetDateTime.now(), members);
    teamRepository.save(team);

    return ResponseEntity.ok(team.teamId());
  }
}
