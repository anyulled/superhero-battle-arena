package org.barcelonajug.superherobattlearena.adapter.in.web;

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
public class MatchController {

  private final MatchUseCase matchUseCase;

  // A simple executor for async simulation (demo purpose)
  private final ExecutorService executor = Executors.newSingleThreadExecutor();

  public MatchController(MatchUseCase matchUseCase) {
    this.matchUseCase = matchUseCase;
  }

  @PostMapping("/create")
  public ResponseEntity<UUID> createMatch(
      @RequestParam UUID teamA,
      @RequestParam UUID teamB,
      @RequestParam(defaultValue = "1") Integer roundNo,
      @RequestParam(required = false) UUID sessionId) {
    return ResponseEntity.ok(matchUseCase.createMatch(teamA, teamB, roundNo, sessionId));
  }

  @PostMapping("/auto-match")
  public ResponseEntity<List<UUID>> createMatchesForRound(
      @RequestParam(required = false) UUID sessionId, @RequestParam Integer roundNo) {
    return ResponseEntity.ok(matchUseCase.autoMatch(sessionId, roundNo));
  }

  @PostMapping("/{matchId}/run")
  public ResponseEntity<String> runMatch(@PathVariable UUID matchId) {
    return ResponseEntity.ok("Match completed. Winner: " + matchUseCase.runMatch(matchId));
  }

  @GetMapping("/{matchId}/events")
  public List<org.barcelonajug.superherobattlearena.domain.json.MatchEvent> getEvents(
      @PathVariable UUID matchId) {
    return matchUseCase.getMatchEvents(matchId);
  }

  @GetMapping
  public ResponseEntity<List<Match>> getAllMatches() {
    return ResponseEntity.ok(matchUseCase.getAllMatches());
  }

  @GetMapping("/{matchId}")
  public ResponseEntity<Match> getMatch(@PathVariable UUID matchId) {
    return matchUseCase
        .getMatch(matchId)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @GetMapping("/{matchId}/events/stream")
  public SseEmitter streamEvents(@PathVariable UUID matchId) {
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
