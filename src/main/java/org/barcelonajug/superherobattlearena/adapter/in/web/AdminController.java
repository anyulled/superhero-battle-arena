package org.barcelonajug.superherobattlearena.adapter.in.web;

import static java.util.Objects.requireNonNull;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.barcelonajug.superherobattlearena.adapter.in.web.dto.BatchSimulationResult;
import org.barcelonajug.superherobattlearena.adapter.in.web.dto.CreateRoundRequest;
import org.barcelonajug.superherobattlearena.application.usecase.AdminUseCase;
import org.barcelonajug.superherobattlearena.domain.Session;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Management", description = "Privileged endpoints for tournament administration")
public class AdminController {

  private final AdminUseCase adminUseCase;

  public AdminController(AdminUseCase adminUseCase) {
    this.adminUseCase = adminUseCase;
  }

  /** Start a new tournament session */
  @Operation(
      summary = "Start a new session",
      description = "Initializes a new tournament session, optionally with a predefined UUID.")
  @ApiResponse(
      responseCode = "200",
      description = "Session started",
      content = @Content(schema = @Schema(implementation = UUID.class)))
  @PostMapping("/sessions/start")
  public ResponseEntity<UUID> startSession(
      @Parameter(description = "Optional custom session ID") @RequestParam(required = false)
          UUID sessionId) {
    return ResponseEntity.ok(adminUseCase.startSession(sessionId));
  }

  /** List all tournament sessions */
  @Operation(
      summary = "List all sessions",
      description = "Retrieves a list of all current and past tournament sessions.")
  @ApiResponse(responseCode = "200", description = "List of sessions retrieved")
  @GetMapping("/sessions")
  public ResponseEntity<List<Session>> listSessions() {
    return ResponseEntity.ok(adminUseCase.listSessions());
  }

  /** Create a new round with custom constraints */
  @Operation(
      summary = "Create a custom round",
      description = "Creates a new round with specified constraints and specifications.")
  @ApiResponse(
      responseCode = "200",
      description = "Round created",
      content = @Content(schema = @Schema(implementation = Integer.class)))
  @PostMapping("/rounds/create")
  public ResponseEntity<Integer> createRound(
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
              description = "Details for the new round",
              required = true)
          @RequestBody
          CreateRoundRequest request) {
    return ResponseEntity.ok(
        adminUseCase.createRound(request.sessionId(), request.roundNo(), request.spec()));
  }

  /** Automatically match teams for a round */
  @Operation(
      summary = "Auto-match teams",
      description = "Automatically creates matches for all registered teams in a round.")
  @ApiResponse(responseCode = "200", description = "Matches created")
  @PostMapping("/matches/auto-match")
  public ResponseEntity<List<UUID>> autoMatch(
      @Parameter(description = "ID of the session", required = true) @RequestParam UUID sessionId,
      @Parameter(description = "Number of the round", required = true) @RequestParam
          Integer roundNo) {
    return ResponseEntity.ok(adminUseCase.autoMatch(sessionId, roundNo));
  }

  /**
   * Run all pending matches for a round.
   *
   * @param roundNo the round number
   * @param sessionId the session ID (optional)
   * @return the batch simulation result
   */
  @Operation(
      summary = "Run all matches in round",
      description = "Simulates all pending matches for a specific round in a batch.")
  @ApiResponse(
      responseCode = "200",
      description = "Simulation batch completed",
      content = @Content(schema = @Schema(implementation = BatchSimulationResult.class)))
  @PostMapping("/matches/run-all")
  @SuppressWarnings("unchecked")
  public ResponseEntity<BatchSimulationResult> runAllBattles(
      @Parameter(description = "Number of the round", required = true) @RequestParam
          Integer roundNo,
      @Parameter(description = "Optional session ID") @RequestParam(required = false)
          UUID sessionId) {
    Map<String, Object> result = adminUseCase.runAllBattles(roundNo, sessionId);

    BatchSimulationResult batchResult =
        new BatchSimulationResult(
            requireNonNull((List<UUID>) result.get("matchIds")),
            requireNonNull((Map<UUID, UUID>) result.get("winners")),
            requireNonNull((Integer) result.get("total")),
            requireNonNull((Integer) result.get("successCount")));

    return ResponseEntity.ok(batchResult);
  }
}
