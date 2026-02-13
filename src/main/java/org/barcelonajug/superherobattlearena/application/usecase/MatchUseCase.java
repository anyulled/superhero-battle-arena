package org.barcelonajug.superherobattlearena.application.usecase;

import static org.slf4j.LoggerFactory.getLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.barcelonajug.superherobattlearena.application.port.out.MatchEventRepositoryPort;
import org.barcelonajug.superherobattlearena.application.port.out.MatchRepositoryPort;
import org.barcelonajug.superherobattlearena.application.port.out.RoundRepositoryPort;
import org.barcelonajug.superherobattlearena.application.port.out.SubmissionRepositoryPort;
import org.barcelonajug.superherobattlearena.domain.Hero;
import org.barcelonajug.superherobattlearena.domain.Match;
import org.barcelonajug.superherobattlearena.domain.MatchEvent;
import org.barcelonajug.superherobattlearena.domain.MatchStatus;
import org.barcelonajug.superherobattlearena.domain.Round;
import org.barcelonajug.superherobattlearena.domain.SimulationResult;
import org.barcelonajug.superherobattlearena.domain.Submission;
import org.barcelonajug.superherobattlearena.domain.json.DraftSubmission;
import org.barcelonajug.superherobattlearena.domain.json.MatchResult;
import org.slf4j.Logger;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

@Service
public class MatchUseCase {

  private static final Logger log = getLogger(MatchUseCase.class);

  private final SubmissionRepositoryPort submissionRepository;
  private final MatchRepositoryPort matchRepository;
  private final MatchEventRepositoryPort matchEventRepository;
  private final RoundRepositoryPort roundRepository;
  private final BattleEngineUseCase battleEngineUseCase;
  private final RosterUseCase rosterUseCase;
  private final FatigueUseCase fatigueUseCase;

  public MatchUseCase(
      SubmissionRepositoryPort submissionRepository,
      MatchRepositoryPort matchRepository,
      MatchEventRepositoryPort matchEventRepository,
      RoundRepositoryPort roundRepository,
      BattleEngineUseCase battleEngineUseCase,
      RosterUseCase rosterUseCase,
      FatigueUseCase fatigueUseCase) {
    this.submissionRepository = submissionRepository;
    this.matchRepository = matchRepository;
    this.matchEventRepository = matchEventRepository;
    this.roundRepository = roundRepository;
    this.battleEngineUseCase = battleEngineUseCase;
    this.rosterUseCase = rosterUseCase;
    this.fatigueUseCase = fatigueUseCase;
  }

  public List<UUID> autoMatch(UUID sessionId, Integer roundNo) {
    MDC.put("roundNo", roundNo.toString());
    MDC.put("sessionId", sessionId.toString());

    try {
      log.info("Starting auto-match for round {} in session {}", roundNo, sessionId);

      // Get existing matches for this round and session
      List<Match> existingMatches = matchRepository.findByRoundNoAndSessionId(roundNo, sessionId);
      java.util.Set<UUID> alreadyMatchedTeams = new java.util.HashSet<>();
      for (Match m : existingMatches) {
        alreadyMatchedTeams.add(m.getTeamA());
        alreadyMatchedTeams.add(m.getTeamB());
      }
      log.debug(
          "Found {} existing matches with {} teams already matched",
          existingMatches.size(),
          alreadyMatchedTeams.size());

      // Get all submissions and filter out already-matched teams
      List<Submission> allSubmissions = submissionRepository.findByRoundNo(roundNo);
      // NOTE: submissionRepository.findByRoundNo(roundNo) returns submissions for ALL
      // sessions.
      // Ideally we should filter by team's session, but Submission doesn't have
      // session_id.
      // We rely on auto-match being called per session and checking teams.
      // However, if we pick a team from another session that also submitted to "Round
      // 1",
      // we might match them. THIS IS A POTENTIAL BUG.
      // For now, fixing Round bug is priority.

      List<Submission> unmatchedSubmissions =
          allSubmissions.stream()
              .filter(s -> !alreadyMatchedTeams.contains(s.getTeamId()))
              .toList();
      log.debug(
          "Found {} total submissions, {} unmatched",
          allSubmissions.size(),
          unmatchedSubmissions.size());

      List<UUID> matchIds = new ArrayList<>();

      for (int i = 0; i < unmatchedSubmissions.size() - 1; i += 2) {
        Submission subA = unmatchedSubmissions.get(i);
        Submission subB = unmatchedSubmissions.get(i + 1);

        Match match =
            Match.builder()
                .matchId(UUID.randomUUID())
                .sessionId(sessionId)
                .teamA(subA.getTeamId())
                .teamB(subB.getTeamId())
                .roundNo(roundNo)
                .status(MatchStatus.PENDING)
                .build();

        matchRepository.save(match);
        matchIds.add(match.getMatchId());
        log.debug(
            "Created match {} between teams {} and {}",
            match.getMatchId(),
            subA.getTeamId(),
            subB.getTeamId());
      }

      log.info("Auto-match completed - created {} matches for round {}", matchIds.size(), roundNo);
      return matchIds;
    } finally {
      MDC.remove("roundNo");
      MDC.remove("sessionId");
    }
  }

