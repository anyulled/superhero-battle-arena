package org.barcelonajug.superherobattlearena.application.usecase;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import org.barcelonajug.superherobattlearena.application.port.out.RoundRepositoryPort;
import org.barcelonajug.superherobattlearena.application.port.out.SubmissionRepositoryPort;
import org.barcelonajug.superherobattlearena.domain.Round;
import org.barcelonajug.superherobattlearena.domain.RoundStatus;
import org.barcelonajug.superherobattlearena.domain.Submission;
import org.barcelonajug.superherobattlearena.domain.json.DraftSubmission;
import org.barcelonajug.superherobattlearena.domain.json.RoundSpec;
import org.springframework.stereotype.Service;

@Service
public class RoundUseCase {

  private final RoundRepositoryPort roundRepository;
  private final SubmissionRepositoryPort submissionRepository;

  public RoundUseCase(
      RoundRepositoryPort roundRepository, SubmissionRepositoryPort submissionRepository) {
    this.roundRepository = roundRepository;
    this.submissionRepository = submissionRepository;
  }

  public Integer createRound(UUID sessionId, Integer roundNo) {
    Round round = new Round();
    round.setRoundNo(roundNo);
    round.setSessionId(sessionId);
    round.setSeed(System.currentTimeMillis());
    round.setStatus(RoundStatus.OPEN);

    RoundSpec spec =
        new RoundSpec(
            "Default Round",
            5,
            100,
            Collections.emptyMap(),
            Collections.emptyMap(),
            Collections.emptyList(),
            Collections.emptyMap(),
            "ARENA_1");
    round.setSpecJson(spec);

    roundRepository.save(round);
    return round.getRoundNo();
  }

  public Optional<RoundSpec> getRoundSpec(Integer roundNo) {
    return roundRepository.findById(roundNo).map(Round::getSpecJson);
  }

  public void submitTeam(Integer roundNo, UUID teamId, DraftSubmission draft) {
    if (submissionRepository.findByTeamIdAndRoundNo(teamId, roundNo).isPresent()) {
      throw new IllegalStateException("Team " + teamId + " already submitted for round " + roundNo);
    }

    if (draft.heroIds().size() != 5) {
      throw new IllegalArgumentException("Team must have exactly 5 heroes");
    }

    Submission submission = new Submission();
    submission.setTeamId(teamId);
    submission.setRoundNo(roundNo);
    submission.setSubmissionJson(draft);
    submission.setAccepted(true);
    submission.setSubmittedAt(OffsetDateTime.now());

    submissionRepository.save(submission);
  }

  public Optional<DraftSubmission> getSubmission(Integer roundNo, UUID teamId) {
    return submissionRepository
        .findByTeamIdAndRoundNo(teamId, roundNo)
        .map(Submission::getSubmissionJson);
  }
}
