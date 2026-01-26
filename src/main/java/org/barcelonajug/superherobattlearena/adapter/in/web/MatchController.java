package org.barcelonajug.superherobattlearena.adapter.in.web;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.barcelonajug.superherobattlearena.application.port.out.HeroUsageRepositoryPort;
import org.barcelonajug.superherobattlearena.application.port.out.MatchEventRepositoryPort;
import org.barcelonajug.superherobattlearena.application.port.out.MatchRepositoryPort;
import org.barcelonajug.superherobattlearena.application.port.out.RoundRepositoryPort;
import org.barcelonajug.superherobattlearena.application.port.out.SubmissionRepositoryPort;
import org.barcelonajug.superherobattlearena.application.usecase.BattleEngine;
import org.barcelonajug.superherobattlearena.application.usecase.FatigueService;
import org.barcelonajug.superherobattlearena.application.usecase.RosterService;
import org.barcelonajug.superherobattlearena.domain.Hero;
import org.barcelonajug.superherobattlearena.domain.HeroUsage;
import org.barcelonajug.superherobattlearena.domain.Match;
import org.barcelonajug.superherobattlearena.domain.MatchEvent;
import org.barcelonajug.superherobattlearena.domain.MatchStatus;
import org.barcelonajug.superherobattlearena.domain.Round;
import org.barcelonajug.superherobattlearena.domain.SimulationResult;
import org.barcelonajug.superherobattlearena.domain.Submission;
import org.barcelonajug.superherobattlearena.domain.json.DraftSubmission;
import org.barcelonajug.superherobattlearena.domain.json.MatchResult;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/matches")
public class MatchController {

    private final MatchRepositoryPort matchRepository;
    private final MatchEventRepositoryPort matchEventRepository;
    private final SubmissionRepositoryPort submissionRepository;
    private final RoundRepositoryPort roundRepository;
    private final BattleEngine battleEngine;
    private final RosterService rosterService;
    private final HeroUsageRepositoryPort heroUsageRepository;
    private final FatigueService fatigueService;

    // A simple executor for async simulation (demo purpose)
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public MatchController(MatchRepositoryPort matchRepository, MatchEventRepositoryPort matchEventRepository,
            SubmissionRepositoryPort submissionRepository, RoundRepositoryPort roundRepository,
            BattleEngine battleEngine, RosterService rosterService,
            HeroUsageRepositoryPort heroUsageRepository, FatigueService fatigueService) {
        this.matchRepository = matchRepository;
        this.matchEventRepository = matchEventRepository;
        this.submissionRepository = submissionRepository;
        this.roundRepository = roundRepository;
        this.battleEngine = battleEngine;
        this.rosterService = rosterService;
        this.heroUsageRepository = heroUsageRepository;
        this.fatigueService = fatigueService;
    }

    @PostMapping("/create")
    public ResponseEntity<UUID> createMatch(@RequestParam UUID teamA, @RequestParam UUID teamB,
            @RequestParam(defaultValue = "1") Integer roundNo) {
        Match match = new Match();
        match.setMatchId(UUID.randomUUID());
        match.setTeamA(teamA);
        match.setTeamB(teamB);
        match.setRoundNo(roundNo);
        match.setStatus(MatchStatus.PENDING);
        matchRepository.save(match);
        return ResponseEntity.ok(match.getMatchId());
    }

    @PostMapping("/{matchId}/run")
    public ResponseEntity<String> runMatch(@PathVariable UUID matchId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new IllegalArgumentException("Match not found"));

        if (match.getStatus() != MatchStatus.PENDING) {
            return ResponseEntity.badRequest().body("Match already run or running");
        }

        // 1. Get Submissions
        Optional<Submission> subA = submissionRepository.findByTeamIdAndRoundNo(match.getTeamA(), match.getRoundNo());
        Optional<Submission> subB = submissionRepository.findByTeamIdAndRoundNo(match.getTeamB(), match.getRoundNo());

        if (subA.isEmpty() || subB.isEmpty()) {
            return ResponseEntity.badRequest().body("Submissions missing for one or both teams");
        }

        // 2. Build Teams (Apply Fatigue)
        List<Hero> teamAHeroes = buildBattleTeam(match.getTeamA(), subA.get().getSubmissionJson(), match.getRoundNo());
        List<Hero> teamBHeroes = buildBattleTeam(match.getTeamB(), subB.get().getSubmissionJson(), match.getRoundNo());

