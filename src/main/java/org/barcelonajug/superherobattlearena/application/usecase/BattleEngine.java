package org.barcelonajug.superherobattlearena.application.usecase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;
import org.barcelonajug.superherobattlearena.domain.Hero;
import org.barcelonajug.superherobattlearena.domain.SimulationResult;
import org.barcelonajug.superherobattlearena.domain.json.MatchEvent;
import org.barcelonajug.superherobattlearena.domain.json.RoundSpec;
import org.springframework.stereotype.Service;

@Service
public class BattleEngine {

    private static final int MAX_TURNS = 50;
    private static final double DAMAGE_DEF_FACTOR = 0.6;

    public SimulationResult simulate(UUID matchId, List<Hero> teamAHeroes, List<Hero> teamBHeroes, long roundSeed,
            UUID teamAId, UUID teamBId, RoundSpec roundSpec) {
        Random random = new Random(roundSeed + matchId.hashCode());

        List<BattleHero> allHeroes = new ArrayList<>();
        teamAHeroes.forEach(h -> allHeroes.add(new BattleHero(h, teamAId)));
        teamBHeroes.forEach(h -> allHeroes.add(new BattleHero(h, teamBId)));

        List<MatchEvent> events = new ArrayList<>();
        long logicalTime = 0;

        events.add(new MatchEvent("MATCH_START", logicalTime++, "Match started", null, null, 0));

        int turn = 0;
        UUID winnerId = null;

        while (turn < MAX_TURNS && winnerId == null) {
            turn++;
            events.add(new MatchEvent("TURN_START", logicalTime++, "Turn " + turn + " started", null, null, turn));

            // Sort by Speed Descending. Stability with ID for determinism.
            allHeroes.sort(Comparator.comparingInt((BattleHero bh) -> bh.hero.powerstats().speed())
                    .reversed()
                    .thenComparingInt(bh -> bh.hero.id()));

            for (BattleHero attacker : allHeroes) {
                if (!attacker.isAlive())
                    continue;

                // Check if match ended mid-turn (e.g. last standing defender died)
                if (isTeamWipedOut(allHeroes, attacker.teamId == teamAId ? teamBId : teamAId)) {
                    winnerId = attacker.teamId;
                    break;
                }

                // Identify Targets (Opposing Team)
                UUID opposingTeamId = attacker.teamId == teamAId ? teamBId : teamAId;
                List<BattleHero> targets = allHeroes.stream()
                        .filter(h -> h.teamId.equals(opposingTeamId) && h.isAlive())
                        .collect(Collectors.toList());

                if (targets.isEmpty()) {
                    winnerId = attacker.teamId;
                    break;
                }

                // Select Target: Lowest HP. Tied -> Random
                BattleHero target = selectTarget(targets, random);

                // Calculate Damage
                int damage = calculateDamage(attacker, target, roundSpec);

                // Apply Damage
                target.currentHp -= damage;
                events.add(new MatchEvent("HIT", logicalTime++,
                        attacker.hero.name() + " hits " + target.hero.name() + " for " + damage,
                        attacker.getUniqueId(),
                        target.getUniqueId(), damage));

                if (target.currentHp <= 0) {
                    target.currentHp = 0;
                    events.add(new MatchEvent("KO", logicalTime++, target.hero.name() + " is KO!",
                            attacker.getUniqueId(),
                            target.getUniqueId(), 0));
                }
            }

            // Check win condition at end of turn as well
            if (winnerId == null) {
                boolean teamADead = isTeamWipedOut(allHeroes, teamAId);
                boolean teamBDead = isTeamWipedOut(allHeroes, teamBId);

                if (teamADead && teamBDead) {
                    // Draw? or Simultaneous KO? The logic implies turn-based, so someone dies
                    // first.
                    // But if we check strictly after loop.
                    // The loop checks break on win. So this block handles case where turn ends and
                    // maybe
                    // we want to double check strictly.
                    // Actually, if Team A wipes out Team B, loop breaks.
                } else if (teamADead) {
                    winnerId = teamBId;
                } else if (teamBDead) {
                    winnerId = teamAId;
                }
            }
        }

        if (winnerId != null) {
            events.add(new MatchEvent("MATCH_END", logicalTime++, "Winner: " + winnerId, null, null, 0));
        } else {
            events.add(new MatchEvent("MATCH_END", logicalTime++, "Draw - Max turns reached", null, null, 0));
        }

        return new SimulationResult(winnerId, turn, events);
    }

    private BattleHero selectTarget(List<BattleHero> targets, Random random) {
        // Find min HP
        int minHp = targets.stream().mapToInt(h -> h.currentHp).min().orElse(0);
        List<BattleHero> lowestHpTargets = targets.stream()
                .filter(h -> h.currentHp == minHp)
                .collect(Collectors.toList());

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
        return allHeroes.stream()
                .filter(h -> h.teamId.equals(teamId))
                .noneMatch(BattleHero::isAlive);
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
