package org.barcelonajug.superherobattlearena.application.usecase;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.barcelonajug.superherobattlearena.application.port.out.MatchEventRepositoryPort;
import org.barcelonajug.superherobattlearena.application.port.out.MatchRepositoryPort;
import org.barcelonajug.superherobattlearena.application.port.out.RoundRepositoryPort;
import org.barcelonajug.superherobattlearena.application.port.out.SessionRepositoryPort;
import org.barcelonajug.superherobattlearena.application.port.out.SubmissionRepositoryPort;
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

  public AdminUseCase(
      SessionRepositoryPort sessionRepository,
      RoundRepositoryPort roundRepository,
      MatchUseCase matchUseCase,
      MatchRepositoryPort matchRepository,
      SubmissionRepositoryPort submissionRepository,
      MatchEventRepositoryPort matchEventRepository,
      BattleEngineUseCase battleEngineUseCase,
      RosterUseCase rosterUseCase,
      FatigueUseCase fatigueUseCase) {
    this.sessionRepository = sessionRepository;
    this.roundRepository = roundRepository;
    this.matchUseCase = matchUseCase;
    this.matchRepository = matchRepository;
    this.submissionRepository = submissionRepository;
    this.matchEventRepository = matchEventRepository;
    this.battleEngineUseCase = battleEngineUseCase;
    this.rosterUseCase = rosterUseCase;
    this.fatigueUseCase = fatigueUseCase;
  }

  @Transactional
  public UUID startSession(UUID sessionId) {
    UUID id = (sessionId != null) ? sessionId : UUID.randomUUID();
    MDC.put("sessionId", id.toString());

    try {
      log.info("Starting new session - sessionId={}", id);
      Session session = new Session(id, OffsetDateTime.now(), true);
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
  public Integer createRound(UUID sessionId, Integer roundNo, RoundSpec spec) {
    MDC.put("sessionId", sessionId.toString());
    MDC.put("roundNo", roundNo.toString());

    try {
      log.info(
          "Creating round - sessionId={}, roundNo={}, spec={}",
          sessionId,
          roundNo,
          spec.description());

      Optional<Session> session = sessionRepository.findById(sessionId);
      if (session.isEmpty() || !session.get().isActive()) {
        log.error(
            "Invalid session - sessionId={}, exists={}, active={}",
            sessionId,
            session.isPresent(),
            session.map(Session::isActive).orElse(false));
        throw new IllegalStateException(
            "Session " + sessionId + " does not exist or is not active");
      }

      Round round = new Round();
      round.setRoundNo(roundNo);
      round.setSessionId(sessionId);
      round.setSeed(System.currentTimeMillis());
      round.setStatus(RoundStatus.OPEN);
      round.setSpecJson(spec);

      roundRepository.save(round);
      log.info("Round created successfully - roundNo={}, seed={}", roundNo, round.getSeed());
      return round.getRoundNo();
    } finally {
      MDC.remove("sessionId");
      MDC.remove("roundNo");
    }
  }

  public List<UUID> autoMatch(UUID sessionId, Integer roundNo) {
    return matchUseCase.autoMatch(sessionId, roundNo);
  }

  public UUID createMatch(UUID teamA, UUID teamB, Integer roundNo, UUID sessionId) {
    return matchUseCase.createMatch(teamA, teamB, roundNo, sessionId);
  }

  public String runMatch(UUID matchId) {
    return matchUseCase.runMatch(matchId);
  }

  @Transactional
  public Map<String, Object> runAllBattles(Integer roundNo, UUID sessionId) {
    long startTime = System.currentTimeMillis();
    MDC.put("roundNo", roundNo.toString());
    if (sessionId != null) {
      MDC.put("sessionId", sessionId.toString());
    }

    try {
      log.info("Starting batch battle execution - roundNo={}, sessionId={}", roundNo, sessionId);

      List<Match> pendingMatches =
          matchRepository.findAll().stream()
              .filter(m -> m.getRoundNo().equals(roundNo))
              .filter(m -> m.getStatus() == MatchStatus.PENDING)
              .filter(m -> sessionId == null || sessionId.equals(m.getSessionId()))
              .toList();

      log.info("Found {} pending matches for round {}", pendingMatches.size(), roundNo);

      List<UUID> matchIds = new ArrayList<>();
      Map<UUID, UUID> winners = new HashMap<>();
      int successCount = 0;

      Round round =
          roundRepository
              .findById(roundNo)
              .orElseThrow(() -> new IllegalArgumentException("Round not found: " + roundNo));

      for (Match match : pendingMatches) {
        try {
          Optional<Submission> subA =
              submissionRepository.findByTeamIdAndRoundNo(match.getTeamA(), match.getRoundNo());
          Optional<Submission> subB =
              submissionRepository.findByTeamIdAndRoundNo(match.getTeamB(), match.getRoundNo());

          if (subA.isEmpty() || subB.isEmpty()) {
            log.warn(
                "Skipping match {} - missing submissions (teamA={}, teamB={})",
                match.getMatchId(),
                subA.isPresent(),
                subB.isPresent());
            continue;
          }

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

          SimulationResult result =
              battleEngineUseCase.simulate(
                  match.getMatchId(),
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

          java.util.concurrent.atomic.AtomicInteger seq =
              new java.util.concurrent.atomic.AtomicInteger(1);
          List<MatchEvent> matchEvents =
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

        } catch (Exception e) {
          log.error("Error simulating match {}: {}", match.getMatchId(), e.getMessage(), e);
        }
      }

      long duration = System.currentTimeMillis() - startTime;
      log.info(
          "Batch battle execution completed - roundNo={}, total={}, successful={}, failed={}, duration={}ms",
          roundNo,
          pendingMatches.size(),
          successCount,
          pendingMatches.size() - successCount,
          duration);

      Map<String, Object> result = new HashMap<>();
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

  private List<Hero> buildBattleTeam(UUID teamId, DraftSubmission submission, int roundNo) {
    List<Hero> battleHeroes = new ArrayList<>();
    for (Integer heroId : submission.heroIds()) {
      Hero baseHero =
          rosterUseCase
              .getHero(heroId)
              .orElseThrow(
                  () -> new IllegalArgumentException("Hero not found in roster: " + heroId));
      Hero fatiguedHero = fatigueUseCase.applyFatigue(teamId, baseHero, roundNo);
      battleHeroes.add(fatiguedHero);
    }
    return battleHeroes;
  }
}
