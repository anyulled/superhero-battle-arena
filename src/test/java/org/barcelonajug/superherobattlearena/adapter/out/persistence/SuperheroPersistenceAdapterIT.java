package org.barcelonajug.superherobattlearena.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.OffsetDateTime;
import java.util.List;
import org.barcelonajug.superherobattlearena.adapter.out.persistence.entity.SuperheroAppearanceEntity;
import org.barcelonajug.superherobattlearena.adapter.out.persistence.entity.SuperheroEntity;
import org.barcelonajug.superherobattlearena.adapter.out.persistence.entity.SuperheroPowerStatsEntity;
import org.barcelonajug.superherobattlearena.adapter.out.persistence.repository.SpringDataSuperheroRepository;
import org.barcelonajug.superherobattlearena.domain.Hero;
import org.barcelonajug.superherobattlearena.domain.HeroSearchCriteria;
import org.barcelonajug.superherobattlearena.domain.HeroSearchCriteria.SortDirection;
import org.barcelonajug.superherobattlearena.domain.HeroSearchResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
@Transactional
class SuperheroPersistenceAdapterIT {

  @Container
  static PostgreSQLContainer<?> postgres =
      new PostgreSQLContainer<>("postgres:17-alpine")
          .withDatabaseName("testdb")
          .withUsername("test")
          .withPassword("test");

  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
  }

  @Autowired private SpringDataSuperheroRepository repository;

  @Autowired private SuperheroPersistenceAdapter adapter;

  @BeforeEach
  void setUp() {
    repository.deleteAll();
  }

  @Test
  void shouldSearchHeroesUsingJoinedFilters() {
    repository.saveAll(
        List.of(
            superhero(
                1,
                "Batman",
                "batman",
                "good",
                "DC Comics",
                60,
                80,
                75,
                60,
                85,
                90,
                "Male",
                "Human"),
            superhero(
                2,
                "Superman",
                "superman",
                "good",
                "DC Comics",
                90,
                100,
                100,
                100,
                100,
                95,
                "Male",
                "Kryptonian")));

    HeroSearchCriteria criteria =
        HeroSearchCriteria.builder()
            .alignment("good")
            .publisher("DC Comics")
            .gender("Male")
            .race("Human")
            .minCost(50)
            .maxCost(65)
            .sortBy("cost")
            .sortDirection(SortDirection.ASC)
            .page(0)
            .size(10)
            .build();

    HeroSearchResult result = adapter.search(criteria);

    assertThat(result.totalElements()).isOne();
    assertThat(result.heroes()).extracting(Hero::name).containsExactly("Batman");
    assertThat(result.hasNext()).isFalse();
    assertThat(result.hasPrevious()).isFalse();
  }

  @Test
  void shouldSortHeroesByPowerStatsField() {
    repository.saveAll(
        List.of(
            superhero(
                1,
                "Batman",
                "batman",
                "good",
                "DC Comics",
                60,
                80,
                75,
                60,
                85,
                90,
                "Male",
                "Human"),
            superhero(
                2,
                "Superman",
                "superman",
                "good",
                "DC Comics",
                90,
                100,
                100,
                100,
                100,
                95,
                "Male",
                "Kryptonian")));

    HeroSearchResult result =
        adapter.search(
            HeroSearchCriteria.builder()
                .sortBy("power")
                .sortDirection(SortDirection.DESC)
                .page(0)
                .size(10)
                .build());

    assertThat(result.heroes()).extracting(Hero::name).containsExactly("Superman", "Batman");
  }

  @Test
  void shouldUseDefaultSortingWhenSortByIsNotProvided() {
    repository.saveAll(
        List.of(
            superhero(
                3,
                "Wonder Woman",
                "wonder-woman",
                "good",
                "DC Comics",
                70,
                90,
                85,
                80,
                90,
                95,
                "Female",
                "Amazon"),
            superhero(
                1,
                "Batman",
                "batman",
                "good",
                "DC Comics",
                60,
                80,
                75,
                60,
                85,
                90,
                "Male",
                "Human"),
            superhero(
                2,
                "Superman",
                "superman",
                "good",
                "DC Comics",
                90,
                100,
                100,
                100,
                100,
                95,
                "Male",
                "Kryptonian")));

    HeroSearchResult result =
        adapter.search(
            HeroSearchCriteria.builder().alignment("good").page(0).size(10).build());

    assertThat(result.totalElements()).isEqualTo(3);
    assertThat(result.heroes()).extracting(Hero::name).containsExactly("Batman", "Superman", "Wonder Woman");
    assertThat(result.hasNext()).isFalse();
    assertThat(result.hasPrevious()).isFalse();
  }

  private SuperheroEntity superhero(
      int id,
      String name,
      String slug,
      String alignment,
      String publisher,
      int cost,
      int power,
      int strength,
      int speed,
      int durability,
      int combat,
      String gender,
      String race) {
    SuperheroEntity superhero = new SuperheroEntity();
    superhero.setId(id);
    superhero.setName(name);
    superhero.setSlug(slug);
    superhero.setAlignment(alignment);
    superhero.setPublisher(publisher);
    superhero.setCreatedAt(OffsetDateTime.parse("2026-01-01T00:00:00Z"));
    superhero.setPowerStats(powerStats(id, cost, power, strength, speed, durability, combat));
    superhero.setAppearance(appearance(id, gender, race));
    return superhero;
  }

  private SuperheroPowerStatsEntity powerStats(
      int id, int cost, int power, int strength, int speed, int durability, int combat) {
    SuperheroPowerStatsEntity powerStats = new SuperheroPowerStatsEntity();
    powerStats.setSuperheroId(id);
    powerStats.setCost(cost);
    powerStats.setPower(power);
    powerStats.setStrength(strength);
    powerStats.setSpeed(speed);
    powerStats.setDurability(durability);
    powerStats.setCombat(combat);
    powerStats.setIntelligence(85);
    return powerStats;
  }

  private SuperheroAppearanceEntity appearance(int id, String gender, String race) {
    SuperheroAppearanceEntity appearance = new SuperheroAppearanceEntity();
    appearance.setSuperheroId(id);
    appearance.setGender(gender);
    appearance.setRace(race);
    return appearance;
  }
}
