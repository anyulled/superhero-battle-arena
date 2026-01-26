package org.barcelonajug.superherobattlearena.application.usecase;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.barcelonajug.superherobattlearena.domain.Hero;
import org.barcelonajug.superherobattlearena.domain.SimulationResult;
import org.barcelonajug.superherobattlearena.domain.json.RoundSpec;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BattleEngineTest {

    private final BattleEngine battleEngine = new BattleEngine();

    @Test
    void shouldBeDeterministic() {
        Hero h1 = new Hero(1, "Strongman", new Hero.PowerStats(100, 10, 10, 10), "Bruiser", 10, List.of());
        Hero h2 = new Hero(2, "Fastguy", new Hero.PowerStats(50, 5, 5, 20), "Infiltrator", 10, List.of());

        UUID matchId = UUID.randomUUID();
        UUID teamA = UUID.randomUUID();
        UUID teamB = UUID.randomUUID();
        long seed = 12345L;
        RoundSpec spec = new RoundSpec("Test", 1, 100, null, null, null, null, "Arena");

        SimulationResult result1 = battleEngine.simulate(matchId, List.of(h1), List.of(h2), seed, teamA, teamB, spec);
        SimulationResult result2 = battleEngine.simulate(matchId, List.of(h1), List.of(h2), seed, teamA, teamB, spec);

        assertThat(result1.events()).hasSameSizeAs(result2.events());
        assertThat(result1.winnerTeamId()).isEqualTo(result2.winnerTeamId());

        // Exact content match
        for (int i = 0; i < result1.events().size(); i++) {
            assertThat(result1.events().get(i)).usingRecursiveComparison().isEqualTo(result2.events().get(i));
        }
    }

    @Test
    void fastHeroShouldGoFirst() {
        // H1 Spd 10, H2 Spd 20. H2 should hit H1 first.
        Hero h1 = new Hero(1, "Slow", new Hero.PowerStats(20, 10, 10, 10), "Bruiser", 10, List.of());
        Hero h2 = new Hero(2, "Fast", new Hero.PowerStats(20, 10, 10, 20), "Infiltrator", 10, List.of());

        UUID matchId = UUID.randomUUID();
        UUID teamA = UUID.randomUUID();
        UUID teamB = UUID.randomUUID();
        RoundSpec spec = new RoundSpec("Test", 1, 100, null, null, null, null, "Arena");

        // Team A has Slow, Team B has Fast.
        SimulationResult result = battleEngine.simulate(matchId, List.of(h1), List.of(h2), 1L, teamA, teamB, spec);

        // Find first HIT event
        var firstHit = result.events().stream().filter(e -> "HIT".equals(e.type())).findFirst();
        assertThat(firstHit).isPresent();
        // Attacker should be H2 (id 2)
        assertThat(firstHit.get().actorId()).isEqualTo(2);
    }

    @Test
    void shouldUseTagModifiers() {
        // H1 vs H2. H1 has "Fire" tag. Modifiers: "Fire" -> 10.0 (Massive boost to
        // ensure one shot or high diff)
        Hero h1 = new Hero(1, "Pyro", new Hero.PowerStats(100, 10, 10, 10), "Blaster", 10, List.of("Fire"));
        Hero h2 = new Hero(2, "Dummy", new Hero.PowerStats(1000, 10, 10, 10), "Target", 10, List.of());

        UUID matchId = UUID.randomUUID();
        UUID teamA = UUID.randomUUID();
        UUID teamB = UUID.randomUUID();
        RoundSpec spec = new RoundSpec("Test", 1, 100, null, null, null, Map.of("Fire", 10.0), "Arena");

        // Pyro hits Dummy.
        // Base Damage: 10 + modifier?
        // Formula: max(1, attacker.ATK * multiplier - (target.DEF * 0.6))
        // ATK 10 * 10.0 = 100. DEF 10 * 0.6 = 6. Damage = 94.
        // Without modifier: 10 - 6 = 4.

        SimulationResult result = battleEngine.simulate(matchId, List.of(h1), List.of(h2), 1L, teamA, teamB, spec);

        // Find first HIT from Pyro
        var hit = result.events().stream()
                .filter(e -> "HIT".equals(e.type()) && Integer.valueOf(1).equals(e.actorId()))
                .findFirst();

        assertThat(hit).isPresent();
        assertThat(hit.get().value()).isEqualTo(94);
    }
}
