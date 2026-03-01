package org.barcelonajug.superherobattlearena.application.usecase;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import org.barcelonajug.superherobattlearena.domain.Hero;
import org.barcelonajug.superherobattlearena.domain.SimulationResult;
import org.barcelonajug.superherobattlearena.domain.json.MatchEventSnapshot;
import org.barcelonajug.superherobattlearena.domain.json.RoundSpec;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

@Service
public class BattleEngineUseCase {

  private static final Logger log = LoggerFactory.getLogger(BattleEngineUseCase.class);
  private static final int MAX_TURNS = 50;
  private static final double DAMAGE_DEF_FACTOR = 0.6;

  public SimulationResult simulate(
      UUID matchId,
      List<Hero> teamAHeroes,
      List<Hero> teamBHeroes,
      long roundSeed,
      final UUID teamAId,
      final UUID teamBId,
      RoundSpec roundSpec) {
    long startTime = System.currentTimeMillis();
    MDC.put("matchId", matchId.toString());

    try {
      log.info(
          "Starting battle simulation - matchId={}, teamA={}, teamB={}, seed={}",
          matchId,
          teamAId,
          teamBId,
          roundSeed);
      log.debug(
          "Team compositions - teamA: {} heroes, teamB: {} heroes",
          teamAHeroes.size(),
          teamBHeroes.size());

      Random random = new Random(roundSeed + matchId.hashCode());

      List<BattleHeroUseCase> allHeroes = new ArrayList<>();
      teamAHeroes.forEach(h -> allHeroes.add(new BattleHeroUseCase(h, teamAId)));
      teamBHeroes.forEach(h -> allHeroes.add(new BattleHeroUseCase(h, teamBId)));

      List<MatchEventSnapshot> events = new ArrayList<>();
      AtomicLong logicalTime = new AtomicLong(0);

      events.add(MatchEventSnapshot.matchStart(logicalTime.getAndIncrement()));

      int turn = 0;
      UUID winnerId = null;

      sortHeroesBySpeed(allHeroes);

      while (turn < MAX_TURNS && winnerId == null) {
        turn++;
        events.add(MatchEventSnapshot.turnStart(turn, logicalTime.getAndIncrement()));

        winnerId = executeTurn(allHeroes, teamAId, teamBId, roundSpec, random, events, logicalTime);

        if (winnerId == null) {
          winnerId = checkWinCondition(allHeroes, teamAId, teamBId);
        }

        if (turn % 10 == 0) {
          log.debug("Battle progress - turn {}/{}", turn, MAX_TURNS);
        }
      }

      if (winnerId != null) {
        events.add(MatchEventSnapshot.matchEnd(winnerId, logicalTime.getAndIncrement()));
        log.info("Battle completed - winner={}, turns={}", winnerId, turn);
      } else {
        events.add(MatchEventSnapshot.draw(logicalTime.getAndIncrement()));
        log.info("Battle completed - result=DRAW, turns={}", turn);
      }

      long duration = System.currentTimeMillis() - startTime;
      log.info(
          "Battle simulation completed in {}ms - {} events generated", duration, events.size());

      return new SimulationResult(winnerId, turn, events);
    } finally {
      MDC.remove("matchId");
    }
  }

  private @Nullable UUID executeTurn(
      List<BattleHeroUseCase> allHeroes,
      UUID teamAId,
      UUID teamBId,
      RoundSpec roundSpec,
      Random random,
      List<MatchEventSnapshot> events,
      AtomicLong logicalTime) {
    for (BattleHeroUseCase attacker : allHeroes) {
      if (!attacker.isAlive()) {
        continue;
      }

      UUID winnerId = checkWinCondition(allHeroes, teamAId, teamBId);
      if (winnerId != null) {
        return winnerId;
      }

      BattleHeroUseCase target = findTarget(attacker, allHeroes, teamAId, teamBId, random);
      if (target == null) {
        return attacker.teamId;
      }

      performAttack(attacker, target, roundSpec, random, events, logicalTime);
    }
    return null;
  }

  private void performAttack(
      BattleHeroUseCase attacker,
      BattleHeroUseCase target,
      RoundSpec roundSpec,
      Random random,
      List<MatchEventSnapshot> events,
      AtomicLong logicalTime) {

    // 1. Dodge Check
    double dodgeChance = calculateDodgeChance(attacker, target);

    if (random.nextDouble() < dodgeChance) {
      events.add(
          MatchEventSnapshot.dodge(
              attacker.hero.name(),
              target.hero.name(),
              attacker.getUniqueId(),
              target.getUniqueId(),
              logicalTime.getAndIncrement()));
      return;
    }

    // 2. Critical Hit Check
    double critChance = calculateCriticalHitChance(attacker);
    boolean isCrit = random.nextDouble() < critChance;

    // 3. Damage Calculation
    int damage = calculateDamage(attacker, target, roundSpec, isCrit);
    target.currentHp -= damage;

    if (isCrit) {
      events.add(
          MatchEventSnapshot.criticalHit(
              attacker.hero.name(),
              target.hero.name(),
              attacker.getUniqueId(),
              target.getUniqueId(),
              damage,
              logicalTime.getAndIncrement()));
    } else {
      events.add(
          MatchEventSnapshot.hit(
              attacker.hero.name(),
              target.hero.name(),
              attacker.getUniqueId(),
              target.getUniqueId(),
              damage,
              logicalTime.getAndIncrement()));
    }

    if (target.currentHp <= 0) {
      target.currentHp = 0;
      events.add(
          MatchEventSnapshot.ko(
              target.hero.name(),
              attacker.getUniqueId(),
              target.getUniqueId(),
              logicalTime.getAndIncrement()));
    }
  }

