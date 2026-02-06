package org.barcelonajug.superherobattlearena.application.usecase;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.barcelonajug.superherobattlearena.application.port.out.RoundRepositoryPort;
import org.barcelonajug.superherobattlearena.application.port.out.SubmissionRepositoryPort;
import org.barcelonajug.superherobattlearena.application.port.out.TeamRepositoryPort;
import org.barcelonajug.superherobattlearena.domain.Round;
import org.barcelonajug.superherobattlearena.domain.Submission;
import org.barcelonajug.superherobattlearena.domain.Team;
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
  private final TeamRepositoryPort teamRepository;

  public RoundUseCase(
      RoundRepositoryPort roundRepository,
      SubmissionRepositoryPort submissionRepository,
      TeamRepositoryPort teamRepository) {
    this.roundRepository = roundRepository;
    this.submissionRepository = submissionRepository;
    this.teamRepository = teamRepository;
  }

  public Optional<RoundSpec> getRoundSpec(Integer roundNo, UUID sessionId) {
    if (sessionId == null) {
      log.warn("getRoundSpec called without sessionId for roundNo={}", roundNo);
      return Optional.empty();
    }
    return roundRepository.findBySessionIdAndRoundNo(sessionId, roundNo).map(Round::getSpecJson);
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

      // Validate team exists and identify session
      Team team =
          teamRepository
              .findById(teamId)
              .orElseThrow(() -> new IllegalArgumentException("Team not found: " + teamId));

      // Validate round exists for this session
      // Team is record -> accessors are name(), sessionId()
      Optional<Round> round = roundRepository.findBySessionIdAndRoundNo(team.sessionId(), roundNo);
      if (round.isEmpty()) {
        throw new IllegalArgumentException(
            "Round " + roundNo + " does not exist in session " + team.sessionId());
      }

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
    if (sessionId == null) {
      log.warn("getSubmissions called without sessionId");
      return java.util.Collections.emptyList();
    }

    Optional<Round> round = roundRepository.findBySessionIdAndRoundNo(sessionId, roundNo);
    if (round.isEmpty()) {
      return java.util.Collections.emptyList();
    }

    // Filter submissions to ensure they belong to the correct session
    List<Team> sessionTeams = teamRepository.findBySessionId(sessionId);
    // Team -> teamId()
    Set<UUID> sessionTeamIds = sessionTeams.stream().map(Team::teamId).collect(Collectors.toSet());

    return submissionRepository.findByRoundNo(roundNo).stream()
        .filter(s -> sessionTeamIds.contains(s.getTeamId()))
        .map(Submission::getSubmissionJson)
        .toList();
  }

  public java.util.List<Round> listRounds(UUID sessionId) {
    return roundRepository.findBySessionId(sessionId);
  }
}
