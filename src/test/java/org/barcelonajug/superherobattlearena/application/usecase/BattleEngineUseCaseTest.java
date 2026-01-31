package org.barcelonajug.superherobattlearena.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.barcelonajug.superherobattlearena.domain.Hero;
import org.barcelonajug.superherobattlearena.domain.SimulationResult;
import org.barcelonajug.superherobattlearena.domain.json.RoundSpec;
import org.junit.jupiter.api.Test;

class BattleEngineUseCaseTest {

  private final BattleEngineUseCase battleEngineUseCase = new BattleEngineUseCase();

  @Test
  void shouldBeDeterministic() {
    Hero h1 =
        Hero.builder()
            .id(1)
            .name("Strongman")
            .slug("strongman")
            .powerstats(
                Hero.PowerStats.builder()
                    .durability(100)
                    .strength(10)
                    .power(10)
                    .speed(10)
                    .intelligence(10)
                    .combat(10)
                    .build())
            .role("Bruiser")
            .alignment("good")
            .publisher("Marvel")
            .build();
    Hero h2 =
        Hero.builder()
            .id(2)
            .name("Fastguy")
            .slug("fastguy")
            .powerstats(
                Hero.PowerStats.builder()
                    .durability(50)
                    .strength(5)
                    .power(5)
                    .speed(20)
                    .intelligence(5)
                    .combat(5)
                    .build())
            .role("Infiltrator")
            .alignment("bad")
            .publisher("DC")
            .build();

    UUID matchId = UUID.randomUUID();
    UUID teamA = UUID.randomUUID();
    UUID teamB = UUID.randomUUID();
    long seed = 12345L;
    RoundSpec spec =
        new RoundSpec("Test", 1, 100, Map.of(), Map.of(), List.of(), Map.of(), "Arena");

    SimulationResult result1 =
        battleEngineUseCase.simulate(matchId, List.of(h1), List.of(h2), seed, teamA, teamB, spec);
    SimulationResult result2 =
        battleEngineUseCase.simulate(matchId, List.of(h1), List.of(h2), seed, teamA, teamB, spec);

    assertThat(result1.events()).hasSameSizeAs(result2.events());
    assertThat(result1.winnerTeamId()).isEqualTo(result2.winnerTeamId());

    // Exact content match
    for (int i = 0; i < result1.events().size(); i++) {
      assertThat(result1.events().get(i))
          .usingRecursiveComparison()
          .isEqualTo(result2.events().get(i));
    }
  }

  @Test
  void fastHeroShouldGoFirst() {
    // H1 Spd 10, H2 Spd 20. H2 should hit H1 first.
    Hero h1 =
        Hero.builder()
            .id(1)
            .name("Slow")
            .slug("slow")
            .powerstats(
                Hero.PowerStats.builder()
                    .durability(20)
                    .strength(10)
                    .power(10)
                    .speed(10)
                    .intelligence(10)
                    .combat(10)
                    .build())
            .role("Bruiser")
            .alignment("neutral")
            .publisher("Image")
            .build();
    Hero h2 =
        Hero.builder()
            .id(2)
            .name("Fast")
            .slug("fast")
            .powerstats(
                Hero.PowerStats.builder()
                    .durability(20)
                    .strength(10)
                    .power(10)
                    .speed(20)
                    .intelligence(10)
                    .combat(10)
                    .build())
            .role("Infiltrator")
            .alignment("neutral")
            .publisher("Image")
            .build();

    UUID matchId = UUID.randomUUID();
    UUID teamA = UUID.randomUUID();
    UUID teamB = UUID.randomUUID();
    RoundSpec spec =
        new RoundSpec("Test", 1, 100, Map.of(), Map.of(), List.of(), Map.of(), "Arena");

    // Team A has Slow, Team B has Fast.
    SimulationResult result =
        battleEngineUseCase.simulate(matchId, List.of(h1), List.of(h2), 1L, teamA, teamB, spec);

    // Find first HIT event
    var firstHit = result.events().stream().filter(e -> "HIT".equals(e.type())).findFirst();
    assertThat(firstHit).isPresent();
    // Attacker should be H2 (id 2). Composite ID: teamB_2
    assertThat(firstHit.get().actorId()).isEqualTo(teamB + "_2");
  }

  @Test
  void shouldUseTagModifiers() {
    // H1 vs H2. H1 has "Fire" tag. Modifiers: "Fire" -> 10.0 (Massive boost to
    // ensure one shot or high diff)
    Hero h1 =
        Hero.builder()
            .id(1)
            .name("Pyro")
            .slug("pyro")
            .powerstats(
                Hero.PowerStats.builder()
                    .durability(100)
                    .strength(10)
                    .power(10)
                    .speed(10)
                    .intelligence(10)
                    .combat(10)
                    .build())
            .role("Blaster")
            .alignment("bad")
            .publisher("Dark Horse")
            .tags(List.of("Fire"))
            .build();
    Hero h2 =
        Hero.builder()
            .id(2)
            .name("Dummy")
            .slug("dummy")
            .powerstats(
                Hero.PowerStats.builder()
                    .durability(1000)
                    .strength(10)
                    .power(10)
                    .speed(10)
                    .intelligence(10)
                    .combat(10)
                    .build())
            .role("Target")
            .alignment("good")
            .publisher("Marvel")
            .build();

    UUID matchId = UUID.randomUUID();
    UUID teamA = UUID.randomUUID();
    UUID teamB = UUID.randomUUID();
    RoundSpec spec =
        new RoundSpec("Test", 1, 100, Map.of(), Map.of(), List.of(), Map.of("Fire", 10.0), "Arena");

    // Pyro hits Dummy.
    // Base Damage: 10 + modifier?
    // Formula: max(1, attacker.ATK * multiplier - (target.DEF * 0.6))
    // ATK 10 * 10.0 = 100. DEF 10 * 0.6 = 6. Damage = 94.
    // Without modifier: 10 - 6 = 4.

    SimulationResult result =
        battleEngineUseCase.simulate(matchId, List.of(h1), List.of(h2), 1L, teamA, teamB, spec);

    // Find first HIT from Pyro (Team A, ID 1)
    var hit =
        result.events().stream()
            .filter(e -> "HIT".equals(e.type()) && (teamA + "_1").equals(e.actorId()))
            .findFirst();

    assertThat(hit).isPresent();
    assertThat(hit.get().value()).isEqualTo(94);
  }
}
