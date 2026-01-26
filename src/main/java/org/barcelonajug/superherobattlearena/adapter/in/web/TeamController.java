package org.barcelonajug.superherobattlearena.adapter.in.web;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.barcelonajug.superherobattlearena.application.port.out.TeamRepositoryPort;
import org.barcelonajug.superherobattlearena.domain.Team;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/teams")
public class TeamController {

    private final TeamRepositoryPort teamRepository;

    public TeamController(TeamRepositoryPort teamRepository) {
        this.teamRepository = teamRepository;
    }

    @PostMapping("/register")
    public ResponseEntity<UUID> registerTeam(@RequestParam String name, @RequestParam List<String> members) {
        if (teamRepository.existsByName(name)) {
            return ResponseEntity.badRequest().build();
        }

        Team team = new Team(UUID.randomUUID(), name, OffsetDateTime.now(), members);
        teamRepository.save(team);

        return ResponseEntity.ok(team.teamId());
    }
}
