package org.barcelonajug.superherobattlearena.application.usecase;

import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElse;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.barcelonajug.superherobattlearena.application.port.out.HeroUsageRepositoryPort;
import org.barcelonajug.superherobattlearena.application.port.out.MatchEventRepositoryPort;
import org.barcelonajug.superherobattlearena.application.port.out.MatchRepositoryPort;
import org.barcelonajug.superherobattlearena.application.port.out.RoundRepositoryPort;
import org.barcelonajug.superherobattlearena.application.port.out.SessionRepositoryPort;
import org.barcelonajug.superherobattlearena.application.port.out.SubmissionRepositoryPort;
import org.barcelonajug.superherobattlearena.application.port.out.TeamRepositoryPort;
import org.barcelonajug.superherobattlearena.domain.Hero;
import org.barcelonajug.superherobattlearena.domain.Match;
import org.barcelonajug.superherobattlearena.domain.MatchEvent;
import org.barcelonajug.superherobattlearena.domain.MatchStatus;
import org.barcelonajug.superherobattlearena.domain.Round;
import org.barcelonajug.superherobattlearena.domain.RoundStatus;
import org.barcelonajug.superherobattlearena.domain.Session;
import org.barcelonajug.superherobattlearena.domain.SimulationResult;
import org.barcelonajug.superherobattlearena.domain.Submission;
import org.barcelonajug.superherobattlearena.domain.json.DraftSubmission;
import org.barcelonajug.superherobattlearena.domain.json.MatchResult;
import org.barcelonajug.superherobattlearena.domain.json.RoundSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminUseCase {

  private static final Logger log = LoggerFactory.getLogger(AdminUseCase.class);

  private final SessionRepositoryPort sessionRepository;
  private final RoundRepositoryPort roundRepository;
  private final MatchUseCase matchUseCase;
  private final MatchRepositoryPort matchRepository;
  private final SubmissionRepositoryPort submissionRepository;
  private final MatchEventRepositoryPort matchEventRepository;
  private final BattleEngineUseCase battleEngineUseCase;
  private final RosterUseCase rosterUseCase;
  private final FatigueUseCase fatigueUseCase;
  private final TeamRepositoryPort teamRepository;
  private final HeroUsageRepositoryPort heroUsageRepository;

  public AdminUseCase(
      final SessionRepositoryPort sessionRepository,
      final RoundRepositoryPort roundRepository,
      final MatchUseCase matchUseCase,
      final MatchRepositoryPort matchRepository,
      final SubmissionRepositoryPort submissionRepository,
      final MatchEventRepositoryPort matchEventRepository,
      final BattleEngineUseCase battleEngineUseCase,
      final RosterUseCase rosterUseCase,
      final FatigueUseCase fatigueUseCase,
      final TeamRepositoryPort teamRepository,
      final HeroUsageRepositoryPort heroUsageRepository) {
    this.sessionRepository = sessionRepository;
    this.roundRepository = roundRepository;
    this.matchUseCase = matchUseCase;
    this.matchRepository = matchRepository;
    this.submissionRepository = submissionRepository;
    this.matchEventRepository = matchEventRepository;
    this.battleEngineUseCase = battleEngineUseCase;
    this.rosterUseCase = rosterUseCase;
    this.fatigueUseCase = fatigueUseCase;
    this.teamRepository = teamRepository;
    this.heroUsageRepository = heroUsageRepository;
  }

  @Transactional
  public void resetDatabase() {
    log.info("Resetting database to initial state...");
    matchEventRepository.deleteAll();
    matchRepository.deleteAll();
    heroUsageRepository.deleteAll();
    submissionRepository.deleteAll();
    roundRepository.deleteAll();
    teamRepository.deleteAll();
    sessionRepository.deleteAll();
    log.info("Database reset successfully.");
  }

  @Transactional
  public UUID startSession(final UUID sessionId) {
    final UUID id = (sessionId != null) ? sessionId : UUID.randomUUID();
    MDC.put("sessionId", id.toString());

    try {
      log.info("Starting new session - sessionId={}", id);
      final Session session = new Session(id, OffsetDateTime.now(), true);
      sessionRepository.save(session);
      log.info("Session started successfully - sessionId={}", id);
      return session.getSessionId();
    } finally {
      MDC.remove("sessionId");
    }
  }

  public List<Session> listSessions() {
    return sessionRepository.findAll();
  }

  @Transactional
  public Integer createRound(final UUID sessionId, final RoundSpec spec) {
    MDC.put("sessionId", sessionId.toString());

    // Auto-calculate next round number
    final Integer nextRoundNo = roundRepository.findMaxRoundNo(sessionId).orElse(0) + 1;
    MDC.put("roundNo", nextRoundNo.toString());

    try {
      log.info(
          "Creating round - sessionId={}, roundNo={}, spec={}",
          sessionId,
          nextRoundNo,
          spec.description());

      final Optional<Session> session = sessionRepository.findById(sessionId);
      if (session.isEmpty() || !session.get().isActive()) {
        log.error(
            "Invalid session - sessionId={}, exists={}, active={}",
            sessionId,
            session.isPresent(),
            session.map(Session::isActive).orElse(false));
        throw new IllegalStateException(
            "Session " + sessionId + " does not exist or is not active");
      }

      final Round round = new Round();
      round.setRoundId(UUID.randomUUID());
      round.setRoundNo(nextRoundNo);
      round.setSessionId(sessionId);
      round.setSeed(System.currentTimeMillis());
      round.setStatus(RoundStatus.OPEN);
      round.setSpecJson(spec);

      roundRepository.save(round);
      log.info("Round created successfully - roundNo={}, seed={}", nextRoundNo, round.getSeed());
      return round.getRoundNo();
    } finally {
      MDC.remove("sessionId");
      MDC.remove("roundNo");
    }
  }

  public List<UUID> autoMatch(final UUID sessionId, final Integer roundNo) {
    return matchUseCase.autoMatch(sessionId, roundNo);
  }

  public UUID createMatch(
      final UUID teamA, final UUID teamB, final Integer roundNo, final UUID sessionId) {
    return matchUseCase.createMatch(teamA, teamB, roundNo, sessionId);
  }

  public String runMatch(final UUID matchId) {
    return matchUseCase.runMatch(matchId);
  }

  @Transactional
  public Map<String, Object> runAllBattles(final Integer roundNo, final UUID sessionIdOrNull) {
    final long startTime = System.currentTimeMillis();
    MDC.put("roundNo", roundNo.toString());

    // Determine effective sessionId
    final UUID sessionId;
    if (sessionIdOrNull != null) {
      sessionId = sessionIdOrNull;
    } else {
      final Optional<Match> anyMatch =
          matchRepository.findAll().stream()
              .filter(m -> m.getRoundNo().equals(roundNo))
              .filter(m -> m.getStatus() == MatchStatus.PENDING)
              .findFirst();

      sessionId = anyMatch.map(Match::getSessionId).orElse(null);
    }

    if (sessionId != null) {
      MDC.put("sessionId", sessionId.toString());
    }

    try {
      log.info("Starting batch battle execution - roundNo={}, sessionId={}", roundNo, sessionId);

      if (sessionId == null) {
        log.warn(
            "No session context found for runAllBattles roundNo={}. Assuming nothing to run.",
            roundNo);
        final Map<String, Object> result = new HashMap<>();
        result.put("matchIds", new ArrayList<>());
        result.put("winners", new HashMap<>());
        result.put("total", 0);
        result.put("successCount", 0);
        return result;
      }

      // Filter matches using the effective sessionId
      final List<Match> pendingMatches =
          matchRepository.findAll().stream()
              .filter(m -> m.getRoundNo().equals(roundNo))
              .filter(m -> m.getStatus() == MatchStatus.PENDING)
              .filter(m -> sessionId.equals(m.getSessionId()))
              .toList();

      log.info("Found {} pending matches for round {}", pendingMatches.size(), roundNo);

      final List<UUID> matchIds = new ArrayList<>();
      final Map<UUID, UUID> winners = new HashMap<>();
      int successCount = 0;

      final Round round =
          roundRepository
              .findBySessionIdAndRoundNo(sessionId, roundNo)
              .orElseThrow(() -> new IllegalArgumentException("Round not found: " + roundNo));

      for (final Match match : pendingMatches) {
        try {
          final Optional<Submission> subA =
              submissionRepository.findByTeamIdAndRoundNo(match.getTeamA(), match.getRoundNo());
          final Optional<Submission> subB =
              submissionRepository.findByTeamIdAndRoundNo(match.getTeamB(), match.getRoundNo());

          if (subA.isEmpty() || subB.isEmpty()) {
            log.warn(
                "Skipping match {} - missing submissions (teamA={}, teamB={})",
                match.getMatchId(),
                subA.isPresent(),
                subB.isPresent());
            continue;
          }

          final List<Hero> teamAHeroes =
              buildBattleTeam(
                  match.getTeamA(),
                  requireNonNull(subA.get().getSubmissionJson()),
                  match.getRoundNo());
          final List<Hero> teamBHeroes =
              buildBattleTeam(
                  match.getTeamB(),
                  requireNonNull(subB.get().getSubmissionJson()),
                  match.getRoundNo());

          final SimulationResult result =
              battleEngineUseCase.simulate(
                  match.getMatchId(),
                  teamAHeroes,
                  teamBHeroes,
                  requireNonNullElse(round.getSeed(), 0L),
                  match.getTeamA(),
                  match.getTeamB(),
                  requireNonNull(round.getSpecJson()));

          match.setStatus(MatchStatus.COMPLETED);
          match.setWinnerTeam(result.winnerTeamId());
          match.setResultJson(
              new MatchResult(
                  result.winnerTeamId() != null ? result.winnerTeamId().toString() : "DRAW",
                  result.totalTurns(),
                  0));
          matchRepository.save(match);

          final java.util.concurrent.atomic.AtomicInteger seq =
              new java.util.concurrent.atomic.AtomicInteger(1);
          final List<MatchEvent> matchEvents =
              result.events().stream()
                  .map(evt -> new MatchEvent(match.getMatchId(), seq.getAndIncrement(), evt))
                  .toList();
          matchEventRepository.saveAll(matchEvents);

          fatigueUseCase.recordUsage(
              match.getTeamA(),
              match.getRoundNo(),
              java.util.Objects.requireNonNull(subA.get().getSubmissionJson()).heroIds());
          fatigueUseCase.recordUsage(
              match.getTeamB(),
              match.getRoundNo(),
              java.util.Objects.requireNonNull(subB.get().getSubmissionJson()).heroIds());

          matchIds.add(match.getMatchId());
          winners.put(match.getMatchId(), result.winnerTeamId());
          successCount++;

        } catch (final Exception e) {
          log.error("Error simulating match {}: {}", match.getMatchId(), e.getMessage(), e);
        }
      }

      final long duration = System.currentTimeMillis() - startTime;

      final List<Match> remainingPending = matchRepository.findPendingMatches(roundNo, sessionId);
      if (remainingPending.isEmpty()) {
        round.setStatus(RoundStatus.CLOSED);
        roundRepository.save(round);
        log.info("Closed round {} for session {} as all matches are completed", roundNo, sessionId);
      }

      log.info(
          "Batch battle execution completed - roundNo={}, total={}, successful={}, failed={}, duration={}ms",
          roundNo,
          pendingMatches.size(),
          successCount,
          pendingMatches.size() - successCount,
          duration);

      final Map<String, Object> result = new HashMap<>();
      result.put("matchIds", matchIds);
      result.put("winners", winners);
      result.put("total", pendingMatches.size());
      result.put("successCount", successCount);
      return result;
    } finally {
      MDC.remove("roundNo");
      MDC.remove("sessionId");
    }
  }

  private List<Hero> buildBattleTeam(
      final UUID teamId, final DraftSubmission submission, final int roundNo) {
    final List<Hero> battleHeroes = new ArrayList<>();
    for (final Integer heroId : submission.heroIds()) {
      final Hero baseHero =
          rosterUseCase
              .getHero(heroId)
              .orElseThrow(
                  () -> new IllegalArgumentException("Hero not found in roster: " + heroId));
      final Hero fatiguedHero = fatigueUseCase.applyFatigue(teamId, baseHero, roundNo);
      battleHeroes.add(fatiguedHero);
    }
    return battleHeroes;
  }
}
