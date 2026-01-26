package org.barcelonajug.superherobattlearena.adapter.in.web;

import java.time.OffsetDateTime;
import java.util.UUID;
import org.barcelonajug.superherobattlearena.application.port.out.RoundRepositoryPort;
import org.barcelonajug.superherobattlearena.application.port.out.SubmissionRepositoryPort;
import org.barcelonajug.superherobattlearena.application.usecase.SubmissionValidator;
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
    private final SubmissionValidator submissionValidator;

    public RoundController(RoundRepositoryPort roundRepository,
            SubmissionRepositoryPort submissionRepository,
            SubmissionValidator submissionValidator) {
        this.roundRepository = roundRepository;
        this.submissionRepository = submissionRepository;
        this.submissionValidator = submissionValidator;
    }

    @GetMapping("/{roundNo}")
    public ResponseEntity<RoundSpec> getRound(@PathVariable Integer roundNo) {
        return roundRepository.findById(roundNo)
                .map(Round::getSpecJson)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{roundNo}/submit")
    public ResponseEntity<String> submitTeam(@PathVariable Integer roundNo,
            @RequestParam UUID teamId,
            @RequestBody DraftSubmission draft) {
        // 1. Get Round
        Round round = roundRepository.findById(roundNo)
                .orElseThrow(() -> new IllegalArgumentException("Round not found"));

        // 2. Validate Deadline (mock check, can be implemented in domain service)
        if (round.getSubmissionDeadline() != null && OffsetDateTime.now().isAfter(round.getSubmissionDeadline())) {
            return ResponseEntity.badRequest().body("Deadline exceeded");
        }

        // 3. Validate Submission Logic
        try {
            submissionValidator.validate(draft, round.getSpecJson());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }

        // 4. Save Submission
        Submission submission = new Submission();
        submission.setTeamId(teamId);
        submission.setRoundNo(roundNo);
        submission.setSubmissionJson(draft);
        submission.setSubmittedAt(OffsetDateTime.now());
        submission.setAccepted(true); // Assuming valid = accepted for now

        submissionRepository.save(submission);

        return ResponseEntity.ok("Submission received");
    }
}