  public UUID createMatch(UUID teamA, UUID teamB, Integer roundNo, UUID sessionId) {
    Match match =
        Match.builder()
            .matchId(UUID.randomUUID())
            .sessionId(sessionId)
            .teamA(teamA)
            .teamB(teamB)
            .roundNo(roundNo)
            .status(MatchStatus.PENDING)
            .build();
    matchRepository.save(match);
    return match.getMatchId();
  }

  public String runMatch(UUID matchId) {
    long startTime = System.currentTimeMillis();
    MDC.put("matchId", matchId.toString());

    try {
      log.info("Starting match execution - matchId={}", matchId);

      Match match =
          matchRepository
              .findById(matchId)
              .orElseThrow(
                  () -> {
                    log.error("Match not found - matchId={}", matchId);
                    return new IllegalArgumentException("Match not found");
                  });

      MDC.put("roundNo", match.getRoundNo().toString());
      log.debug(
          "Match details - teamA={}, teamB={}, roundNo={}",
          match.getTeamA(),
          match.getTeamB(),
          match.getRoundNo());

      if (match.getStatus() != MatchStatus.PENDING) {
        log.warn("Match already processed - matchId={}, status={}", matchId, match.getStatus());
        throw new IllegalStateException("Match already run or running");
      }

      Optional<Submission> subA =
          submissionRepository.findByTeamIdAndRoundNo(match.getTeamA(), match.getRoundNo());
      Optional<Submission> subB =
          submissionRepository.findByTeamIdAndRoundNo(match.getTeamB(), match.getRoundNo());

      if (subA.isEmpty() || subB.isEmpty()) {
        log.error(
            "Missing submissions - matchId={}, teamA_submitted={}, teamB_submitted={}",
            matchId,
            subA.isPresent(),
            subB.isPresent());
        throw new IllegalStateException("Submissions missing for one or both teams");
      }

      log.debug("Building battle teams with fatigue applied");
      List<Hero> teamAHeroes =
          buildBattleTeam(
              match.getTeamA(),
              java.util.Objects.requireNonNull(subA.get().getSubmissionJson()),
              match.getRoundNo());
      List<Hero> teamBHeroes =
          buildBattleTeam(
              match.getTeamB(),
              java.util.Objects.requireNonNull(subB.get().getSubmissionJson()),
              match.getRoundNo());

      UUID sessionId =
          java.util.Objects.requireNonNull(match.getSessionId(), "Match session ID cannot be null");
      Round round =
          roundRepository
              .findBySessionIdAndRoundNo(sessionId, match.getRoundNo())
              .orElseThrow(
                  () -> new IllegalArgumentException("Round not found: " + match.getRoundNo()));

      log.debug("Delegating to battle engine for simulation");
      SimulationResult result =
          battleEngineUseCase.simulate(
              matchId,
              teamAHeroes,
              teamBHeroes,
              java.util.Objects.requireNonNullElse(round.getSeed(), 0L),
              match.getTeamA(),
              match.getTeamB(),
              java.util.Objects.requireNonNull(round.getSpecJson()));

      match.setStatus(MatchStatus.COMPLETED);
      match.setWinnerTeam(result.winnerTeamId());
      match.setResultJson(
          new MatchResult(
              result.winnerTeamId() != null ? result.winnerTeamId().toString() : "DRAW",
              result.totalTurns(),
              0));
      matchRepository.save(match);

      log.debug("Persisting {} match events", result.events().size());
      List<MatchEvent> eventsToSave =
          java.util.stream.IntStream.range(0, result.events().size())
              .mapToObj(i -> new MatchEvent(matchId, i + 1, result.events().get(i)))
              .toList();
      matchEventRepository.saveAll(eventsToSave);

      log.debug("Recording hero usage for both teams");
      updateHeroUsage(
          match.getTeamA(),
          match.getRoundNo(),
          java.util.Objects.requireNonNull(subA.get().getSubmissionJson()).heroIds());
      updateHeroUsage(
          match.getTeamB(),
          match.getRoundNo(),
          java.util.Objects.requireNonNull(subB.get().getSubmissionJson()).heroIds());

      String resultStr = result.winnerTeamId() != null ? result.winnerTeamId().toString() : "DRAW";
      long duration = System.currentTimeMillis() - startTime;
      log.info(
          "Match execution completed - matchId={}, result={}, duration={}ms",
          matchId,
          resultStr,
          duration);

      return resultStr;
    } catch (Exception e) {
      log.error("Match execution failed - matchId={}", matchId, e);
      throw e;
    } finally {
      MDC.remove("matchId");
      MDC.remove("roundNo");
    }
  }

