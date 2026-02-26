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
import org.mockito.Mockito;

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
                    .powerstats(Hero.PowerStats.builder().build())
                    .tags(List.of())
                    .images(Hero.Images.builder().build())
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
            .tags(List.of())
            .images(Hero.Images.builder().build())
            .build();
    when(repository.findById(1)).thenReturn(Optional.of(mockHero));

    Optional<Hero> hero = rosterUseCase.getHero(1);
    assertThat(hero).isPresent();
    assertThat(hero.get().name()).isEqualTo("A-Bomb");
    assertThat(hero.get().powerstats().durability()).isPositive();
  }

  @Test
  void shouldReturnEmptyForUnknownId() {
    when(repository.findById(999)).thenReturn(Optional.empty());
    assertThat(rosterUseCase.getHero(999)).isEmpty();
  }

  @Test
  void shouldGetAllHeroesWithPagination() {
    when(repository.findAll(0, 10)).thenReturn(List.of());
    assertThat(rosterUseCase.getAllHeroes(0, 10)).isEmpty();
    Mockito.verify(repository).findAll(0, 10);
  }

  @Test
  void shouldGetHeroesByIds() {
    List<Integer> ids = List.of(1, 2);
    when(repository.findByIds(ids)).thenReturn(List.of());
    assertThat(rosterUseCase.getHeroes(ids)).isEmpty();
    Mockito.verify(repository).findByIds(ids);
  }

  @Test
  void shouldSearchHeroes() {
    String term = "Spider";
    when(repository.searchByName(term)).thenReturn(List.of());
    assertThat(rosterUseCase.searchHeroes(term)).isEmpty();
    Mockito.verify(repository).searchByName(term);
  }

  @Test
  void shouldFilterHeroes() {
    String alignment = "good";
    String publisher = "Marvel Comics";
    when(repository.findByAlignmentAndPublisher(alignment, publisher)).thenReturn(List.of());
    assertThat(rosterUseCase.filterHeroes(alignment, publisher)).isEmpty();
    Mockito.verify(repository).findByAlignmentAndPublisher(alignment, publisher);
  }

  @Test
  void shouldCountHeroes() {
    when(repository.count()).thenReturn(100L);
    assertThat(rosterUseCase.countHeroes()).isEqualTo(100L);
    Mockito.verify(repository).count();
  }
}
