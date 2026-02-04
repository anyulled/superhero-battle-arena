package org.barcelonajug.superherobattlearena.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.barcelonajug.superherobattlearena.application.usecase.MatchUseCase;
import org.barcelonajug.superherobattlearena.domain.Match;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/matches")
@Tag(name = "Match Management", description = "Endpoints for creating and simulating matches")
public class MatchController {

  private final MatchUseCase matchUseCase;

  // A simple executor for async simulation (demo purpose)
  private final ExecutorService executor = Executors.newSingleThreadExecutor();

  public MatchController(MatchUseCase matchUseCase) {
    this.matchUseCase = matchUseCase;
  }

  @Operation(
      summary = "Create a match",
      description = "Manually creates a match between two teams.")
  @ApiResponse(
      responseCode = "200",
      description = "Match created",
      content = @Content(schema = @Schema(implementation = UUID.class)))
  @PostMapping("/create")
  public ResponseEntity<UUID> createMatch(
      @Parameter(description = "ID of the first team", required = true) @RequestParam UUID teamA,
      @Parameter(description = "ID of the second team", required = true) @RequestParam UUID teamB,
      @Parameter(description = "Number of the round", example = "1")
          @RequestParam(defaultValue = "1")
          Integer roundNo,
      @Parameter(description = "Optional session ID") @RequestParam(required = false)
          UUID sessionId) {
    return ResponseEntity.ok(matchUseCase.createMatch(teamA, teamB, roundNo, sessionId));
  }

  @Operation(
      summary = "Auto-match teams",
      description = "Automatically creates matches for a specific round based on registered teams.")
  @ApiResponse(responseCode = "200", description = "Matches created")
  @PostMapping("/auto-match")
  public ResponseEntity<List<UUID>> createMatchesForRound(
      @Parameter(description = "Optional session ID") @RequestParam(required = false)
          UUID sessionId,
      @Parameter(description = "Number of the round", required = true) @RequestParam
          Integer roundNo) {
    return ResponseEntity.ok(matchUseCase.autoMatch(sessionId, roundNo));
  }

  @Operation(summary = "Run a match", description = "Simulates a match and returns the winner.")
  @ApiResponse(responseCode = "200", description = "Match completed")
  @PostMapping("/{matchId}/run")
  public ResponseEntity<String> runMatch(
      @Parameter(description = "ID of the match to run", required = true) @PathVariable
          UUID matchId) {
    return ResponseEntity.ok("Match completed. Winner: " + matchUseCase.runMatch(matchId));
  }

  @Operation(
      summary = "Get match events",
      description = "Retrieves a list of all events that occurred during a match.")
  @ApiResponse(responseCode = "200", description = "List of events retrieved")
  @GetMapping("/{matchId}/events")
  public List<org.barcelonajug.superherobattlearena.domain.json.MatchEvent> getEvents(
      @Parameter(description = "ID of the match", required = true) @PathVariable UUID matchId) {
    return matchUseCase.getMatchEvents(matchId);
  }

  @Operation(summary = "List all matches", description = "Retrieves a list of all matches.")
  @GetMapping
  public ResponseEntity<List<Match>> getAllMatches() {
    return ResponseEntity.ok(matchUseCase.getAllMatches());
  }

  @Operation(
      summary = "Get match details",
      description = "Retrieves detailed information about a specific match.")
  @ApiResponse(responseCode = "200", description = "Match details found")
  @ApiResponse(responseCode = "404", description = "Match not found")
  @GetMapping("/{matchId}")
  public ResponseEntity<Match> getMatch(
      @Parameter(description = "ID of the match", required = true) @PathVariable UUID matchId) {
    return matchUseCase
        .getMatch(matchId)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @Operation(
      summary = "Stream match events",
      description = "Streams events for a match using Server-Sent Events (SSE).")
  @GetMapping("/{matchId}/events/stream")
  public SseEmitter streamEvents(
      @Parameter(description = "ID of the match to stream", required = true) @PathVariable
          UUID matchId) {
    SseEmitter emitter = new SseEmitter(600000L); // 10 min timeout
    executor.submit(
        () -> {
          try {
            List<org.barcelonajug.superherobattlearena.domain.MatchEvent> events =
                matchUseCase.getMatchEventEntities(matchId);
            for (org.barcelonajug.superherobattlearena.domain.MatchEvent event : events) {
              emitter.send(event.eventJson());
              Thread.sleep(500);
            }
            emitter.complete();
          } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            emitter.completeWithError(e);
          } catch (Exception e) {
            emitter.completeWithError(e);
          }
        });
    return emitter;
  }
}
