package org.barcelonajug.superherobattlearena.web;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.barcelonajug.superherobattlearena.domain.Hero;
import org.barcelonajug.superherobattlearena.domain.HeroUsage;
import org.barcelonajug.superherobattlearena.domain.Match;
import org.barcelonajug.superherobattlearena.domain.MatchEvent;
import org.barcelonajug.superherobattlearena.domain.MatchStatus;
import org.barcelonajug.superherobattlearena.domain.Round;
import org.barcelonajug.superherobattlearena.domain.SimulationResult;
import org.barcelonajug.superherobattlearena.domain.Submission;
import org.barcelonajug.superherobattlearena.domain.SubmissionId;
import org.barcelonajug.superherobattlearena.domain.json.MatchResult;
import org.barcelonajug.superherobattlearena.repository.HeroUsageRepository;
import org.barcelonajug.superherobattlearena.repository.MatchEventRepository;
import org.barcelonajug.superherobattlearena.repository.MatchRepository;
import org.barcelonajug.superherobattlearena.repository.RoundRepository;
import org.barcelonajug.superherobattlearena.repository.SubmissionRepository;
import org.barcelonajug.superherobattlearena.service.BattleEngine;
import org.barcelonajug.superherobattlearena.service.FatigueService;
import org.barcelonajug.superherobattlearena.service.RosterService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/matches")
public class MatchController {

    private final MatchRepository matchRepository;
    private final MatchEventRepository matchEventRepository;
    private final SubmissionRepository submissionRepository;
    private final RoundRepository roundRepository;
    private final BattleEngine battleEngine;
    private final RosterService rosterService;
    private final HeroUsageRepository heroUsageRepository;
    private final FatigueService fatigueService; // Added for completeness, if we want to log fatigue or verify

    public MatchController(MatchRepository matchRepository, MatchEventRepository matchEventRepository,
            SubmissionRepository submissionRepository, RoundRepository roundRepository,
            BattleEngine battleEngine, RosterService rosterService,
            HeroUsageRepository heroUsageRepository, FatigueService fatigueService) {
        this.matchRepository = matchRepository;
        this.matchEventRepository = matchEventRepository;
        this.submissionRepository = submissionRepository;
        this.roundRepository = roundRepository;
        this.battleEngine = battleEngine;
        this.rosterService = rosterService;
        this.heroUsageRepository = heroUsageRepository;
        this.fatigueService = fatigueService;
    }

