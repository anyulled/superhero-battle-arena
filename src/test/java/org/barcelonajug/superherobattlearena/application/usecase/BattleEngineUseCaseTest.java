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
        new Hero(
            1,
            "Strongman",
            "strongman",
            new Hero.PowerStats(100, 10, 10, 10, 10, 10),
            "Bruiser",
            10,
            "good",
            "Marvel",
            null,
            null,
            List.of(),
            new Hero.Images(null, null, null, null));
    Hero h2 =
        new Hero(
            2,
            "Fastguy",
            "fastguy",
            new Hero.PowerStats(50, 5, 5, 20, 5, 5),
            "Infiltrator",
            10,
            "bad",
            "DC",
            null,
            null,
            List.of(),
            new Hero.Images(null, null, null, null));

    UUID matchId = UUID.randomUUID();
    UUID teamA = UUID.randomUUID();
    UUID teamB = UUID.randomUUID();
    long seed = 12345L;
    RoundSpec spec = new RoundSpec("Test", 1, 100, null, null, null, null, "Arena");

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
        new Hero(
            1,
            "Slow",
            "slow",
            new Hero.PowerStats(20, 10, 10, 10, 10, 10),
            "Bruiser",
            10,
            "neutral",
            "Image",
            null,
            null,
            List.of(),
            new Hero.Images(null, null, null, null));
    Hero h2 =
        new Hero(
            2,
            "Fast",
            "fast",
            new Hero.PowerStats(20, 10, 10, 20, 10, 10),
            "Infiltrator",
            10,
            "neutral",
            "Image",
            null,
            null,
            List.of(),
            new Hero.Images(null, null, null, null));

    UUID matchId = UUID.randomUUID();
    UUID teamA = UUID.randomUUID();
    UUID teamB = UUID.randomUUID();
    RoundSpec spec = new RoundSpec("Test", 1, 100, null, null, null, null, "Arena");

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
        new Hero(
            1,
            "Pyro",
            "pyro",
            new Hero.PowerStats(100, 10, 10, 10, 10, 10),
            "Blaster",
            10,
            "bad",
            "Dark Horse",
            null,
            null,
            List.of("Fire"),
            new Hero.Images(null, null, null, null));
    Hero h2 =
        new Hero(
            2,
            "Dummy",
            "dummy",
            new Hero.PowerStats(1000, 10, 10, 10, 10, 10),
            "Target",
            10,
            "good",
            "Marvel",
            null,
            null,
            List.of(),
            new Hero.Images(null, null, null, null));

    UUID matchId = UUID.randomUUID();
    UUID teamA = UUID.randomUUID();
    UUID teamB = UUID.randomUUID();
    RoundSpec spec = new RoundSpec("Test", 1, 100, null, null, null, Map.of("Fire", 10.0), "Arena");

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
