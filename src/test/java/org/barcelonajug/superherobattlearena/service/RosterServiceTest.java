package org.barcelonajug.superherobattlearena.application.usecase;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Optional;
import org.barcelonajug.superherobattlearena.domain.Hero;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RosterServiceTest {

    private RosterService rosterService;

    @BeforeEach
    void setUp() throws IOException {
        rosterService = new RosterService(new ObjectMapper());
        rosterService.loadRoster();
    }

    @Test
    void shouldLoadHeroesFromFile() {
        assertThat(rosterService.getAllHeroes()).isNotEmpty();
    }

    @Test
    void shouldGetHeroById() {
        Optional<Hero> hero = rosterService.getHero(1);
        assertThat(hero).isPresent();
        assertThat(hero.get().name()).isEqualTo("A-Bomb");
    }

    @Test
    void shouldReturnEmptyForUnknownId() {
        assertThat(rosterService.getHero(999)).isEmpty();
    }
}
