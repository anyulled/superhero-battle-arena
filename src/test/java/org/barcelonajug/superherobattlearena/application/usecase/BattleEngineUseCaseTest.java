package org.barcelonajug.superherobattlearena.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.barcelonajug.superherobattlearena.application.usecase.BattleEngineUseCase.BattleHeroUseCase;
import org.barcelonajug.superherobattlearena.domain.Hero;
import org.barcelonajug.superherobattlearena.domain.SimulationResult;
import org.barcelonajug.superherobattlearena.domain.json.RoundSpec;
import org.barcelonajug.superherobattlearena.domain.mother.HeroMother;
import org.barcelonajug.superherobattlearena.domain.mother.RoundSpecMother;
import org.junit.jupiter.api.Test;

class BattleEngineUseCaseTest {

  private final BattleEngineUseCase battleEngineUseCase = new BattleEngineUseCase();
  private final UUID teamAId = UUID.randomUUID();
  private final UUID teamBId = UUID.randomUUID();

  @Test
  void calculateDamage_shouldUseCorrectFormula() {
    Hero attacker = HeroMother.aStandardHero();
    Hero target = HeroMother.aStandardHero();

    BattleHeroUseCase bhAttacker = new BattleHeroUseCase(attacker, teamAId);
    BattleHeroUseCase bhTarget = new BattleHeroUseCase(target, teamBId);
    RoundSpec spec = RoundSpecMother.aStandardRoundSpec();

    int damage = battleEngineUseCase.calculateDamage(bhAttacker, bhTarget, spec, false);
    assertThat(damage).isEqualTo(38);

    // Test with multiplier
    spec = RoundSpecMother.aRoundSpecWithTags(Map.of("tag1", 2.0), Collections.emptyList());
    Hero attackerWithTag = HeroMother.aHeroWithTags(List.of("tag1"));
    bhAttacker = new BattleHeroUseCase(attackerWithTag, teamAId);

    // rawDamage = (int)(80 * 2.0 - 42) = 160 - 42 = 118
    damage = battleEngineUseCase.calculateDamage(bhAttacker, bhTarget, spec, false);
    assertThat(damage).isEqualTo(118);

    // Test with crit
    // rawDamage = (int)(80 * 2.0 * 1.5 - 42) = 240 - 42 = 198
    damage = battleEngineUseCase.calculateDamage(bhAttacker, bhTarget, spec, true);
    assertThat(damage).isEqualTo(198);

    // Test min damage
    Hero weakAttacker = HeroMother.aWeakHero();
    bhAttacker = new BattleHeroUseCase(weakAttacker, teamAId);
    damage = battleEngineUseCase.calculateDamage(bhAttacker, bhTarget, spec, false);
    assertThat(damage).isEqualTo(1);
  }

  @Test
  void isTeamWipedOut_shouldReturnCorrectValue() {
    Hero hero = HeroMother.aStandardHero();
    BattleHeroUseCase bh = new BattleHeroUseCase(hero, teamAId);
    List<BattleHeroUseCase> heroes = List.of(bh);

    assertThat(battleEngineUseCase.isTeamWipedOut(heroes, teamAId)).isFalse();
    assertThat(battleEngineUseCase.isTeamWipedOut(heroes, teamBId)).isTrue();

    bh.currentHp = 0;
    assertThat(battleEngineUseCase.isTeamWipedOut(heroes, teamAId)).isTrue();
  }

  @Test
  void checkWinCondition_shouldIdentifyWinner() {
    Hero heroA = HeroMother.aStandardHero();
    Hero heroB = HeroMother.aStandardHero();
    BattleHeroUseCase bhA = new BattleHeroUseCase(heroA, teamAId);
    BattleHeroUseCase bhB = new BattleHeroUseCase(heroB, teamBId);
    List<BattleHeroUseCase> heroes = List.of(bhA, bhB);

    assertThat(battleEngineUseCase.checkWinCondition(heroes, teamAId, teamBId)).isNull();

    bhA.currentHp = 0;
    assertThat(battleEngineUseCase.checkWinCondition(heroes, teamAId, teamBId)).isEqualTo(teamBId);

    bhA.currentHp = 10;
    bhB.currentHp = 0;
    assertThat(battleEngineUseCase.checkWinCondition(heroes, teamAId, teamBId)).isEqualTo(teamAId);

    bhA.currentHp = 0;
    bhB.currentHp = 0;
    assertThat(battleEngineUseCase.checkWinCondition(heroes, teamAId, teamBId)).isNull(); // Draw
  }

  @Test
  void simulate_shouldLogPeriodically() {
    Hero heroA =
        Hero.builder()
            .id(1)
            .name("Tank A")
            .slug("tank-a")
            .role("Tank")
            .powerstats(
                Hero.PowerStats.builder()
                    .durability(100)
                    .strength(10)
                    .power(10)
                    .speed(10)
                    .intelligence(10)
                    .combat(10)
                    .build())
            .build();
    Hero heroB =
        Hero.builder()
            .id(2)
            .name("Tank B")
            .slug("tank-b")
            .role("Tank")
            .powerstats(
                Hero.PowerStats.builder()
                    .durability(100)
                    .strength(10)
                    .power(10)
                    .speed(10)
                    .intelligence(10)
                    .combat(10)
                    .build())
            .build();

    RoundSpec spec = RoundSpecMother.aStandardRoundSpec();

    SimulationResult result =
        battleEngineUseCase.simulate(
            UUID.randomUUID(), List.of(heroA), List.of(heroB), 123L, teamAId, teamBId, spec);

    assertThat(result.totalTurns()).isGreaterThanOrEqualTo(10);
  }
}