        // 3. Get Round context
        Round round = roundRepository.findById(match.getRoundNo()).orElseThrow();

        // 4. Simulate
        SimulationResult result = battleEngine.simulate(matchId, teamAHeroes, teamBHeroes, round.getSeed(),
                match.getTeamA(), match.getTeamB(), round.getSpecJson());

        // 5. Persist Result
        match.setStatus(MatchStatus.COMPLETED);
        match.setWinnerTeam(result.winnerTeamId());
        match.setResultJson(new MatchResult(
                result.winnerTeamId() != null ? result.winnerTeamId().toString() : "DRAW",
                result.totalTurns(),
                0));
        matchRepository.save(match);

        // 6. Persist Events
        int seq = 1;
        for (org.barcelonajug.superherobattlearena.domain.json.MatchEvent evt : result.events()) {
            MatchEvent matchEvent = new MatchEvent(matchId, seq++, evt);
            matchEventRepository.save(matchEvent);
        }

        // 7. Update Hero Usage (for Fatigue next round)
        updateHeroUsage(match.getTeamA(), match.getRoundNo(), subA.get().getSubmissionJson().heroIds());
        updateHeroUsage(match.getTeamB(), match.getRoundNo(), subB.get().getSubmissionJson().heroIds());

        return ResponseEntity.ok("Match completed. Winner: " + result.winnerTeamId());
    }

    private List<Hero> buildBattleTeam(UUID teamId, DraftSubmission submission, int roundNo) {
        List<Hero> battleHeroes = new ArrayList<>();
        for (Integer heroId : submission.heroIds()) {
            Hero baseHero = rosterService.getHero(heroId).orElseThrow();
            // Apply Fatigue
            Hero fatiguedHero = fatigueService.applyFatigue(teamId, baseHero);
            battleHeroes.add(fatiguedHero);
        }
        return battleHeroes;
    }

    private void updateHeroUsage(UUID teamId, int roundNo, List<Integer> heroIds) {
        // Need to calculate streak. Simplified:
        // Query previous usage.
        // We actually need a robust way to store usage.
        // For this Hackathon step, we store a record for this round.
        // Logic for streak calculation resides in Service or we pre-calculate it here.

        for (Integer heroId : heroIds) {
            // Find streak from previous round
            // This logic ideally belongs to Domain Service / FatigueService "recordUsage"
            // For now, simple insert
            HeroUsage usage = new HeroUsage(
                    teamId,
                    heroId,
                    roundNo,
                    1 + getStreakFromPrevious(teamId, heroId, roundNo),
                    java.math.BigDecimal.ONE);

            heroUsageRepository.save(usage);
        }
    }

    private int getStreakFromPrevious(UUID teamId, Integer heroId, int currentRound) {
        // Mock query
        return 0;
    }

    @GetMapping("/{matchId}/events")
    public List<org.barcelonajug.superherobattlearena.domain.json.MatchEvent> getEvents(@PathVariable UUID matchId) {
        return matchEventRepository.findByMatchId(matchId).stream()
                .map(MatchEvent::eventJson)
                .toList();
    }

    @GetMapping
    public ResponseEntity<List<Match>> getAllMatches() {
        return ResponseEntity.ok(matchRepository.findAll());
    }

    @GetMapping("/{matchId}")
    public ResponseEntity<Match> getMatch(@PathVariable UUID matchId) {
        return matchRepository.findById(matchId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{matchId}/events/stream")
    public org.springframework.web.servlet.mvc.method.annotation.SseEmitter streamEvents(@PathVariable UUID matchId) {
        org.springframework.web.servlet.mvc.method.annotation.SseEmitter emitter = new org.springframework.web.servlet.mvc.method.annotation.SseEmitter(
                600000L); // 10 min timeout
        executor.submit(() -> {
            try {
                // If match is still pending or running, we might need to wait or poll.
                // But for now, we assume we stream what is available or replay.
                // In a real scenario, this would subscribe to a live event bus.
                // Here we just dump existing events with a slight delay to simulate replay.
                List<MatchEvent> events = matchEventRepository.findByMatchId(matchId);
                for (MatchEvent event : events) {
                    emitter.send(event.eventJson());
                    Thread.sleep(500);
                }
                emitter.complete();
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        });
        return emitter;
    }
}
