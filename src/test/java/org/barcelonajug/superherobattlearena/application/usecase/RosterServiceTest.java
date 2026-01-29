package org.barcelonajug.superherobattlearena.application.usecase;

import java.util.Optional;
import java.util.List;
import org.barcelonajug.superherobattlearena.application.port.out.SuperheroRepositoryPort;
import org.barcelonajug.superherobattlearena.domain.Hero;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class RosterServiceTest {

    private RosterService rosterService;
    private SuperheroRepositoryPort repository;

    @BeforeEach
    void setUp() {
        repository = Mockito.mock(SuperheroRepositoryPort.class);
        rosterService = new RosterService(repository);
    }

    @Test
    void shouldGetAllHeroes() {
        when(repository.findAll()).thenReturn(List
                .of(new Hero(1, "Test", "test", null, "Fighter", 10, "good", "Marvel", null, null, List.of(), null)));
        assertThat(rosterService.getAllHeroes()).isNotEmpty();
    }

    @Test
    void shouldGetHeroById() {
        Hero mockHero = new Hero(1, "A-Bomb", "1-a-bomb", new Hero.PowerStats(80, 100, 24, 17, 38, 64), "Fighter", 15,
                "good", "Marvel Comics", null, null, List.of(), null);
        when(repository.findById(1)).thenReturn(Optional.of(mockHero));

        Optional<Hero> hero = rosterService.getHero(1);
        assertThat(hero).isPresent();
        assertThat(hero.get().name()).isEqualTo("A-Bomb");
        assertThat(hero.get().powerstats().durability()).isGreaterThan(0);
    }

    @Test
    void shouldReturnEmptyForUnknownId() {
        when(repository.findById(999)).thenReturn(Optional.empty());
        assertThat(rosterService.getHero(999)).isEmpty();
    }
}
