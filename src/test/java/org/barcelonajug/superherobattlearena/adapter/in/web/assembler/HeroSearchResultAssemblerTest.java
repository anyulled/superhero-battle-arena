package org.barcelonajug.superherobattlearena.adapter.in.web.assembler;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.barcelonajug.superherobattlearena.domain.Hero;
import org.barcelonajug.superherobattlearena.domain.HeroSearchCriteria;
import org.barcelonajug.superherobattlearena.domain.HeroSearchResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.hateoas.PagedModel;

class HeroSearchResultAssemblerTest {

  private HeroModelAssembler heroModelAssembler;
  private HeroSearchResultAssembler assembler;

  @BeforeEach
  void setUp() {
    heroModelAssembler = new HeroModelAssembler();
    assembler = new HeroSearchResultAssembler(heroModelAssembler);
  }

  @Test
  void shouldConvertEmptySearchResultToPagedModel() {
    HeroSearchResult searchResult = new HeroSearchResult(List.of(), 0, 0, 0, 20, false, false);
    HeroSearchCriteria criteria = HeroSearchCriteria.builder().page(0).size(20).build();

    PagedModel<?> result = assembler.toModelWithPagination(searchResult, criteria);

    assertThat(result.getContent()).isEmpty();
    assertThat(result.getLinks()).hasSize(3);
    assertThat(result.getLink("self")).isPresent();
    assertThat(result.getLink("first")).isPresent();
    assertThat(result.getLink("last")).isPresent();
  }

  @Test
  void shouldConvertSearchResultWithHeroesToPagedModel() {
    List<Hero> heroes =
        List.of(createTestHero(1, "Batman", "batman"), createTestHero(2, "Superman", "superman"));
    HeroSearchResult searchResult = new HeroSearchResult(heroes, 2, 1, 0, 20, false, false);
    HeroSearchCriteria criteria = HeroSearchCriteria.builder().page(0).size(20).build();

    PagedModel<?> result = assembler.toModelWithPagination(searchResult, criteria);

    assertThat(result.getContent()).hasSize(2);
    assertThat(result.getLinks()).hasSize(3);
  }

  @Test
  void shouldIncludePaginationLinksForMiddlePage() {
    List<Hero> heroes = List.of(createTestHero(1, "Hero", "hero"));
    HeroSearchResult searchResult = new HeroSearchResult(heroes, 50, 5, 2, 10, true, true);
    HeroSearchCriteria criteria = HeroSearchCriteria.builder().page(2).size(10).build();

    PagedModel<?> result = assembler.toModelWithPagination(searchResult, criteria);

    assertThat(result.getLinks()).hasSize(5);
    assertThat(result.getLink("self")).isPresent();
    assertThat(result.getLink("first")).isPresent();
    assertThat(result.getLink("prev")).isPresent();
    assertThat(result.getLink("next")).isPresent();
    assertThat(result.getLink("last")).isPresent();
  }

  @Test
  void shouldNotIncludePrevOnFirstPage() {
    List<Hero> heroes = List.of(createTestHero(1, "Hero", "hero"));
    HeroSearchResult searchResult = new HeroSearchResult(heroes, 50, 5, 0, 10, true, false);
    HeroSearchCriteria criteria = HeroSearchCriteria.builder().page(0).size(10).build();

    PagedModel<?> result = assembler.toModelWithPagination(searchResult, criteria);

    assertThat(result.getLink("prev")).isEmpty();
    assertThat(result.getLink("first")).isPresent();
    assertThat(result.getLink("next")).isPresent();
  }

  @Test
  void shouldNotIncludeNextOnLastPage() {
    List<Hero> heroes = List.of(createTestHero(1, "Hero", "hero"));
    HeroSearchResult searchResult = new HeroSearchResult(heroes, 10, 1, 0, 10, false, false);
    HeroSearchCriteria criteria = HeroSearchCriteria.builder().page(0).size(10).build();

    PagedModel<?> result = assembler.toModelWithPagination(searchResult, criteria);

    assertThat(result.getLink("next")).isEmpty();
    assertThat(result.getLink("last")).isPresent();
  }

  @Test
  void shouldPreserveFiltersInPaginationLinks() {
    List<Hero> heroes = List.of(createTestHero(1, "Batman", "batman"));
    HeroSearchResult searchResult = new HeroSearchResult(heroes, 20, 2, 1, 10, true, true);
    HeroSearchCriteria criteria =
        HeroSearchCriteria.builder().name("Bat").alignment("good").page(1).size(10).build();

    PagedModel<?> result = assembler.toModelWithPagination(searchResult, criteria);

    assertThat(result.getLink("self")).isPresent();
    String selfLink = result.getLink("self").get().getHref();
    assertThat(selfLink).contains("name=Bat").contains("alignment=good").contains("page=1");

    assertThat(result.getLink("prev")).isPresent();
    String prevLink = result.getLink("prev").get().getHref();
    assertThat(prevLink).contains("name=Bat").contains("alignment=good").contains("page=0");
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
}