  public List<org.barcelonajug.superherobattlearena.domain.json.MatchEvent> getMatchEvents(
      UUID matchId) {
    return matchEventRepository.findByMatchId(matchId).stream().map(MatchEvent::eventJson).toList();
  }

  public List<Match> getAllMatches() {
    return matchRepository.findAll();
  }

  public Optional<Match> getMatch(UUID matchId) {
    return matchRepository.findById(matchId);
  }

  public List<MatchEvent> getMatchEventEntities(UUID matchId) {
    return matchEventRepository.findByMatchId(matchId);
  }

  public List<Match> getPendingMatches(Integer roundNo, UUID sessionId) {
    return matchRepository.findPendingMatches(roundNo, sessionId);
  }

  public String runMatchResult(Match match) {
    return runMatch(match.getMatchId());
  }

  public Optional<Submission> getSubmission(UUID teamId, Integer roundNo) {
    return submissionRepository.findByTeamIdAndRoundNo(teamId, roundNo);
  }

  public List<Hero> getBattleTeam(UUID teamId, DraftSubmission submission, int roundNo) {
    return buildBattleTeam(teamId, submission, roundNo);
  }

  public void updateUsage(UUID teamId, int roundNo, List<Integer> heroIds) {
    updateHeroUsage(teamId, roundNo, heroIds);
  }

  private List<Hero> buildBattleTeam(UUID teamId, DraftSubmission submission, int roundNo) {
    List<Hero> fetchedHeroes = rosterUseCase.getHeroes(submission.heroIds());
    Map<Integer, Hero> heroMap =
        fetchedHeroes.stream()
            .collect(Collectors.toMap(Hero::id, Function.identity(), (h1, h2) -> h1));

    List<Hero> orderedHeroes =
        submission.heroIds().stream()
            .map(
                id ->
                    Optional.ofNullable(heroMap.get(id))
                        .orElseThrow(
                            () -> new IllegalArgumentException("Hero not found in roster: " + id)))
            .toList();
    return fatigueUseCase.applyFatigue(teamId, orderedHeroes, roundNo);
  }

  private void updateHeroUsage(UUID teamId, int roundNo, List<Integer> heroIds) {
    fatigueUseCase.recordUsage(teamId, roundNo, heroIds);
  }
}
