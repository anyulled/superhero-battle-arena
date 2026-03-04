package org.barcelonajug.superherobattlearena.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class HeroSearchCriteriaTest {

  @Test
  void shouldCreateDefaultCriteria() {
    HeroSearchCriteria criteria = HeroSearchCriteria.builder().build();

    assertThat(criteria.page()).isZero();
    assertThat(criteria.size()).isEqualTo(20);
    assertThat(criteria.sortBy()).isEqualTo("id");
    assertThat(criteria.sortDirection()).isEqualTo(HeroSearchCriteria.SortDirection.ASC);
  }

  @Test
  void shouldCreateCriteriaWithAllFilters() {
    HeroSearchCriteria criteria =
        HeroSearchCriteria.builder()
            .name("Batman")
            .alignment("good")
            .publisher("DC")
            .role("Fighter")
            .gender("Male")
            .race("Human")
            .minCost(10)
            .maxCost(50)
            .minPower(30)
            .maxPower(100)
            .page(1)
            .size(50)
            .sortBy("name")
            .sortDirection(HeroSearchCriteria.SortDirection.DESC)
            .build();

    assertThat(criteria.name()).isEqualTo("Batman");
    assertThat(criteria.alignment()).isEqualTo("good");
    assertThat(criteria.publisher()).isEqualTo("DC");
    assertThat(criteria.role()).isEqualTo("Fighter");
    assertThat(criteria.gender()).isEqualTo("Male");
    assertThat(criteria.race()).isEqualTo("Human");
    assertThat(criteria.minCost()).isEqualTo(10);
    assertThat(criteria.maxCost()).isEqualTo(50);
    assertThat(criteria.minPower()).isEqualTo(30);
    assertThat(criteria.maxPower()).isEqualTo(100);
    assertThat(criteria.page()).isOne();
    assertThat(criteria.size()).isEqualTo(50);
    assertThat(criteria.sortBy()).isEqualTo("name");
    assertThat(criteria.sortDirection()).isEqualTo(HeroSearchCriteria.SortDirection.DESC);
  }

  @Test
  void shouldHandleNegativePage() {
    HeroSearchCriteria criteria = HeroSearchCriteria.builder().page(-5).build();
    assertThat(criteria.page()).isZero();
  }

  @Test
  void shouldHandleZeroPage() {
    HeroSearchCriteria criteria = HeroSearchCriteria.builder().page(0).build();
    assertThat(criteria.page()).isZero();
  }

  @Test
  void shouldHandleSizeBelowMinimum() {
    HeroSearchCriteria criteria = HeroSearchCriteria.builder().size(0).build();
    assertThat(criteria.size()).isEqualTo(20);
  }

  @Test
  void shouldHandleSizeAboveMaximum() {
    HeroSearchCriteria criteria = HeroSearchCriteria.builder().size(200).build();
    assertThat(criteria.size()).isEqualTo(20);
  }

  @Test
  void shouldHaveNameFilter() {
    HeroSearchCriteria withName = HeroSearchCriteria.builder().name("test").build();
    assertThat(withName.hasNameFilter()).isTrue();

    HeroSearchCriteria withoutName = HeroSearchCriteria.builder().build();
    assertThat(withoutName.hasNameFilter()).isFalse();
  }

  @Test
  void shouldHaveNameFilterWithBlank() {
    HeroSearchCriteria withBlank = HeroSearchCriteria.builder().name("   ").build();
    assertThat(withBlank.hasNameFilter()).isFalse();
  }

  @Test
  void shouldHaveAlignmentFilter() {
    HeroSearchCriteria withAlignment = HeroSearchCriteria.builder().alignment("good").build();
    assertThat(withAlignment.hasAlignmentFilter()).isTrue();

    HeroSearchCriteria withoutAlignment = HeroSearchCriteria.builder().build();
    assertThat(withoutAlignment.hasAlignmentFilter()).isFalse();
  }

  @Test
  void shouldHavePublisherFilter() {
    HeroSearchCriteria withPublisher = HeroSearchCriteria.builder().publisher("Marvel").build();
    assertThat(withPublisher.hasPublisherFilter()).isTrue();

    HeroSearchCriteria withoutPublisher = HeroSearchCriteria.builder().build();
    assertThat(withoutPublisher.hasPublisherFilter()).isFalse();
  }

  @Test
  void shouldHaveRoleFilter() {
    HeroSearchCriteria withRole = HeroSearchCriteria.builder().role("Tank").build();
    assertThat(withRole.hasRoleFilter()).isTrue();

    HeroSearchCriteria withoutRole = HeroSearchCriteria.builder().build();
    assertThat(withoutRole.hasRoleFilter()).isFalse();
  }

  @Test
  void shouldHaveGenderFilter() {
    HeroSearchCriteria withGender = HeroSearchCriteria.builder().gender("Male").build();
    assertThat(withGender.hasGenderFilter()).isTrue();

    HeroSearchCriteria withoutGender = HeroSearchCriteria.builder().build();
    assertThat(withoutGender.hasGenderFilter()).isFalse();
  }

  @Test
  void shouldHaveRaceFilter() {
    HeroSearchCriteria withRace = HeroSearchCriteria.builder().race("Human").build();
    assertThat(withRace.hasRaceFilter()).isTrue();

    HeroSearchCriteria withoutRace = HeroSearchCriteria.builder().build();
    assertThat(withoutRace.hasRaceFilter()).isFalse();
  }

  @Test
  void shouldHaveCostRange() {
    HeroSearchCriteria withMinCost = HeroSearchCriteria.builder().minCost(10).build();
    assertThat(withMinCost.hasCostRange()).isTrue();

    HeroSearchCriteria withMaxCost = HeroSearchCriteria.builder().maxCost(50).build();
    assertThat(withMaxCost.hasCostRange()).isTrue();

    HeroSearchCriteria withoutCost = HeroSearchCriteria.builder().build();
    assertThat(withoutCost.hasCostRange()).isFalse();
  }

  @Test
  void shouldHavePowerStatsFilterWithMinPower() {
    HeroSearchCriteria criteria = HeroSearchCriteria.builder().minPower(50).build();
    assertThat(criteria.hasPowerStatsFilter()).isTrue();
  }

  @Test
  void shouldHavePowerStatsFilterWithMaxPower() {
    HeroSearchCriteria criteria = HeroSearchCriteria.builder().maxPower(100).build();
    assertThat(criteria.hasPowerStatsFilter()).isTrue();
  }

  @Test
  void shouldHavePowerStatsFilterWithMinStrength() {
    HeroSearchCriteria criteria = HeroSearchCriteria.builder().minStrength(50).build();
    assertThat(criteria.hasPowerStatsFilter()).isTrue();
  }

  @Test
  void shouldHavePowerStatsFilterWithMaxStrength() {
    HeroSearchCriteria criteria = HeroSearchCriteria.builder().maxStrength(100).build();
    assertThat(criteria.hasPowerStatsFilter()).isTrue();
  }

  @Test
  void shouldHavePowerStatsFilterWithMinSpeed() {
    HeroSearchCriteria criteria = HeroSearchCriteria.builder().minSpeed(50).build();
    assertThat(criteria.hasPowerStatsFilter()).isTrue();
  }

  @Test
  void shouldHavePowerStatsFilterWithMaxSpeed() {
    HeroSearchCriteria criteria = HeroSearchCriteria.builder().maxSpeed(100).build();
    assertThat(criteria.hasPowerStatsFilter()).isTrue();
  }

  @Test
  void shouldHavePowerStatsFilterWithMinIntelligence() {
    HeroSearchCriteria criteria = HeroSearchCriteria.builder().minIntelligence(50).build();
    assertThat(criteria.hasPowerStatsFilter()).isTrue();
  }

  @Test
  void shouldHavePowerStatsFilterWithMaxIntelligence() {
    HeroSearchCriteria criteria = HeroSearchCriteria.builder().maxIntelligence(100).build();
    assertThat(criteria.hasPowerStatsFilter()).isTrue();
  }

  @Test
  void shouldHavePowerStatsFilterWithMinDurability() {
    HeroSearchCriteria criteria = HeroSearchCriteria.builder().minDurability(50).build();
    assertThat(criteria.hasPowerStatsFilter()).isTrue();
  }

  @Test
  void shouldHavePowerStatsFilterWithMaxDurability() {
    HeroSearchCriteria criteria = HeroSearchCriteria.builder().maxDurability(100).build();
    assertThat(criteria.hasPowerStatsFilter()).isTrue();
  }

  @Test
  void shouldHavePowerStatsFilterWithMinCombat() {
    HeroSearchCriteria criteria = HeroSearchCriteria.builder().minCombat(50).build();
    assertThat(criteria.hasPowerStatsFilter()).isTrue();
  }

  @Test
  void shouldHavePowerStatsFilterWithMaxCombat() {
    HeroSearchCriteria criteria = HeroSearchCriteria.builder().maxCombat(100).build();
    assertThat(criteria.hasPowerStatsFilter()).isTrue();
  }

  @Test
  void shouldNotHavePowerStatsFilter() {
    HeroSearchCriteria criteria = HeroSearchCriteria.builder().build();
    assertThat(criteria.hasPowerStatsFilter()).isFalse();
  }

  @Test
  void shouldHandleNullSortBy() {
    HeroSearchCriteria criteria = HeroSearchCriteria.builder().sortBy(null).build();
    assertThat(criteria.sortBy()).isEqualTo("id");
  }

  @Test
  void shouldHandleBlankSortBy() {
    HeroSearchCriteria criteria = HeroSearchCriteria.builder().sortBy("   ").build();
    assertThat(criteria.sortBy()).isEqualTo("id");
  }

  @Test
  void shouldHandleNullSortDirection() {
    HeroSearchCriteria criteria = HeroSearchCriteria.builder().sortDirection(null).build();
    assertThat(criteria.sortDirection()).isEqualTo(HeroSearchCriteria.SortDirection.ASC);
  }
}
