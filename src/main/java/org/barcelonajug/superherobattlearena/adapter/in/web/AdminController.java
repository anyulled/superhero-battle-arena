package org.barcelonajug.superherobattlearena.adapter.in.web;

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
public class AdminController {

  private final AdminUseCase adminUseCase;

  public AdminController(AdminUseCase adminUseCase) {
    this.adminUseCase = adminUseCase;
  }

  /** Start a new tournament session */
  @PostMapping("/sessions/start")
  public ResponseEntity<UUID> startSession(@RequestParam(required = false) UUID sessionId) {
    return ResponseEntity.ok(adminUseCase.startSession(sessionId));
  }

  /** List all tournament sessions */
  @GetMapping("/sessions")
  public ResponseEntity<List<Session>> listSessions() {
    return ResponseEntity.ok(adminUseCase.listSessions());
  }

  /** Create a new round with custom constraints */
  @PostMapping("/rounds/create")
  public ResponseEntity<Integer> createRound(@RequestBody CreateRoundRequest request) {
    return ResponseEntity.ok(
        adminUseCase.createRound(request.sessionId(), request.roundNo(), request.spec()));
  }

  /** Automatically match teams for a round */
  @PostMapping("/matches/auto-match")
  public ResponseEntity<List<UUID>> autoMatch(
      @RequestParam UUID sessionId, @RequestParam Integer roundNo) {
    return ResponseEntity.ok(adminUseCase.autoMatch(sessionId, roundNo));
  }

  /**
   * Run all pending matches for a round.
   *
   * @param roundNo   the round number
   * @param sessionId the session ID (optional)
   * @return the batch simulation result
   */
  @PostMapping("/matches/run-all")
  @SuppressWarnings("unchecked")
  public ResponseEntity<BatchSimulationResult> runAllBattles(
      @RequestParam Integer roundNo, @RequestParam(required = false) UUID sessionId) {
    Map<String, Object> result = adminUseCase.runAllBattles(roundNo, sessionId);

    BatchSimulationResult batchResult = new BatchSimulationResult(
        (List<UUID>) result.get("matchIds"),
        (Map<UUID, UUID>) result.get("winners"),
        (Integer) result.get("total"),
        (Integer) result.get("successCount"));

    return ResponseEntity.ok(batchResult);
  }
}
