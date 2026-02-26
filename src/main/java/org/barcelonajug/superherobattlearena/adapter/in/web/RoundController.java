package org.barcelonajug.superherobattlearena.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import org.barcelonajug.superherobattlearena.application.usecase.RoundUseCase;
import org.barcelonajug.superherobattlearena.domain.Round;
import org.barcelonajug.superherobattlearena.domain.Submission;
import org.barcelonajug.superherobattlearena.domain.json.DraftSubmission;
import org.barcelonajug.superherobattlearena.domain.json.RoundSpec;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rounds")
@Tag(name = "Round Management", description = "Endpoints for managing rounds and submissions")
public class RoundController {

  private final RoundUseCase roundUseCase;

  public RoundController(RoundUseCase roundUseCase) {
    this.roundUseCase = roundUseCase;
  }

  @Operation(
      summary = "List rounds for a session",
      description = "Retrieves all rounds for a specific session.")
  @ApiResponse(
      responseCode = "200",
      description = "List of rounds retrieved successfully",
      content = @Content(schema = @Schema(implementation = Round.class)))
  @GetMapping
  public ResponseEntity<List<Round>> listRounds(
      @Parameter(description = "Session ID", required = true) @RequestParam UUID sessionId) {
    return ResponseEntity.ok(roundUseCase.listRounds(sessionId));
  }

  @Operation(
      summary = "Get round details",
      description = "Retrieves the specifications and constraints for a specific round.")
  @ApiResponse(
      responseCode = "200",
      description = "Round details retrieved successfully",
      content = @Content(schema = @Schema(implementation = RoundSpec.class)))
  @ApiResponse(responseCode = "404", description = "Round not found")
  @GetMapping("/{roundNo}")
  public ResponseEntity<RoundSpec> getRound(
      @Parameter(description = "Number of the round", required = true) @PathVariable
          Integer roundNo,
      @Parameter(description = "Session ID", required = true) @RequestParam UUID sessionId) {
    return roundUseCase
        .getRoundSpec(roundNo, sessionId)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @Operation(
      summary = "Submit a team for a round",
      description = "Submits a draft team configuration for a specific round.")
  @ApiResponse(responseCode = "200", description = "Team submitted successfully")
  @ApiResponse(responseCode = "400", description = "Invalid submission data")
  @ApiResponse(responseCode = "404", description = "Round not found")
  @PostMapping("/{roundNo}/submit")
  public ResponseEntity<Void> submitTeam(
      @Parameter(description = "Number of the round", required = true) @PathVariable
          Integer roundNo,
      @Parameter(description = "ID of the team", required = true) @RequestParam UUID teamId,
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
              description = "Draft submission containing hero IDs and strategy",
              required = true,
              content = @Content(schema = @Schema(implementation = DraftSubmission.class)))
          @RequestBody
          DraftSubmission draft) {
    roundUseCase.submitTeam(roundNo, teamId, draft);
    return ResponseEntity.ok().build();
  }

  @Operation(
      summary = "Get team submission",
      description = "Retrieves the submitted team configuration for a specific round.")
  @ApiResponse(
      responseCode = "200",
      description = "Submission retrieved successfully",
      content = @Content(schema = @Schema(implementation = DraftSubmission.class)))
  @ApiResponse(responseCode = "404", description = "Submission or round not found")
  @GetMapping("/{roundNo}/submission")
  public ResponseEntity<DraftSubmission> getSubmission(
      @Parameter(description = "Number of the round", required = true) @PathVariable
          Integer roundNo,
      @Parameter(description = "ID of the team", required = true) @RequestParam UUID teamId) {
    return roundUseCase
        .getSubmission(roundNo, teamId)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @Operation(
      summary = "Get all submissions for a round",
      description = "Retrieves all team submissions for a specific round.")
  @ApiResponse(
      responseCode = "200",
      description = "List of submissions retrieved successfully",
      content = @Content(schema = @Schema(implementation = Submission.class)))
  @GetMapping("/{roundNo}/submissions")
  public ResponseEntity<List<Submission>> getSubmissions(
      @Parameter(description = "Number of the round", required = true) @PathVariable
          Integer roundNo,
      @Parameter(description = "Optional session ID for validation") @RequestParam(required = false)
          UUID sessionId) {
    return ResponseEntity.ok(roundUseCase.getSubmissions(roundNo, sessionId));
  }
}