    @PostMapping("/{matchId}/run")
    public ResponseEntity<String> runMatch(@PathVariable UUID matchId) {
        Optional<Match> matchOpt = matchRepository.findById(matchId);
        if (matchOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Match match = matchOpt.get();
        if (match.getStatus() == MatchStatus.COMPLETED) {
            return ResponseEntity.badRequest().body("Match already completed");
        }

        Optional<Round> roundOpt = roundRepository.findById(match.getRoundNo());
        if (roundOpt.isEmpty())
            return ResponseEntity.badRequest().body("Round not found");
        Round round = roundOpt.get();

        // Fetch Submissions
        Submission subA = submissionRepository.findById(new SubmissionId(match.getTeamA(), match.getRoundNo()))
                .orElseThrow();
        Submission subB = submissionRepository.findById(new SubmissionId(match.getTeamB(), match.getRoundNo()))
                .orElseThrow();

        // Build Hero Lists (with fatigue logic applied? BattleEngine takes Hero
        // objects.
        // Hero objects in domain/Hero.java have base stats.
        // If fatigue affects stats, we should clone and modify, or pass modifiers to
        // engine.
        // The engine only sees tag modifiers from RoundSpec.
        // It doesn't seem to have explicit fatigue params.
        // However, we can modify the input Heroes passed to the engine.

        List<org.barcelonajug.superherobattlearena.domain.Hero> teamAHeroes = buildBattleHeroes(subA,
                round.getRoundNo());
        List<org.barcelonajug.superherobattlearena.domain.Hero> teamBHeroes = buildBattleHeroes(subB,
                round.getRoundNo());

        // Run Simulation
        SimulationResult result = battleEngine.simulate(
                matchId,
                teamAHeroes,
                teamBHeroes,
                round.getSeed() != null ? round.getSeed() : 0L,
                match.getTeamA(),
                match.getTeamB(),
                round.getSpecJson());

        // Save Result
        match.setWinnerTeam(result.winnerTeamId());
        match.setStatus(MatchStatus.COMPLETED);
        match.setResultJson(new MatchResult(
                result.winnerTeamId() != null ? result.winnerTeamId().toString() : "DRAW",
                result.totalTurns(),
                0 // TODO: Calculate damage from events if needed
        ));
        matchRepository.save(match);

        // Save Events
        int seq = 0;
        for (org.barcelonajug.superherobattlearena.domain.json.MatchEvent event : result.events()) {
            MatchEvent dbEvent = new MatchEvent();
            dbEvent.setMatchId(matchId);
            dbEvent.setSeq(seq++);
            dbEvent.setEventJson(event);
            matchEventRepository.save(dbEvent);
        }

        // Update Hero Usage (Streaks)
        updateHeroUsage(subA, round.getRoundNo());
        updateHeroUsage(subB, round.getRoundNo());

        return ResponseEntity.ok("Match completed. Winner: " + result.winnerTeamId());
    }

    private List<org.barcelonajug.superherobattlearena.domain.Hero> buildBattleHeroes(Submission submission,
            int roundNo) {
        List<org.barcelonajug.superherobattlearena.domain.Hero> heroes = new ArrayList<>();
        for (Integer heroId : submission.getSubmissionJson().heroIds()) {
            rosterService.getHero(heroId).ifPresent(original -> {
                // Apply Fatigue
                BigDecimal multiplier = fatigueService.calculateFatigueMultiplier(submission.getTeamId(), heroId,
                        roundNo);

                // create copy with modified stats
                int newHp = (int) (original.powerstats().hp() * multiplier.doubleValue());
                int newAtk = (int) (original.powerstats().atk() * multiplier.doubleValue());
                int newDef = (int) (original.powerstats().def() * multiplier.doubleValue());
                int newSpd = (int) (original.powerstats().spd() * multiplier.doubleValue()); // Should speed be
                                                                                             // affected? usually
                                                                                             // physical stats.

                org.barcelonajug.superherobattlearena.domain.Hero.PowerStats stats = new org.barcelonajug.superherobattlearena.domain.Hero.PowerStats(
                        newHp, newAtk, newDef, newSpd);

                heroes.add(new org.barcelonajug.superherobattlearena.domain.Hero(
                        original.id(), original.name(), stats, original.role(), original.cost(), original.tags()));
            });
        }
        return heroes;
    }

    private void updateHeroUsage(Submission submission, int roundNo) {
        for (Integer heroId : submission.getSubmissionJson().heroIds()) {
            // Find previous usage
            int prevRound = roundNo - 1;
            int currentStreak = 1;
            Optional<HeroUsage> lastUsage = heroUsageRepository.findByTeamIdAndHeroIdAndRoundNo(submission.getTeamId(),
                    heroId, prevRound);
            if (lastUsage.isPresent()) {
                currentStreak = lastUsage.get().getStreak() + 1;
            }

            HeroUsage usage = new HeroUsage();
            usage.setTeamId(submission.getTeamId());
            usage.setHeroId(heroId);
            usage.setRoundNo(roundNo);
            usage.setStreak(currentStreak);
            usage.setMultiplier(BigDecimal.ONE); // Used multiplier? Or calculated? Just storing for history.

            heroUsageRepository.save(usage);
        }
    }

    @GetMapping("/{matchId}/events")
    public ResponseEntity<List<org.barcelonajug.superherobattlearena.domain.json.MatchEvent>> getMatchEvents(
            @PathVariable UUID matchId) {
        List<MatchEvent> events = matchEventRepository.findByMatchIdOrderBySeqAsc(matchId);
        List<org.barcelonajug.superherobattlearena.domain.json.MatchEvent> jsonEvents = events.stream()
                .map(MatchEvent::getEventJson)
                .toList();
        return ResponseEntity.ok(jsonEvents);
    }

    // Simple executor for demo streaming
    private final ExecutorService executor = Executors.newCachedThreadPool();

    @GetMapping(value = "/{matchId}/events/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamEvents(@PathVariable UUID matchId) {
        SseEmitter emitter = new SseEmitter(60000L); // 1 min timeout
        ExecutorService sseExecutor = Executors.newSingleThreadExecutor();

        sseExecutor.execute(() -> {
            try {
                List<MatchEvent> events = matchEventRepository.findByMatchIdOrderBySeqAsc(matchId);
                for (MatchEvent event : events) {
                    emitter.send(event.getEventJson());
                    Thread.sleep(100); // Simulate delay
                }
                emitter.complete();
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }
}
