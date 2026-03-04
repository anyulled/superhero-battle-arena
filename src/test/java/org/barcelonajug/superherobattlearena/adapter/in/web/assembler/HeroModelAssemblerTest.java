package org.barcelonajug.superherobattlearena.adapter.in.web.assembler;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.barcelonajug.superherobattlearena.domain.Hero;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.hateoas.EntityModel;

class HeroModelAssemblerTest {

  private HeroModelAssembler assembler;

  @BeforeEach
  void setUp() {
    assembler = new HeroModelAssembler();
  }

  @Test
  void shouldConvertHeroToEntityModelWithLinks() {
    Hero hero = createTestHero(1, "Batman", "batman");

    EntityModel<Hero> result = assembler.toModel(hero);

    assertThat(result.getContent()).isNotNull();
    assertThat(result.getContent().name()).isEqualTo("Batman");
    assertThat(result.getLinks()).hasSize(2);
    assertThat(result.getLink("self")).isPresent();
    assertThat(result.getLink("heroes")).isPresent();
  }

  @Test
  void shouldGenerateCorrectSelfLink() {
    Hero hero = createTestHero(42, "Spider-Man", "spider-man");

    EntityModel<Hero> result = assembler.toModel(hero);

    assertThat(result.getLink("self")).isPresent();
    assertThat(result.getLink("self").get().getHref()).contains("/api/heroes/42");
  }

  @Test
  void shouldHandleHeroWithNullImages() {
    Hero hero =
        Hero.builder()
            .id(1)
            .name("Test")
            .slug("test")
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
            .build();

    EntityModel<Hero> result = assembler.toModel(hero);

    assertThat(result.getContent()).isNotNull();
    assertThat(result.getContent().images()).isNull();
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
