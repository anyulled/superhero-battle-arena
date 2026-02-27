package org.barcelonajug.superherobattlearena.application.usecase;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
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
import org.jspecify.annotations.Nullable;
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

  public Optional<RoundSpec> getRoundSpec(Integer roundNo, @Nullable UUID sessionId) {
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
      Team team = teamRepository
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

      RoundSpec spec = round.get().getSpecJson();
      int requiredTeamSize = 5;
      if (spec != null) {
        int teamSize = spec.teamSize();
        if (teamSize > 0) {
          requiredTeamSize = teamSize;
        }
      }
      if (draft.heroIds().size() != requiredTeamSize) {
        log.error(
            "Invalid team size - teamId={}, roundNo={}, size={}, required={}",
            teamId,
            roundNo,
            draft.heroIds().size(),
            requiredTeamSize);
        throw new IllegalArgumentException(
            "Team must have exactly " + requiredTeamSize + " heroes");
      }

      Submission submission = Submission.builder()
          .teamId(teamId)
          .roundNo(roundNo)
          .submissionJson(draft)
          .accepted(true)
          .submittedAt(OffsetDateTime.now(ZoneOffset.UTC))
          .build();

      submissionRepository.save(submission);
      log.info("Team submission successful - teamId={}, roundNo={}", teamId, roundNo);
    } catch (IllegalArgumentException | IllegalStateException e) {
      throw e;
    } catch (Exception e) {
      throw new RuntimeException(
          String.format("Team submission failed - teamId=%s, roundNo=%d", teamId, roundNo), e);
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

  public List<Submission> getSubmissions(Integer roundNo, @Nullable UUID sessionId) {
    if (sessionId == null) {
      log.warn("getSubmissions called without sessionId");
      return Collections.emptyList();
    }

    Optional<Round> round = roundRepository.findBySessionIdAndRoundNo(sessionId, roundNo);
    if (round.isEmpty()) {
      return Collections.emptyList();
    }

    List<Team> sessionTeams = teamRepository.findBySessionId(sessionId);
    Set<UUID> sessionTeamIds = sessionTeams.stream().map(Team::teamId).collect(Collectors.toSet());

    return submissionRepository.findByRoundNo(roundNo).stream()
        .filter(s -> sessionTeamIds.contains(s.getTeamId()))
        .toList();
  }

  public List<Round> listRounds(UUID sessionId) {
    return roundRepository.findBySessionId(sessionId);
  }
}
