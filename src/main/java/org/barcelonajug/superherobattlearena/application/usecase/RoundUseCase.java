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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

@Service
public class RoundUseCase {

  private static final Logger log = LoggerFactory.getLogger(RoundUseCase.class);

  private final RoundRepositoryPort roundRepository;
  private final SubmissionRepositoryPort submissionRepository;

  public RoundUseCase(
      RoundRepositoryPort roundRepository, SubmissionRepositoryPort submissionRepository) {
    this.roundRepository = roundRepository;
    this.submissionRepository = submissionRepository;
  }

  public Integer createRound(UUID sessionId, Integer roundNo) {
    MDC.put("sessionId", sessionId.toString());
    MDC.put("roundNo", roundNo.toString());

    try {
      log.info("Creating round - sessionId={}, roundNo={}", sessionId, roundNo);

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
      log.info("Round created successfully - roundNo={}, seed={}", roundNo, round.getSeed());
      return round.getRoundNo();
    } finally {
      MDC.remove("sessionId");
      MDC.remove("roundNo");
    }
  }

  public Optional<RoundSpec> getRoundSpec(Integer roundNo) {
    return roundRepository.findById(roundNo).map(Round::getSpecJson);
  }

  public void submitTeam(Integer roundNo, UUID teamId, DraftSubmission draft) {
    MDC.put("roundNo", roundNo.toString());
    MDC.put("teamId", teamId.toString());

    try {
      log.info(
          "Submitting team - teamId={}, roundNo={}, heroes={}",
          teamId,
          roundNo,
          draft.heroIds().size());

      if (submissionRepository.findByTeamIdAndRoundNo(teamId, roundNo).isPresent()) {
        log.warn("Team already submitted - teamId={}, roundNo={}", teamId, roundNo);
        throw new IllegalStateException(
            "Team " + teamId + " already submitted for round " + roundNo);
      }

      if (draft.heroIds().size() != 5) {
        log.error(
            "Invalid team size - teamId={}, roundNo={}, size={}",
            teamId,
            roundNo,
            draft.heroIds().size());
        throw new IllegalArgumentException("Team must have exactly 5 heroes");
      }

      Submission submission = new Submission();
      submission.setTeamId(teamId);
      submission.setRoundNo(roundNo);
      submission.setSubmissionJson(draft);
      submission.setAccepted(true);
      submission.setSubmittedAt(OffsetDateTime.now());

      submissionRepository.save(submission);
      log.info("Team submission successful - teamId={}, roundNo={}", teamId, roundNo);
    } catch (Exception e) {
      log.error("Team submission failed - teamId={}, roundNo={}", teamId, roundNo, e);
      throw e;
    } finally {
      MDC.remove("roundNo");
      MDC.remove("teamId");
    }
  }

  public Optional<DraftSubmission> getSubmission(Integer roundNo, UUID teamId) {
    return submissionRepository
        .findByTeamIdAndRoundNo(teamId, roundNo)
        .map(Submission::getSubmissionJson);
  }

  public java.util.List<DraftSubmission> getSubmissions(Integer roundNo, UUID sessionId) {
    Optional<Round> round = roundRepository.findById(roundNo);
    if (round.isPresent() && sessionId != null && !sessionId.equals(round.get().getSessionId())) {
      log.warn(
          "Requesting submissions for round {} with mismatched session {}. Round belongs to session {}",
          roundNo,
          sessionId,
          round.get().getSessionId());
      return java.util.Collections.emptyList();
    }
    return submissionRepository.findByRoundNo(roundNo).stream()
        .map(Submission::getSubmissionJson)
        .toList();
  }

  public java.util.List<Round> listRounds(UUID sessionId) {
    return roundRepository.findBySessionId(sessionId);
  }
}
