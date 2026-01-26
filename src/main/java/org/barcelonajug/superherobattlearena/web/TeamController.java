package org.barcelonajug.superherobattlearena.web;

import java.time.OffsetDateTime;
import java.util.UUID;
import org.barcelonajug.superherobattlearena.domain.Team;
import org.barcelonajug.superherobattlearena.repository.TeamRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/teams")
public class TeamController {

    private final TeamRepository teamRepository;

    public TeamController(TeamRepository teamRepository) {
        this.teamRepository = teamRepository;
    }

    @PostMapping("/register")
    public ResponseEntity<UUID> registerTeam(@RequestParam String name) {
        if (teamRepository.existsByName(name)) {
            return ResponseEntity.badRequest().build();
        }

        Team team = new Team(UUID.randomUUID(), name, OffsetDateTime.now());
        teamRepository.save(team);
        return ResponseEntity.ok(team.getTeamId());
    }
}