  private void sortHeroesBySpeed(List<BattleHeroUseCase> heroes) {
    heroes.sort(
        Comparator.comparingInt((BattleHeroUseCase bh) -> bh.hero.powerstats().speed())
            .thenComparingInt(bh -> bh.hero.powerstats().combat())
            .reversed());
  }

  @Nullable UUID checkWinCondition(List<BattleHeroUseCase> allHeroes, UUID teamAId, UUID teamBId) {
    boolean teamADead = isTeamWipedOut(allHeroes, teamAId);
    boolean teamBDead = isTeamWipedOut(allHeroes, teamBId);

    return switch ((teamADead ? 1 : 0) | (teamBDead ? 2 : 0)) {
      case 3 -> null; // Both teams dead (simultaneous KO - unlikely with serial logic)
      case 1 -> teamBId; // Team A dead, Team B wins
      case 2 -> teamAId; // Team B dead, Team A wins
      default -> null; // Battle continues
    };
  }

  private @Nullable BattleHeroUseCase findTarget(
      BattleHeroUseCase attacker,
      List<BattleHeroUseCase> allHeroes,
      UUID teamAId,
      UUID teamBId,
      Random random) {
    UUID opposingTeamId = attacker.teamId.equals(teamAId) ? teamBId : teamAId;
    List<BattleHeroUseCase> targets =
        allHeroes.stream().filter(h -> h.teamId.equals(opposingTeamId) && h.isAlive()).toList();

    if (targets.isEmpty()) {
      return null;
    }
    return selectTarget(targets, random);
  }

  public double calculateDodgeChance(BattleHeroUseCase attacker, BattleHeroUseCase target) {
    double combatTarget = Math.max(0.0, target.hero.powerstats().combat());
    double combatAttacker = Math.max(0.0, attacker.hero.powerstats().combat());
    double dodgeChance = combatTarget / (combatTarget + combatAttacker + 0.1);
    // Cap dodge at 50% max, and floor at 0%
    return Math.clamp(dodgeChance, 0.0, 0.5);
  }

  public double calculateCriticalHitChance(BattleHeroUseCase attacker) {
    return Math.clamp(attacker.hero.powerstats().intelligence() / 200.0, 0.0, 0.5);
  }

  private BattleHeroUseCase selectTarget(List<BattleHeroUseCase> targets, Random random) {
    // Find min HP
    int minHp = targets.stream().mapToInt(h -> h.currentHp).min().orElse(0);
    List<BattleHeroUseCase> lowestHpTargets =
        targets.stream().filter(h -> h.currentHp == minHp).toList();

    if (lowestHpTargets.size() == 1) {
      return lowestHpTargets.getFirst();
    }

    return lowestHpTargets.get(random.nextInt(lowestHpTargets.size()));
  }

  int calculateDamage(
      BattleHeroUseCase attacker, BattleHeroUseCase target, RoundSpec roundSpec, boolean isCrit) {
    double multiplier = 1.0;

    // Apply Tag Modifiers
    if (roundSpec.tagModifiers() != null) {
      for (String tag : attacker.hero.tags()) {
        if (roundSpec.tagModifiers().containsKey(tag)) {
          multiplier *= roundSpec.tagModifiers().get(tag);
        }
      }
    }

    if (isCrit) {
      multiplier *= 1.5;
    }

    // Power for attack, Strength for defense
    int baseAtk = Math.max(0, attacker.hero.powerstats().power());
    int targetDef = Math.max(0, target.hero.powerstats().strength());

    int rawDamage = (int) (baseAtk * multiplier - (targetDef * DAMAGE_DEF_FACTOR));
    return Math.max(1, rawDamage);
  }

  boolean isTeamWipedOut(List<BattleHeroUseCase> allHeroes, UUID teamId) {
    return allHeroes.stream()
        .filter(h -> h.teamId.equals(teamId))
        .noneMatch(BattleHeroUseCase::isAlive);
  }

  static class BattleHeroUseCase {
    Hero hero;
    UUID teamId;
    int currentHp;

    public BattleHeroUseCase(Hero hero, UUID teamId) {
      this.hero = hero;
      this.teamId = teamId;
      this.currentHp = hero.powerstats().durability();
    }

    public boolean isAlive() {
      return currentHp > 0;
    }

    public String getUniqueId() {
      return teamId.toString() + "_" + hero.id();
    }
  }
}
