package org.barcelonajug.superherobattlearena.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.barcelonajug.superherobattlearena.application.usecase.SessionUseCase;
import org.barcelonajug.superherobattlearena.domain.Session;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sessions")
@Tag(name = "Session Management", description = "Endpoints for managing tournament sessions")
public class SessionController {

  private final SessionUseCase sessionUseCase;

  public SessionController(SessionUseCase sessionUseCase) {
    this.sessionUseCase = sessionUseCase;
  }

  @Operation(
      summary = "Create a new session",
      description = "Initializes a new tournament session.")
  @ApiResponse(
      responseCode = "200",
      description = "Session created",
      content = @Content(schema = @Schema(implementation = Session.class)))
  @PostMapping
  public ResponseEntity<Session> createSession() {
    return ResponseEntity.ok(sessionUseCase.createSession());
  }

  @Operation(summary = "List all sessions", description = "Returns all tournament sessions.")
  @ApiResponse(
      responseCode = "200",
      description = "Sessions retrieved",
      content = @Content(schema = @Schema(implementation = Session.class)))
  @GetMapping
  public ResponseEntity<List<Session>> listSessions() {
    return ResponseEntity.ok(sessionUseCase.listSessions());
  }

  @Operation(
      summary = "Get active session",
      description = "Retrieves the currently active tournament session.")
  @ApiResponse(
      responseCode = "200",
      description = "Active session found",
      content = @Content(schema = @Schema(implementation = Session.class)))
  @ApiResponse(responseCode = "404", description = "No active session found")
  @GetMapping("/active")
  public ResponseEntity<Session> getActiveSession() {
    return sessionUseCase
        .getActiveSession()
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }
}
