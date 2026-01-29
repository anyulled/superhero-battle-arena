package org.barcelonajug.superherobattlearena.adapter.in.web;

import java.util.UUID;
import org.barcelonajug.superherobattlearena.application.port.out.RoundRepositoryPort;
import org.barcelonajug.superherobattlearena.application.port.out.SubmissionRepositoryPort;
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
public class RoundController {

  private final RoundRepositoryPort roundRepository;
  private final SubmissionRepositoryPort submissionRepository;

  public RoundController(
      RoundRepositoryPort roundRepository, SubmissionRepositoryPort submissionRepository) {
    this.roundRepository = roundRepository;
    this.submissionRepository = submissionRepository;
  }

  @PostMapping
  public ResponseEntity<Integer> createRound(
      @RequestParam UUID sessionId, @RequestParam Integer roundNo) {
    // Simple logic: create a round if not exists
    // In real app, we check if session exists.

    Round round = new Round();
    round.setRoundNo(roundNo);
    round.setSessionId(sessionId);
    round.setSeed(System.currentTimeMillis()); // Random seed
    round.setStatus(org.barcelonajug.superherobattlearena.domain.RoundStatus.OPEN);

    // Ensure default spec
    RoundSpec spec =
        new RoundSpec(
            "Default Round",
            5,
            100,
            java.util.Collections.emptyMap(),
            java.util.Collections.emptyMap(),
            java.util.Collections.emptyList(),
            java.util.Collections.emptyMap(),
            "ARENA_1");
    round.setSpecJson(spec);

    roundRepository.save(round);
    return ResponseEntity.ok(round.getRoundNo());
  }

  @GetMapping("/{roundNo}")
  public ResponseEntity<RoundSpec> getRound(@PathVariable Integer roundNo) {
    return roundRepository
        .findById(roundNo)
        .map(Round::getSpecJson)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @PostMapping("/{roundNo}/submit")
  public ResponseEntity<Void> submitTeam(
      @PathVariable Integer roundNo,
      @RequestParam UUID teamId,
      @RequestBody DraftSubmission draft) {
    if (submissionRepository.findByTeamIdAndRoundNo(teamId, roundNo).isPresent()) {
      throw new IllegalStateException("Team " + teamId + " already submitted for round " + roundNo);
    }

    // Naive validation
    if (draft.heroIds().size() != 5) {
      throw new IllegalArgumentException("Team must have exactly 5 heroes");
    }

    Submission submission = new Submission();
    submission.setTeamId(teamId);
    submission.setRoundNo(roundNo);
    submission.setSubmissionJson(draft);
    submission.setAccepted(true); // Auto-accept for now
    submission.setSubmittedAt(java.time.OffsetDateTime.now());

    submissionRepository.save(submission);
    return ResponseEntity.ok().build();
  }

  @GetMapping("/{roundNo}/submission")
  public ResponseEntity<DraftSubmission> getSubmission(
      @PathVariable Integer roundNo, @RequestParam UUID teamId) {
    return submissionRepository
        .findByTeamIdAndRoundNo(teamId, roundNo)
        .map(Submission::getSubmissionJson)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }
}
