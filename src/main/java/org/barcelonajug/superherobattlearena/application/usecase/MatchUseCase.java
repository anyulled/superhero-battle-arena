package org.barcelonajug.superherobattlearena.application.usecase;

import static java.util.stream.Collectors.toMap;
import static org.slf4j.LoggerFactory.getLogger;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.barcelonajug.superherobattlearena.application.port.out.EliminationRepositoryPort;
import org.barcelonajug.superherobattlearena.application.port.out.MatchEventRepositoryPort;
import org.barcelonajug.superherobattlearena.application.port.out.MatchRepositoryPort;
import org.barcelonajug.superherobattlearena.application.port.out.RoundRepositoryPort;
import org.barcelonajug.superherobattlearena.application.port.out.SubmissionRepositoryPort;
import org.barcelonajug.superherobattlearena.application.port.out.TeamRepositoryPort;
import org.barcelonajug.superherobattlearena.domain.Elimination;
import org.barcelonajug.superherobattlearena.domain.EliminationReason;
import org.barcelonajug.superherobattlearena.domain.Hero;
import org.barcelonajug.superherobattlearena.domain.Match;
import org.barcelonajug.superherobattlearena.domain.MatchEvent;
import org.barcelonajug.superherobattlearena.domain.MatchStatus;
import org.barcelonajug.superherobattlearena.domain.MatchType;
import org.barcelonajug.superherobattlearena.domain.Round;
import org.barcelonajug.superherobattlearena.domain.RoundStatus;
import org.barcelonajug.superherobattlearena.domain.SimulationResult;
import org.barcelonajug.superherobattlearena.domain.Submission;
import org.barcelonajug.superherobattlearena.domain.Team;
import org.barcelonajug.superherobattlearena.domain.json.DraftSubmission;
import org.barcelonajug.superherobattlearena.domain.json.MatchEventSnapshot;
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
  private final TeamRepositoryPort teamRepository;
  private final EliminationRepositoryPort eliminationRepository;

  public MatchUseCase(
      SubmissionRepositoryPort submissionRepository,
      MatchRepositoryPort matchRepository,
      MatchEventRepositoryPort matchEventRepository,
      RoundRepositoryPort roundRepository,
      BattleEngineUseCase battleEngineUseCase,
      RosterUseCase rosterUseCase,
      FatigueUseCase fatigueUseCase,
      TeamRepositoryPort teamRepository,
      EliminationRepositoryPort eliminationRepository) {
    this.submissionRepository = submissionRepository;
    this.matchRepository = matchRepository;
    this.matchEventRepository = matchEventRepository;
    this.roundRepository = roundRepository;
    this.battleEngineUseCase = battleEngineUseCase;
    this.rosterUseCase = rosterUseCase;
    this.fatigueUseCase = fatigueUseCase;
    this.teamRepository = teamRepository;
    this.eliminationRepository = eliminationRepository;
  }

  public List<UUID> autoMatch(UUID sessionId, Integer roundNo) {
    MDC.put("roundNo", roundNo.toString());
    MDC.put("sessionId", sessionId.toString());

    try {
      log.info("Starting auto-match for round {} in session {}", roundNo, sessionId);

      List<Match> existingMatches = matchRepository.findByRoundNoAndSessionId(roundNo, sessionId);
      if (!existingMatches.isEmpty()) {
        log.info(
            "Found {} existing matches for round {} in session {}",
            existingMatches.size(),
            roundNo,
            sessionId);
        return existingMatches.stream().map(Match::getMatchId).toList();
      }

      Round round =
          roundRepository
              .findBySessionIdAndRoundNo(sessionId, roundNo)
              .orElseThrow(
                  () ->
                      new IllegalArgumentException(
                          "Round " + roundNo + " does not exist in session " + sessionId));

      List<Team> sessionTeams = teamRepository.findBySessionId(sessionId);
      Set<UUID> sessionTeamIds =
          sessionTeams.stream().map(Team::teamId).collect(Collectors.toSet());

      List<Submission> roundSubmissions =
          submissionRepository.findByRoundNo(roundNo).stream()
              .filter(s -> sessionTeamIds.isEmpty() || sessionTeamIds.contains(s.getTeamId()))
              .toList();

      Map<UUID, Submission> submissionMap =
          roundSubmissions.stream()
              .collect(toMap(Submission::getTeamId, Function.identity(), (s1, s2) -> s1));

      List<Match> matchesToSave;
      if (roundNo == 1) {
        matchesToSave =
            createRoundOneMatches(sessionId, round, sessionTeams, roundSubmissions, submissionMap);
      } else {
        matchesToSave = createSubsequentRoundMatches(sessionId, roundNo, submissionMap);
      }

      if (!matchesToSave.isEmpty()) {
        matchRepository.saveAll(matchesToSave);
      }

      List<UUID> matchIds = matchesToSave.stream().map(Match::getMatchId).toList();
      log.info("Auto-match completed - created {} matches for round {}", matchIds.size(), roundNo);
      return matchIds;
    } finally {
      MDC.remove("roundNo");
      MDC.remove("sessionId");
    }
  }

  private List<Match> createRoundOneMatches(
      UUID sessionId,
      Round round,
      List<Team> sessionTeams,
      List<Submission> roundSubmissions,
      Map<UUID, Submission> submissionMap) {
    List<UUID> teamIds;
    if (sessionTeams.isEmpty()) {
      teamIds = roundSubmissions.stream().map(Submission::getTeamId).toList();
    } else {
      teamIds = sessionTeams.stream().map(Team::teamId).toList();
    }

    int teamCount = teamIds.size();
    if (teamCount < 2 || (teamCount & (teamCount - 1)) != 0) {
      throw new IllegalArgumentException(
          "Tournament requires a power of two number of teams (e.g. 2, 4, 8, 16)");
    }

    for (UUID teamId : teamIds) {
      if (!submissionMap.containsKey(teamId)) {
        eliminationRepository.save(
            new Elimination(
                teamId,
                sessionId,
                1,
                EliminationReason.NO_SUBMISSION,
                OffsetDateTime.now(ZoneOffset.UTC)));
      }
    }

    List<UUID> shuffled = new ArrayList<>(teamIds);
    shuffled.sort(Comparator.naturalOrder());
    long seed;
    if (round.getSeed() != null) {
      seed = round.getSeed();
    } else {
      seed = 0L;
    }
    Collections.shuffle(shuffled, new Random(seed));

    int numMatches = teamCount / 2;
    MatchType matchType;
    if (numMatches == 1) {
      matchType = MatchType.FINAL;
    } else {
      matchType = MatchType.KNOCKOUT;
    }
    List<Match> matches = new ArrayList<>();

    for (int k = 0; k < numMatches; k++) {
      UUID teamA = shuffled.get(2 * k);
      UUID teamB = shuffled.get(2 * k + 1);
      matches.add(buildMatch(sessionId, 1, teamA, teamB, matchType, k, submissionMap));
    }
    return matches;
  }

  private List<Match> createSubsequentRoundMatches(
      UUID sessionId, int roundNo, Map<UUID, Submission> submissionMap) {
    List<Match> prevMatches = matchRepository.findByRoundNoAndSessionId(roundNo - 1, sessionId);
    if (prevMatches.isEmpty()) {
      throw new IllegalStateException(
          "Previous round matches not found for round " + (roundNo - 1));
    }

    boolean allCompleted =
        prevMatches.stream().allMatch(m -> m.getStatus() == MatchStatus.COMPLETED);
    if (!allCompleted) {
      throw new IllegalStateException(
          "Previous round matches must be completed before auto-matching round " + roundNo);
    }

    List<Match> sortedPrev = new ArrayList<>(prevMatches);
    sortedPrev.sort(
        Comparator.comparingInt(m -> m.getBracketPosition() != null ? m.getBracketPosition() : 0));

    if (sortedPrev.size() == 2) {
      return createFinalsStageMatches(sessionId, roundNo, sortedPrev, submissionMap);
    }

    if (sortedPrev.size() == 1) {
      throw new IllegalStateException("Tournament already completed at round " + (roundNo - 1));
    }

    return createKnockoutMatches(sessionId, roundNo, sortedPrev, submissionMap);
  }

  private List<Match> createFinalsStageMatches(
      UUID sessionId,
      int roundNo,
      List<Match> semifinalMatches,
      Map<UUID, Submission> submissionMap) {
    Match sf0 = semifinalMatches.get(0);
    Match sf1 = semifinalMatches.get(1);

    UUID winner0 = Objects.requireNonNull(sf0.getWinnerTeam());
    UUID winner1 = Objects.requireNonNull(sf1.getWinnerTeam());
    UUID loser0 = sf0.loserTeam().orElseThrow();
    UUID loser1 = sf1.loserTeam().orElseThrow();

    List<UUID> allFinalsTeams = List.of(winner0, winner1, loser0, loser1);
    for (UUID teamId : allFinalsTeams) {
      if (!submissionMap.containsKey(teamId)) {
        eliminationRepository.save(
            new Elimination(
                teamId,
                sessionId,
                roundNo,
                EliminationReason.NO_SUBMISSION,
                OffsetDateTime.now(ZoneOffset.UTC)));
      }
    }

    Match grandFinal =
        buildMatch(sessionId, roundNo, winner0, winner1, MatchType.FINAL, 0, submissionMap);
    Match thirdPlace =
        buildMatch(sessionId, roundNo, loser0, loser1, MatchType.THIRD_PLACE, 1, submissionMap);

    return List.of(grandFinal, thirdPlace);
  }

  private List<Match> createKnockoutMatches(
      UUID sessionId, int roundNo, List<Match> prevMatches, Map<UUID, Submission> submissionMap) {
    int prevCount = prevMatches.size();
    int nextCount = prevCount / 2;
    MatchType matchType = MatchType.KNOCKOUT;

    for (Match prev : prevMatches) {
      UUID winner = Objects.requireNonNull(prev.getWinnerTeam());
      if (!submissionMap.containsKey(winner)) {
        eliminationRepository.save(
            new Elimination(
                winner,
                sessionId,
                roundNo,
                EliminationReason.NO_SUBMISSION,
                OffsetDateTime.now(ZoneOffset.UTC)));
      }
    }

    List<Match> matches = new ArrayList<>();
    for (int k = 0; k < nextCount; k++) {
      UUID teamA = Objects.requireNonNull(prevMatches.get(2 * k).getWinnerTeam());
      UUID teamB = Objects.requireNonNull(prevMatches.get(2 * k + 1).getWinnerTeam());
      matches.add(buildMatch(sessionId, roundNo, teamA, teamB, matchType, k, submissionMap));
    }
    return matches;
  }

  private Match buildMatch(
      UUID sessionId,
      int roundNo,
      UUID teamA,
      UUID teamB,
      MatchType matchType,
      int bracketPosition,
      Map<UUID, Submission> submissionMap) {
    boolean subA = submissionMap.containsKey(teamA);
    boolean subB = submissionMap.containsKey(teamB);

    Match.Builder builder =
        Match.builder()
            .matchId(UUID.randomUUID())
            .sessionId(sessionId)
            .roundNo(roundNo)
            .teamA(teamA)
            .teamB(teamB)
            .matchType(matchType)
            .bracketPosition(bracketPosition);

    if (subA && subB) {
      return builder.status(MatchStatus.PENDING).build();
    }
    if (subA) {
      return builder
          .status(MatchStatus.COMPLETED)
          .winnerTeam(teamA)
          .resultJson(new MatchResult(teamA.toString(), 0, 0))
          .build();
    }
    if (subB) {
      return builder
          .status(MatchStatus.COMPLETED)
          .winnerTeam(teamB)
          .resultJson(new MatchResult(teamB.toString(), 0, 0))
          .build();
    }
    return builder.status(MatchStatus.CANCELLED).build();
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
              Objects.requireNonNull(subA.get().getSubmissionJson()),
              match.getRoundNo());
      List<Hero> teamBHeroes =
          buildBattleTeam(
              match.getTeamB(),
              Objects.requireNonNull(subB.get().getSubmissionJson()),
              match.getRoundNo());

      UUID sessionId =
          Objects.requireNonNull(match.getSessionId(), "Match session ID cannot be null");
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
              Objects.requireNonNullElse(round.getSeed(), 0L),
              match.getTeamA(),
              match.getTeamB(),
              Objects.requireNonNull(round.getSpecJson()));

      match.setStatus(MatchStatus.COMPLETED);
      match.setWinnerTeam(result.winnerTeamId());
      match.setResultJson(
          new MatchResult(
              result.winnerTeamId() != null ? result.winnerTeamId().toString() : "DRAW",
              result.totalTurns(),
              0));
      matchRepository.save(match);
      recordEliminationOnMatchEnd(match);

      log.debug("Persisting {} match events", result.events().size());
      List<MatchEvent> eventsToSave =
          IntStream.range(0, result.events().size())
              .mapToObj(i -> new MatchEvent(matchId, i + 1, result.events().get(i)))
              .toList();
      matchEventRepository.saveAll(eventsToSave);

      log.debug("Recording hero usage for both teams");
      updateHeroUsage(
          match.getTeamA(),
          match.getRoundNo(),
          Objects.requireNonNull(subA.get().getSubmissionJson()).heroIds());
      updateHeroUsage(
          match.getTeamB(),
          match.getRoundNo(),
          Objects.requireNonNull(subB.get().getSubmissionJson()).heroIds());
      String resultStr;

      if (result.winnerTeamId() != null) {
        resultStr = result.winnerTeamId().toString();
      } else {
        resultStr = "DRAW";
      }
      long duration = System.currentTimeMillis() - startTime;

      List<Match> remainingPending =
          matchRepository.findPendingMatches(match.getRoundNo(), sessionId);
      if (remainingPending.isEmpty()) {
        round.setStatus(RoundStatus.CLOSED);
        roundRepository.save(round);
        log.info(
            "Closed round {} for session {} as all matches are completed",
            match.getRoundNo(),
            sessionId);
      }

      log.info(
          "Match execution completed - matchId={}, result={}, duration={}ms",
          matchId,
          resultStr,
          duration);

      return resultStr;
    } finally {
      MDC.remove("matchId");
      MDC.remove("roundNo");
    }
  }

  public List<MatchEventSnapshot> getMatchEvents(UUID matchId) {
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
        fetchedHeroes.stream().collect(toMap(Hero::id, Function.identity(), (h1, h2) -> h1));

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

  private void recordEliminationOnMatchEnd(Match match) {
    if (match.getSessionId() == null || match.getWinnerTeam() == null) {
      return;
    }
    UUID loserTeamId = match.loserTeam().orElse(null);
    if (loserTeamId == null) {
      return;
    }
    List<Match> roundMatches =
        matchRepository.findByRoundNoAndSessionId(match.getRoundNo(), match.getSessionId());
    boolean isSemifinal = match.getMatchType() == MatchType.KNOCKOUT && roundMatches.size() == 2;
    if (!isSemifinal) {
      eliminationRepository.save(
          new Elimination(
              loserTeamId,
              match.getSessionId(),
              match.getRoundNo(),
              EliminationReason.LOST_MATCH,
              OffsetDateTime.now(ZoneOffset.UTC)));
    }
  }
}
