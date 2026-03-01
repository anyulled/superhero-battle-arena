package org.barcelonajug.superherobattlearena.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Combinators;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;
import org.barcelonajug.superherobattlearena.domain.Hero;
import org.barcelonajug.superherobattlearena.domain.json.RoundSpec;

class BattleEngineUseCasePropertyTest {

  private final BattleEngineUseCase battleEngine = new BattleEngineUseCase();

  @Property
  void damageIsAlwaysAtLeastOne(
      @ForAll("battleHeroes") BattleEngineUseCase.BattleHeroUseCase attacker,
      @ForAll("battleHeroes") BattleEngineUseCase.BattleHeroUseCase target,
      @ForAll("roundSpecs") RoundSpec roundSpec,
      @ForAll boolean isCrit) {

    int damage = battleEngine.calculateDamage(attacker, target, roundSpec, isCrit);

    assertThat(damage).isGreaterThanOrEqualTo(1);
  }

  @Property
  void criticalHitNeverDealsLessDamageThanRegularHit(
      @ForAll("battleHeroes") BattleEngineUseCase.BattleHeroUseCase attacker,
      @ForAll("battleHeroes") BattleEngineUseCase.BattleHeroUseCase target,
      @ForAll("roundSpecs") RoundSpec roundSpec) {

    int regularDamage = battleEngine.calculateDamage(attacker, target, roundSpec, false);
    int criticalDamage = battleEngine.calculateDamage(attacker, target, roundSpec, true);

    assertThat(criticalDamage).isGreaterThanOrEqualTo(regularDamage);
  }

  @Property
  void dodgeChanceIsBetweenZeroAndFiftyPercent(
      @ForAll("battleHeroes") BattleEngineUseCase.BattleHeroUseCase attacker,
      @ForAll("battleHeroes") BattleEngineUseCase.BattleHeroUseCase target) {

    double dodgeChance = battleEngine.calculateDodgeChance(attacker, target);

    assertThat(dodgeChance).isGreaterThanOrEqualTo(0.0).isLessThanOrEqualTo(0.5);
  }

  @Property
  void criticalHitChanceIsBetweenZeroAndFiftyPercent(
      @ForAll("battleHeroes") BattleEngineUseCase.BattleHeroUseCase attacker) {

    double critChance = battleEngine.calculateCriticalHitChance(attacker);

    assertThat(critChance).isGreaterThanOrEqualTo(0.0).isLessThanOrEqualTo(0.5);
  }

  @Provide
  Arbitrary<BattleEngineUseCase.BattleHeroUseCase> battleHeroes() {
    Arbitrary<Hero.PowerStats> powerStatsArbitrary =
        Combinators.combine(
                Arbitraries.integers(),
                Arbitraries.integers(),
                Arbitraries.integers(),
                Arbitraries.integers(),
                Arbitraries.integers(),
                Arbitraries.integers())
            .as(
                (dur, str, pow, spd, intel, cbt) ->
                    Hero.PowerStats.builder()
                        .durability(dur)
                        .strength(str)
                        .power(pow)
                        .speed(spd)
                        .intelligence(intel)
                        .combat(cbt)
                        .build());

    return powerStatsArbitrary.map(
        stats ->
            new BattleEngineUseCase.BattleHeroUseCase(
                Hero.builder()
                    .id(1)
                    .name("Hero")
                    .slug("hero")
                    .role("Fighter")
                    .powerstats(stats)
                    .build(),
                UUID.randomUUID()));
  }

  @Provide
  Arbitrary<RoundSpec> roundSpecs() {
    Arbitrary<Double> modifiers = Arbitraries.doubles().between(0.5, 2.0);
    return modifiers.map(
        m ->
            new RoundSpec(
                "Test Round",
                5,
                1000,
                Collections.emptyMap(),
                Collections.emptyMap(),
                Collections.emptyList(),
                Map.of("TECH", m, "MAGIC", m),
                "ARENA"));
  }
}
