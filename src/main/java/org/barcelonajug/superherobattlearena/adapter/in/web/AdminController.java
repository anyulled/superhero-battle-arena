package org.barcelonajug.superherobattlearena.adapter.in.web;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.barcelonajug.superherobattlearena.adapter.in.web.dto.BatchSimulationResult;
import org.barcelonajug.superherobattlearena.adapter.in.web.dto.CreateRoundRequest;
import org.barcelonajug.superherobattlearena.application.port.out.MatchEventRepositoryPort;
import org.barcelonajug.superherobattlearena.application.port.out.MatchRepositoryPort;
import org.barcelonajug.superherobattlearena.application.port.out.RoundRepositoryPort;
import org.barcelonajug.superherobattlearena.application.port.out.SessionRepositoryPort;
import org.barcelonajug.superherobattlearena.application.port.out.SubmissionRepositoryPort;
import org.barcelonajug.superherobattlearena.application.usecase.BattleEngine;
import org.barcelonajug.superherobattlearena.application.usecase.FatigueService;
import org.barcelonajug.superherobattlearena.application.usecase.MatchCreationService;
import org.barcelonajug.superherobattlearena.application.usecase.RosterService;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
  private static final Logger log = LoggerFactory.getLogger(AdminController.class);

  private final SessionRepositoryPort sessionRepository;
  private final RoundRepositoryPort roundRepository;
  private final MatchCreationService matchCreationService;
  private final MatchRepositoryPort matchRepository;
  private final SubmissionRepositoryPort submissionRepository;
  private final MatchEventRepositoryPort matchEventRepository;
  private final BattleEngine battleEngine;
  private final RosterService rosterService;
  private final FatigueService fatigueService;

  public AdminController(
      SessionRepositoryPort sessionRepository,
      RoundRepositoryPort roundRepository,
      MatchCreationService matchCreationService,
      MatchRepositoryPort matchRepository,
      SubmissionRepositoryPort submissionRepository,
      MatchEventRepositoryPort matchEventRepository,
      BattleEngine battleEngine,
      RosterService rosterService,
      FatigueService fatigueService) {
    this.sessionRepository = sessionRepository;
    this.roundRepository = roundRepository;
    this.matchCreationService = matchCreationService;
    this.matchRepository = matchRepository;
    this.submissionRepository = submissionRepository;
    this.matchEventRepository = matchEventRepository;
    this.battleEngine = battleEngine;
    this.rosterService = rosterService;
    this.fatigueService = fatigueService;
  }

  /** Start a new tournament session */
  @PostMapping("/sessions/start")
  public ResponseEntity<UUID> startSession(@RequestParam(required = false) UUID sessionId) {
    UUID id = (sessionId != null) ? sessionId : UUID.randomUUID();
    Session session = new Session(id, OffsetDateTime.now(), true);
    sessionRepository.save(session);
    return ResponseEntity.ok(session.getSessionId());
  }

  /** List all tournament sessions */
  @GetMapping("/sessions")
  public ResponseEntity<List<Session>> listSessions() {
    return ResponseEntity.ok(sessionRepository.findAll());
  }

  /** Create a new round with custom constraints */
  @PostMapping("/rounds/create")
  public ResponseEntity<Integer> createRound(@RequestBody CreateRoundRequest request) {
    // Validate session exists
    Optional<Session> session = sessionRepository.findById(request.sessionId());
    if (session.isEmpty() || !session.get().isActive()) {
      throw new IllegalStateException(
          "Session " + request.sessionId() + " does not exist or is not active");
    }

    Round round = new Round();
    round.setRoundNo(request.roundNo());
    round.setSessionId(request.sessionId());
    round.setSeed(System.currentTimeMillis());
    round.setStatus(RoundStatus.OPEN);
    round.setSpecJson(request.spec());

    roundRepository.save(round);
    return ResponseEntity.ok(round.getRoundNo());
  }

  /** Automatically match teams for a round */
  @PostMapping("/matches/auto-match")
  public ResponseEntity<List<UUID>> autoMatch(
      @RequestParam UUID sessionId, @RequestParam Integer roundNo) {
    List<UUID> matchIds = matchCreationService.autoMatch(sessionId, roundNo);
    return ResponseEntity.ok(matchIds);
  }

  /** Run all pending matches for a round */
  @PostMapping("/matches/run-all")
  public ResponseEntity<BatchSimulationResult> runAllBattles(
      @RequestParam Integer roundNo, @RequestParam(required = false) UUID sessionId) {

    // Find all pending matches for this round
    List<Match> pendingMatches =
        matchRepository.findAll().stream()
            .filter(m -> m.getRoundNo().equals(roundNo))
            .filter(m -> m.getStatus() == MatchStatus.PENDING)
            .filter(m -> sessionId == null || sessionId.equals(m.getSessionId()))
            .toList();

    List<UUID> matchIds = new ArrayList<>();
    Map<UUID, UUID> winners = new HashMap<>();
    int successCount = 0;

    Round round =
        roundRepository
            .findById(roundNo)
            .orElseThrow(() -> new IllegalArgumentException("Round not found: " + roundNo));

    for (Match match : pendingMatches) {
      try {
        // Get submissions
        Optional<Submission> subA =
            submissionRepository.findByTeamIdAndRoundNo(match.getTeamA(), match.getRoundNo());
        Optional<Submission> subB =
            submissionRepository.findByTeamIdAndRoundNo(match.getTeamB(), match.getRoundNo());

        if (subA.isEmpty() || subB.isEmpty()) {
          continue; // Skip matches without submissions
        }

        // Build teams with fatigue
        List<Hero> teamAHeroes =
            buildBattleTeam(match.getTeamA(), subA.get().getSubmissionJson(), match.getRoundNo());
        List<Hero> teamBHeroes =
            buildBattleTeam(match.getTeamB(), subB.get().getSubmissionJson(), match.getRoundNo());

        // Simulate
        SimulationResult result =
            battleEngine.simulate(
                match.getMatchId(),
                teamAHeroes,
                teamBHeroes,
                round.getSeed(),
                match.getTeamA(),
                match.getTeamB(),
                round.getSpecJson());

        // Persist result
        match.setStatus(MatchStatus.COMPLETED);
        match.setWinnerTeam(result.winnerTeamId());
        match.setResultJson(
            new MatchResult(
                result.winnerTeamId() != null ? result.winnerTeamId().toString() : "DRAW",
                result.totalTurns(),
                0));
        matchRepository.save(match);

        // Persist events
        int seq = 1;
        for (org.barcelonajug.superherobattlearena.domain.json.MatchEvent evt : result.events()) {
          MatchEvent matchEvent = new MatchEvent(match.getMatchId(), seq++, evt);
          matchEventRepository.save(matchEvent);
        }

        // Update hero usage
        fatigueService.recordUsage(
            match.getTeamA(), match.getRoundNo(), subA.get().getSubmissionJson().heroIds());
        fatigueService.recordUsage(
            match.getTeamB(), match.getRoundNo(), subB.get().getSubmissionJson().heroIds());

        matchIds.add(match.getMatchId());
        winners.put(match.getMatchId(), result.winnerTeamId());
        successCount++;

      } catch (Exception e) {
        // Log error and continue with next match
        log.error("Error simulating match {}: {}", match.getMatchId(), e.getMessage());
      }
    }

    BatchSimulationResult result =
        new BatchSimulationResult(matchIds, winners, pendingMatches.size(), successCount);

    return ResponseEntity.ok(result);
  }

  private List<Hero> buildBattleTeam(UUID teamId, DraftSubmission submission, int roundNo) {
    List<Hero> baseHeroes =
        submission.heroIds().stream()
            .map(
                heroId ->
                    rosterService
                        .getHero(heroId)
                        .orElseThrow(
                            () ->
                                new IllegalArgumentException(
                                    "Hero not found in roster: " + heroId)))
            .toList();
    return fatigueService.applyFatigue(teamId, baseHeroes, roundNo);
  }
}
