package org.barcelonajug.superherobattlearena.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.barcelonajug.superherobattlearena.application.port.out.SuperheroRepositoryPort;
import org.barcelonajug.superherobattlearena.domain.Hero;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RosterUseCaseTest {

  private RosterUseCase rosterUseCase;
  private SuperheroRepositoryPort repository;

  @BeforeEach
  void setUp() {
    repository = mock(SuperheroRepositoryPort.class);
    rosterUseCase = new RosterUseCase(repository);
  }

  @Test
  void shouldGetAllHeroes() {
    when(repository.findAll())
        .thenReturn(
            List.of(
                Hero.builder()
                    .id(1)
                    .name("Test")
                    .slug("test")
                    .role("Fighter")
                    .alignment("good")
                    .publisher("Marvel")
                    .build()));
    assertThat(rosterUseCase.getAllHeroes()).isNotEmpty();
  }

  @Test
  void shouldGetHeroById() {
    Hero mockHero =
        Hero.builder()
            .id(1)
            .name("A-Bomb")
            .slug("1-a-bomb")
            .powerstats(
                Hero.PowerStats.builder()
                    .durability(80)
                    .strength(100)
                    .power(24)
                    .speed(17)
                    .intelligence(38)
                    .combat(64)
                    .build())
            .role("Fighter")
            .cost(15)
            .alignment("good")
            .publisher("Marvel Comics")
            .build();
    when(repository.findById(1)).thenReturn(Optional.of(mockHero));

    Optional<Hero> hero = rosterUseCase.getHero(1);
    assertThat(hero).isPresent();
    assertThat(hero.get().name()).isEqualTo("A-Bomb");
    assertThat(hero.get().powerstats().durability()).isGreaterThan(0);
  }

  @Test
  void shouldReturnEmptyForUnknownId() {
    when(repository.findById(999)).thenReturn(Optional.empty());
    assertThat(rosterUseCase.getHero(999)).isEmpty();
  }
}
