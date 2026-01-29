package org.barcelonajug.superherobattlearena.adapter.in.web;

import java.util.List;
import java.util.UUID;
import org.barcelonajug.superherobattlearena.application.usecase.RosterUseCase;
import org.barcelonajug.superherobattlearena.application.usecase.TeamUseCase;
import org.barcelonajug.superherobattlearena.domain.Hero;
import org.barcelonajug.superherobattlearena.domain.Team;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/teams")
public class TeamController {

  private final TeamUseCase teamUseCase;
  private final RosterUseCase rosterUseCase;

  public TeamController(TeamUseCase teamUseCase, RosterUseCase rosterUseCase) {
    this.teamUseCase = teamUseCase;
    this.rosterUseCase = rosterUseCase;
  }

  @GetMapping("/heroes")
  public List<Hero> getHeroes() {
    return rosterUseCase.getAllHeroes();
  }

  @GetMapping
  public ResponseEntity<List<Team>> getTeams(@RequestParam(required = false) UUID sessionId) {
    return ResponseEntity.ok(teamUseCase.getTeams(sessionId));
  }

  @PostMapping("/register")
  public ResponseEntity<UUID> registerTeam(
      @RequestParam String name,
      @RequestParam List<String> members,
      @RequestParam(required = false) UUID sessionId) {
    return ResponseEntity.ok(teamUseCase.registerTeam(name, members, sessionId));
  }
}
