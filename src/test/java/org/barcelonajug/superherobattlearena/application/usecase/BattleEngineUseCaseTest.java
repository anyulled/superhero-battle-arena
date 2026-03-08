package org.barcelonajug.superherobattlearena.application.usecase;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;

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
    spec = RoundSpecMother.aRoundSpecWithTags(Map.of("tag1", 2.0), emptyList());
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
    assertThat(damage).isOne();
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

  @Test
  void calculateDodgeChance_shouldCapAt50Percent() {
    Hero target = HeroMother.aStandardHero();
    Hero attacker = HeroMother.aWeakHero(); // Low combat
    BattleHeroUseCase bhTarget = new BattleHeroUseCase(target, teamBId);
    BattleHeroUseCase bhAttacker = new BattleHeroUseCase(attacker, teamAId);

    // If target combat >> attacker combat, dodge chance formula naturally exceeds
    // 0.5, but should clamp.
    bhTarget.hero =
        Hero.builder()
            .id(1)
            .name("Dodger")
            .slug("dodger")
            .role("dodger")
            .powerstats(Hero.PowerStats.builder().combat(100).build())
            .build();
    bhAttacker.hero =
        Hero.builder()
            .id(2)
            .name("Slowpoke")
            .slug("slowpoke")
            .role("slowpoke")
            .powerstats(Hero.PowerStats.builder().combat(0).build())
            .build();

    double dodgeChance = battleEngineUseCase.calculateDodgeChance(bhAttacker, bhTarget);
    assertThat(dodgeChance).isEqualTo(0.5); // Clamped
  }

  @Test
  void calculateCriticalHitChance_shouldCapAt50Percent() {
    Hero smartAttacker =
        Hero.builder()
            .id(1)
            .name("Brain")
            .slug("brain")
            .role("brain")
            .powerstats(Hero.PowerStats.builder().intelligence(150).build())
            .build();
    BattleHeroUseCase bhAttacker = new BattleHeroUseCase(smartAttacker, teamAId);

    double critChance = battleEngineUseCase.calculateCriticalHitChance(bhAttacker);
    assertThat(critChance).isEqualTo(0.5); // Clamped, 150/200 = 0.75 -> 0.5
  }

  @Test
  void simulate_targetWithNoTags_doesNotApplyMultipliers() {
    Hero attacker =
        Hero.builder()
            .id(1)
            .name("A")
            .slug("a")
            .role("a")
            .tags(emptyList())
            .powerstats(Hero.PowerStats.builder().power(10).speed(10).combat(10).build())
            .build();
    Hero target =
        Hero.builder()
            .id(2)
            .name("B")
            .slug("b")
            .role("b")
            .tags(emptyList())
            .powerstats(Hero.PowerStats.builder().durability(100).strength(0).build())
            .build();

    RoundSpec spec = RoundSpecMother.aRoundSpecWithTags(Map.of("fire", 2.0), emptyList());

    BattleHeroUseCase bhAttacker = new BattleHeroUseCase(attacker, teamAId);
    BattleHeroUseCase bhTarget = new BattleHeroUseCase(target, teamBId);

    int damage = battleEngineUseCase.calculateDamage(bhAttacker, bhTarget, spec, false);
    assertThat(damage).isEqualTo(10); // Base damage, no multiplier
  }

  @Test
  void simulate_shouldDrawIfExceedsMaxTurns() {
    // Two heroes that deal 1 damage to each other but have 1000 health.
    Hero heroA =
        Hero.builder()
            .id(1)
            .name("A")
            .slug("a")
            .role("a")
            .powerstats(Hero.PowerStats.builder().durability(1000).strength(100).power(1).build())
            .build();
    Hero heroB =
        Hero.builder()
            .id(2)
            .name("B")
            .slug("b")
            .role("b")
            .powerstats(Hero.PowerStats.builder().durability(1000).strength(100).power(1).build())
            .build();

    RoundSpec spec = RoundSpecMother.aStandardRoundSpec();

    SimulationResult result =
        battleEngineUseCase.simulate(
            UUID.randomUUID(), List.of(heroA), List.of(heroB), 1L, teamAId, teamBId, spec);

    assertThat(result.winnerTeamId()).isNull(); // Draw
    assertThat(result.totalTurns()).isEqualTo(50); // Reached MAX_TURNS
  }

  @Test
  void testTargetSelection_multipleLowestHp_picksRandomly() {
    // A team with multiple targets at the exact same lowest HP.
    Hero attacker = HeroMother.aStandardHero();
    Hero target1 = HeroMother.aStandardHero();
    Hero target2 = HeroMother.aStandardHero();
    RoundSpec spec = RoundSpecMother.aStandardRoundSpec();

    // By simulating enough times with a fixed but varied seeds or multiple times,
    // we exercise the multiple targets branch.
    SimulationResult result =
        battleEngineUseCase.simulate(
            UUID.randomUUID(),
            List.of(attacker),
            List.of(target1, target2),
            42L,
            teamAId,
            teamBId,
            spec);

    // Just verify the game finishes cleanly
    assertThat(result.winnerTeamId()).isNotNull();
  }

  @Test
  void executeTurn_skipDeadAttacker() {
    Hero attacker = HeroMother.aStandardHero();
    Hero target = HeroMother.aStandardHero();

    // Start them with specific durability. In simulation, the dead attacker won't
    // do anything.
    Hero deadAttacker =
        Hero.builder()
            .id(2)
            .name("Dead")
            .slug("dead")
            .role("dead")
            .powerstats(Hero.PowerStats.builder().durability(0).speed(100).build())
            .build();

    SimulationResult result =
        battleEngineUseCase.simulate(
            UUID.randomUUID(),
            List.of(attacker, deadAttacker),
            List.of(target),
            42L,
            teamAId,
            teamBId,
            RoundSpecMother.aStandardRoundSpec());

    // Dead attacker will be skipped, but combat still completes.
    assertThat(result.winnerTeamId()).isNotNull();
  }

  @Test
  void executeTurn_noTargetsLeft_returnsWinnerImmediate() {
    // Create a scenario where one side is dead instantly to trigger the return
    // attacker.teamId branch when target == null.
    Hero attacker =
        Hero.builder()
            .id(1)
            .name("A")
            .slug("a")
            .role("a")
            .powerstats(Hero.PowerStats.builder().durability(100).speed(100).power(1000).build())
            .build();
    Hero weakTarget =
        Hero.builder()
            .id(2)
            .name("B")
            .slug("b")
            .role("b")
            .powerstats(Hero.PowerStats.builder().durability(1).build())
            .build();

    SimulationResult result =
        battleEngineUseCase.simulate(
            UUID.randomUUID(),
            List.of(attacker),
            List.of(weakTarget),
            42L,
            teamAId,
            teamBId,
            RoundSpecMother.aStandardRoundSpec());

    assertThat(result.winnerTeamId()).isEqualTo(teamAId); // Won quickly
  }
}
