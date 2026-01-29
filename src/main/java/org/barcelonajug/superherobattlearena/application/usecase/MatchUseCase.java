package org.barcelonajug.superherobattlearena.application.usecase;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
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
import org.springframework.stereotype.Service;

@Service
public class MatchUseCase {

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
    List<Submission> submissions = submissionRepository.findByRoundNo(roundNo);
    List<UUID> matchIds = new ArrayList<>();

    for (int i = 0; i < submissions.size() - 1; i += 2) {
      Submission subA = submissions.get(i);
      Submission subB = submissions.get(i + 1);

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
    }

    return matchIds;
  }

  public UUID createMatch(UUID teamA, UUID teamB, Integer roundNo, UUID sessionId) {
    Match match = new Match();
    match.setMatchId(UUID.randomUUID());
    match.setSessionId(sessionId);
    match.setTeamA(teamA);
    match.setTeamB(teamB);
    match.setRoundNo(roundNo);
    match.setStatus(MatchStatus.PENDING);
    matchRepository.save(match);
    return match.getMatchId();
  }

  public String runMatch(UUID matchId) {
    Match match =
        matchRepository
            .findById(matchId)
            .orElseThrow(() -> new IllegalArgumentException("Match not found"));

    if (match.getStatus() != MatchStatus.PENDING) {
      throw new IllegalStateException("Match already run or running");
    }

    Optional<Submission> subA =
        submissionRepository.findByTeamIdAndRoundNo(match.getTeamA(), match.getRoundNo());
    Optional<Submission> subB =
        submissionRepository.findByTeamIdAndRoundNo(match.getTeamB(), match.getRoundNo());

    if (subA.isEmpty() || subB.isEmpty()) {
      throw new IllegalStateException("Submissions missing for one or both teams");
    }

    List<Hero> teamAHeroes =
        buildBattleTeam(match.getTeamA(), subA.get().getSubmissionJson(), match.getRoundNo());
    List<Hero> teamBHeroes =
        buildBattleTeam(match.getTeamB(), subB.get().getSubmissionJson(), match.getRoundNo());

    Round round = roundRepository.findById(match.getRoundNo()).orElseThrow();

    SimulationResult result =
        battleEngineUseCase.simulate(
            matchId,
            teamAHeroes,
            teamBHeroes,
            round.getSeed(),
            match.getTeamA(),
            match.getTeamB(),
            round.getSpecJson());

    match.setStatus(MatchStatus.COMPLETED);
    match.setWinnerTeam(result.winnerTeamId());
    match.setResultJson(
        new MatchResult(
            result.winnerTeamId() != null ? result.winnerTeamId().toString() : "DRAW",
            result.totalTurns(),
            0));
    matchRepository.save(match);

    int seq = 1;
    for (org.barcelonajug.superherobattlearena.domain.json.MatchEvent evt : result.events()) {
      MatchEvent matchEvent = new MatchEvent(matchId, seq++, evt);
      matchEventRepository.save(matchEvent);
    }

    updateHeroUsage(match.getTeamA(), match.getRoundNo(), subA.get().getSubmissionJson().heroIds());
    updateHeroUsage(match.getTeamB(), match.getRoundNo(), subB.get().getSubmissionJson().heroIds());

    return result.winnerTeamId() != null ? result.winnerTeamId().toString() : "DRAW";
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
    return matchRepository.findAll().stream()
        .filter(m -> m.getRoundNo().equals(roundNo))
        .filter(m -> m.getStatus() == MatchStatus.PENDING)
        .filter(m -> sessionId == null || sessionId.equals(m.getSessionId()))
        .toList();
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

  private void updateHeroUsage(UUID teamId, int roundNo, List<Integer> heroIds) {
    fatigueUseCase.recordUsage(teamId, roundNo, heroIds);
  }
}
