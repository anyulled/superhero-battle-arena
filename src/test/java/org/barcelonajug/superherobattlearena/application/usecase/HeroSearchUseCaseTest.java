package org.barcelonajug.superherobattlearena.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.barcelonajug.superherobattlearena.application.port.out.SuperheroRepositoryPort;
import org.barcelonajug.superherobattlearena.domain.Hero;
import org.barcelonajug.superherobattlearena.domain.HeroSearchCriteria;
import org.barcelonajug.superherobattlearena.domain.HeroSearchResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class HeroSearchUseCaseTest {

  private HeroSearchUseCase heroSearchUseCase;
  private SuperheroRepositoryPort repository;

  @BeforeEach
  void setUp() {
    repository = mock(SuperheroRepositoryPort.class);
    heroSearchUseCase = new HeroSearchUseCase(repository);
  }

  @Test
  void shouldSearchHeroesWithEmptyCriteria() {
    HeroSearchCriteria criteria = HeroSearchCriteria.builder().page(0).size(20).build();
    HeroSearchResult expectedResult = new HeroSearchResult(List.of(), 0, 0, 0, 20, false, false);
    when(repository.search(criteria)).thenReturn(expectedResult);

    HeroSearchResult result = heroSearchUseCase.search(criteria);

    assertThat(result.heroes()).isEmpty();
    assertThat(result.totalElements()).isZero();
    verify(repository).search(criteria);
  }

  @Test
  void shouldSearchHeroesWithNameFilter() {
    HeroSearchCriteria criteria =
        HeroSearchCriteria.builder().name("Batman").page(0).size(20).build();
    Hero hero = createTestHero(1, "Batman", "batman");
    HeroSearchResult expectedResult =
        new HeroSearchResult(List.of(hero), 1, 1, 0, 20, false, false);
    when(repository.search(criteria)).thenReturn(expectedResult);

    HeroSearchResult result = heroSearchUseCase.search(criteria);

    assertThat(result.heroes()).hasSize(1);
    assertThat(result.heroes().getFirst().name()).isEqualTo("Batman");
    assertThat(result.totalElements()).isOne();
    verify(repository).search(criteria);
  }

  @Test
  void shouldSearchHeroesWithAlignmentFilter() {
    HeroSearchCriteria criteria =
        HeroSearchCriteria.builder().alignment("good").page(0).size(20).build();
    List<Hero> heroes = List.of(createTestHero(1, "Superman", "superman"));
    HeroSearchResult expectedResult = new HeroSearchResult(heroes, 1, 1, 0, 20, false, false);
    when(repository.search(criteria)).thenReturn(expectedResult);

    HeroSearchResult result = heroSearchUseCase.search(criteria);

    assertThat(result.heroes()).hasSize(1);
    assertThat(result.heroes().getFirst().alignment()).isEqualTo("good");
    verify(repository).search(criteria);
  }

  @Test
  void shouldSearchHeroesWithRoleFilter() {
    HeroSearchCriteria criteria =
        HeroSearchCriteria.builder().role("Tank").page(0).size(20).build();
    Hero hero = createTestHeroWithRole(1, "Hulk", "hulk", "Tank");
    HeroSearchResult expectedResult =
        new HeroSearchResult(List.of(hero), 1, 1, 0, 20, false, false);
    when(repository.search(criteria)).thenReturn(expectedResult);

    HeroSearchResult result = heroSearchUseCase.search(criteria);

    assertThat(result.heroes()).hasSize(1);
    assertThat(result.heroes().getFirst().role()).isEqualTo("Tank");
    verify(repository).search(criteria);
  }

  @Test
  void shouldSearchHeroesWithPowerStatsFilter() {
    HeroSearchCriteria criteria =
        HeroSearchCriteria.builder().minPower(50).maxPower(100).page(0).size(20).build();
    List<Hero> heroes = List.of(createTestHero(1, "Thor", "thor"));
    HeroSearchResult expectedResult = new HeroSearchResult(heroes, 1, 1, 0, 20, false, false);
    when(repository.search(criteria)).thenReturn(expectedResult);

    HeroSearchResult result = heroSearchUseCase.search(criteria);

    assertThat(result.heroes()).hasSize(1);
    verify(repository).search(criteria);
  }

  @Test
  void shouldSearchHeroesWithPagination() {
    HeroSearchCriteria criteria = HeroSearchCriteria.builder().page(1).size(10).build();
    List<Hero> heroes =
        List.of(createTestHero(1, "Hero1", "hero1"), createTestHero(2, "Hero2", "hero2"));
    HeroSearchResult expectedResult = new HeroSearchResult(heroes, 25, 3, 1, 10, true, true);
    when(repository.search(criteria)).thenReturn(expectedResult);

    HeroSearchResult result = heroSearchUseCase.search(criteria);

    assertThat(result.heroes()).hasSize(2);
    assertThat(result.currentPage()).isOne();
    assertThat(result.totalPages()).isEqualTo(3);
    assertThat(result.hasNext()).isTrue();
    assertThat(result.hasPrevious()).isTrue();
    verify(repository).search(criteria);
  }

  @Test
  void shouldSearchHeroesWithAllFilters() {
    HeroSearchCriteria criteria =
        HeroSearchCriteria.builder()
            .name("Man")
            .alignment("good")
            .publisher("Marvel")
            .role("Fighter")
            .gender("Male")
            .race("Human")
            .minCost(10)
            .maxCost(50)
            .minPower(30)
            .maxPower(100)
            .minStrength(40)
            .maxStrength(100)
            .minSpeed(20)
            .maxSpeed(80)
            .minIntelligence(30)
            .maxIntelligence(90)
            .minDurability(40)
            .maxDurability(100)
            .minCombat(20)
            .maxCombat(80)
            .page(0)
            .size(20)
            .sortBy("name")
            .sortDirection(HeroSearchCriteria.SortDirection.ASC)
            .build();

    HeroSearchResult expectedResult = new HeroSearchResult(List.of(), 0, 0, 0, 20, false, false);
    when(repository.search(criteria)).thenReturn(expectedResult);

    HeroSearchResult result = heroSearchUseCase.search(criteria);

    assertThat(result.heroes()).isEmpty();
    Mockito.verify(repository).search(criteria);
  }

  private Hero createTestHero(int id, String name, String slug) {
    return Hero.builder()
        .id(id)
        .name(name)
        .slug(slug)
        .role("Fighter")
        .cost(10)
        .alignment("good")
        .powerstats(
            Hero.PowerStats.builder()
                .durability(50)
                .strength(50)
                .power(50)
                .speed(50)
                .intelligence(50)
                .combat(50)
                .build())
        .tags(List.of())
        .images(Hero.Images.builder().build())
        .build();
  }

  private Hero createTestHeroWithRole(int id, String name, String slug, String role) {
    return Hero.builder()
        .id(id)
        .name(name)
        .slug(slug)
        .role(role)
        .cost(10)
        .alignment("good")
        .powerstats(
            Hero.PowerStats.builder()
                .durability(50)
                .strength(50)
                .power(50)
                .speed(50)
                .intelligence(50)
                .combat(50)
                .build())
        .tags(List.of())
        .images(Hero.Images.builder().build())
        .build();
  }
}
