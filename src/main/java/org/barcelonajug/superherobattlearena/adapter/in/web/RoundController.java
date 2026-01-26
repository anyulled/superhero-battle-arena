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

    public ResponseEntity<Void> submitTeam(@PathVariable Integer roundNo, @RequestParam UUID teamId,
            @RequestBody DraftSubmission draft) {
        if (submissionRepository.findByTeamIdAndRoundNo(teamId, roundNo).isPresent()) {
            return ResponseEntity.badRequest().build(); // Already submitted
        }

        // Naive validation
        if (draft.heroIds().size() != 5) {
            return ResponseEntity.badRequest().build();
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
    public ResponseEntity<DraftSubmission> getSubmission(@PathVariable Integer roundNo, @RequestParam UUID teamId) {
        return submissionRepository.findByTeamIdAndRoundNo(teamId, roundNo)
                .map(Submission::getSubmissionJson)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
