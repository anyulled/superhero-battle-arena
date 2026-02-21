package org.barcelonajug.superherobattlearena.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Team Management", description = "Endpoints for registering and viewing teams")
public class TeamController {

  private final TeamUseCase teamUseCase;
  private final RosterUseCase rosterUseCase;

  public TeamController(TeamUseCase teamUseCase, RosterUseCase rosterUseCase) {
    this.teamUseCase = teamUseCase;
    this.rosterUseCase = rosterUseCase;
  }

  @Operation(
      summary = "Get available heroes",
      description =
          "Retrieves a list of heroes that can be picked for a team. Supports pagination.")
  @ApiResponse(
      responseCode = "200",
      description = "List of heroes",
      content = @Content(array = @ArraySchema(schema = @Schema(implementation = Hero.class))))
  @GetMapping("/heroes")
  public List<Hero> getHeroes(
      @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
      @Parameter(description = "Number of items per page") @RequestParam(defaultValue = "50")
          int size) {
    return rosterUseCase.getAllHeroes(page, size);
  }

  @Operation(
      summary = "List teams",
      description = "Retrieves a list of teams, optionally filtered by session.")
  @ApiResponse(
      responseCode = "200",
      description = "List of teams",
      content = @Content(array = @ArraySchema(schema = @Schema(implementation = Team.class))))
  @GetMapping
  public ResponseEntity<List<Team>> getTeams(
      @Parameter(description = "Optional session ID to filter teams")
          @RequestParam(required = false)
          UUID sessionId) {
    return ResponseEntity.ok(teamUseCase.getTeams(sessionId));
  }

  @Operation(summary = "Register a team", description = "Registers a new team for the arena.")
  @ApiResponse(
      responseCode = "200",
      description = "Team registered successfully",
      content = @Content(schema = @Schema(implementation = UUID.class)))
  @PostMapping("/register")
  public ResponseEntity<UUID> registerTeam(
      @Parameter(description = "Name of the team", required = true) @RequestParam String name,
      @Parameter(description = "List of hero names/IDs in the team", required = true) @RequestParam
          List<String> members,
      @Parameter(description = "Optional session ID") @RequestParam(required = false)
          UUID sessionId) {
    return ResponseEntity.ok(teamUseCase.registerTeam(name, members, sessionId));
  }
}
