package org.barcelonajug.superherobattlearena.application.usecase;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import org.barcelonajug.superherobattlearena.domain.Hero;
import org.barcelonajug.superherobattlearena.domain.SimulationResult;
import org.barcelonajug.superherobattlearena.domain.json.MatchEvent;
import org.barcelonajug.superherobattlearena.domain.json.RoundSpec;
import org.springframework.stereotype.Service;

@Service
public class BattleEngine {

  private static final int MAX_TURNS = 50;
  private static final double DAMAGE_DEF_FACTOR = 0.6;

  public SimulationResult simulate(
      UUID matchId,
      List<Hero> teamAHeroes,
      List<Hero> teamBHeroes,
      long roundSeed,
      UUID teamAId,
      UUID teamBId,
      RoundSpec roundSpec) {
    Random random = new Random(roundSeed + matchId.hashCode());

    List<BattleHero> allHeroes = new ArrayList<>();
    teamAHeroes.forEach(h -> allHeroes.add(new BattleHero(h, teamAId)));
    teamBHeroes.forEach(h -> allHeroes.add(new BattleHero(h, teamBId)));

    List<MatchEvent> events = new ArrayList<>();
    java.util.concurrent.atomic.AtomicLong logicalTime =
        new java.util.concurrent.atomic.AtomicLong(0);

    events.add(
        new MatchEvent(
            "MATCH_START", logicalTime.getAndIncrement(), "Match started", null, null, 0));

    int turn = 0;
    UUID winnerId = null;

    sortHeroesBySpeed(allHeroes);
    allHeroes = List.copyOf(allHeroes);

    while (turn < MAX_TURNS && winnerId == null) {
      turn++;
      events.add(
          new MatchEvent(
              "TURN_START",
              logicalTime.getAndIncrement(),
              "Turn " + turn + " started",
              null,
              null,
              turn));

      winnerId = executeTurn(allHeroes, teamAId, teamBId, roundSpec, random, events, logicalTime);

      if (winnerId == null) {
        winnerId = checkWinCondition(allHeroes, teamAId, teamBId);
      }
    }

    if (winnerId != null) {
      events.add(
          new MatchEvent(
              "MATCH_END", logicalTime.getAndIncrement(), "Winner: " + winnerId, null, null, 0));
    } else {
      events.add(
          new MatchEvent(
              "MATCH_END",
              logicalTime.getAndIncrement(),
              "Draw - Max turns reached",
              null,
              null,
              0));
    }

    return new SimulationResult(winnerId, turn, events);
  }

  private UUID executeTurn(
      List<BattleHero> allHeroes,
      UUID teamAId,
      UUID teamBId,
      RoundSpec roundSpec,
      Random random,
      List<MatchEvent> events,
      java.util.concurrent.atomic.AtomicLong logicalTime) {
    for (BattleHero attacker : allHeroes) {
      if (!attacker.isAlive()) {
        continue;
      }

      UUID winnerId = checkWinCondition(allHeroes, teamAId, teamBId);
      if (winnerId != null) {
        return winnerId;
      }

      BattleHero target = findTarget(attacker, allHeroes, teamAId, teamBId, random);
      if (target == null) {
        return attacker.teamId;
      }

      performAttack(attacker, target, roundSpec, events, logicalTime);
    }
    return null;
  }

  private void performAttack(
      BattleHero attacker,
      BattleHero target,
      RoundSpec roundSpec,
      List<MatchEvent> events,
      java.util.concurrent.atomic.AtomicLong logicalTime) {
    int damage = calculateDamage(attacker, target, roundSpec);
    target.currentHp -= damage;

    events.add(
        new MatchEvent(
            "HIT",
            logicalTime.getAndIncrement(),
            attacker.hero.name() + " hits " + target.hero.name() + " for " + damage,
            attacker.getUniqueId(),
            target.getUniqueId(),
            damage));

    if (target.currentHp <= 0) {
      target.currentHp = 0;
      events.add(
          new MatchEvent(
              "KO",
              logicalTime.getAndIncrement(),
              target.hero.name() + " is KO!",
              attacker.getUniqueId(),
              target.getUniqueId(),
              0));
    }
  }

  private void sortHeroesBySpeed(List<BattleHero> heroes) {
    heroes.sort(
        Comparator.comparingInt((BattleHero bh) -> bh.hero.powerstats().speed())
            .reversed()
            .thenComparingInt(bh -> bh.hero.id()));
  }

  private UUID checkWinCondition(List<BattleHero> allHeroes, UUID teamAId, UUID teamBId) {
    boolean teamADead = isTeamWipedOut(allHeroes, teamAId);
    boolean teamBDead = isTeamWipedOut(allHeroes, teamBId);

    if (teamADead && teamBDead) {
      // Simultaneous KO case (unlikely with current serial logic, but safe handling)
      return null;
    } else if (teamADead) {
      return teamBId;
    } else if (teamBDead) {
      return teamAId;
    }
    return null; // Battle continues
  }

  private BattleHero findTarget(
      BattleHero attacker, List<BattleHero> allHeroes, UUID teamAId, UUID teamBId, Random random) {
    UUID opposingTeamId = attacker.teamId.equals(teamAId) ? teamBId : teamAId;
    List<BattleHero> targets =
        allHeroes.stream().filter(h -> h.teamId.equals(opposingTeamId) && h.isAlive()).toList();

    if (targets.isEmpty()) {
      return null;
    }
    return selectTarget(targets, random);
  }

  private BattleHero selectTarget(List<BattleHero> targets, Random random) {
    // Find min HP
    int minHp = targets.stream().mapToInt(h -> h.currentHp).min().orElse(0);
    List<BattleHero> lowestHpTargets = targets.stream().filter(h -> h.currentHp == minHp).toList();

    if (lowestHpTargets.size() == 1) {
      return lowestHpTargets.get(0);
    }

    return lowestHpTargets.get(random.nextInt(lowestHpTargets.size()));
  }

  private int calculateDamage(BattleHero attacker, BattleHero target, RoundSpec roundSpec) {
    double multiplier = 1.0;

    // Apply Tag Modifiers
    if (roundSpec.tagModifiers() != null) {
      for (String tag : attacker.hero.tags()) {
        if (roundSpec.tagModifiers().containsKey(tag)) {
          multiplier *= roundSpec.tagModifiers().get(tag);
        }
      }
    }

    int baseAtk = attacker.hero.powerstats().strength();
    int targetDef = target.hero.powerstats().power();

    int rawDamage = (int) (baseAtk * multiplier - (targetDef * DAMAGE_DEF_FACTOR));
    return Math.max(1, rawDamage);
  }

  private boolean isTeamWipedOut(List<BattleHero> allHeroes, UUID teamId) {
    return allHeroes.stream().filter(h -> h.teamId.equals(teamId)).noneMatch(BattleHero::isAlive);
  }

  private static class BattleHero {
    Hero hero;
    UUID teamId;
    int currentHp;

    public BattleHero(Hero hero, UUID teamId) {
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
