package org.barcelonajug.superherobattlearena.adapter.in.web;

import java.util.UUID;
import org.barcelonajug.superherobattlearena.application.usecase.RoundUseCase;
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
public class RoundController {

  private final RoundUseCase roundUseCase;

  public RoundController(RoundUseCase roundUseCase) {
    this.roundUseCase = roundUseCase;
  }

  @PostMapping
  public ResponseEntity<Integer> createRound(
      @RequestParam UUID sessionId, @RequestParam Integer roundNo) {
    return ResponseEntity.ok(roundUseCase.createRound(sessionId, roundNo));
  }

  @GetMapping("/{roundNo}")
  public ResponseEntity<RoundSpec> getRound(@PathVariable Integer roundNo) {
    return roundUseCase
        .getRoundSpec(roundNo)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @PostMapping("/{roundNo}/submit")
  public ResponseEntity<Void> submitTeam(
      @PathVariable Integer roundNo,
      @RequestParam UUID teamId,
      @RequestBody DraftSubmission draft) {
    roundUseCase.submitTeam(roundNo, teamId, draft);
    return ResponseEntity.ok().build();
  }

  @GetMapping("/{roundNo}/submission")
  public ResponseEntity<DraftSubmission> getSubmission(
      @PathVariable Integer roundNo, @RequestParam UUID teamId) {
    return roundUseCase
        .getSubmission(roundNo, teamId)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }
}
