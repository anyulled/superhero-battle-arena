package org.barcelonajug.superherobattlearena.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.barcelonajug.superherobattlearena.application.port.out.HeroUsageRepositoryPort;
import org.barcelonajug.superherobattlearena.domain.Hero;
import org.barcelonajug.superherobattlearena.domain.HeroUsage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class FatigueUseCaseTest {

  private HeroUsageRepositoryPort heroUsageRepository;
  private FatigueUseCase fatigueUseCase;

  @BeforeEach
  void setUp() {
    heroUsageRepository = mock(HeroUsageRepositoryPort.class);
    fatigueUseCase = new FatigueUseCase(heroUsageRepository);
  }

  @Test
  void shouldApplyFatigueIfStreakExists() {
    UUID teamId = UUID.randomUUID();
    Hero hero = createHero(1, 100);
    int streak = 2; // 10% reduction (0.9 multiplier)

    HeroUsage usage = new HeroUsage(teamId, 1, 0, streak, BigDecimal.valueOf(0.9));

    when(heroUsageRepository.findByTeamIdAndRoundNo(teamId, 0)).thenReturn(List.of(usage));

    Hero result = fatigueUseCase.applyFatigue(teamId, hero, 1);

    assertThat(result.powerstats().durability()).isEqualTo(90);
    assertThat(result.powerstats().strength()).isEqualTo(9);
  }

  @Test
  void shouldCapFatigueAtThirtyPercent() {
    UUID teamId = UUID.randomUUID();
    Hero hero = createHero(1, 100);
    int streak = 10; // 50% theoretical reduction, but capped at 30% (0.7 multiplier)

    HeroUsage usage = new HeroUsage(teamId, 1, 0, streak, BigDecimal.valueOf(0.7));

    when(heroUsageRepository.findByTeamIdAndRoundNo(teamId, 0)).thenReturn(List.of(usage));

    Hero result = fatigueUseCase.applyFatigue(teamId, hero, 1);

    assertThat(result.powerstats().durability()).isEqualTo(70);
  }

  @Test
  void shouldRecordUsageCorrectlyForNewHero() {
    UUID teamId = UUID.randomUUID();
    when(heroUsageRepository.findByTeamIdAndRoundNo(any(), anyInt()))
        .thenReturn(Collections.emptyList());

    fatigueUseCase.recordUsage(teamId, 1, List.of(1, 2));

    @SuppressWarnings("unchecked")
    ArgumentCaptor<List<HeroUsage>> captor = ArgumentCaptor.forClass(List.class);
    verify(heroUsageRepository).saveAll(captor.capture());

    List<HeroUsage> saved = captor.getValue();
    assertThat(saved).hasSize(2);
    assertThat(saved.get(0).streak()).isEqualTo(1);
    assertThat(saved.get(0).multiplier()).isEqualByComparingTo("0.95");
  }

  @Test
  void shouldRecordUsageCorrectlyForHeroWithHistory() {
    UUID teamId = UUID.randomUUID();
    HeroUsage previousUsage = new HeroUsage(teamId, 1, 1, 1, BigDecimal.valueOf(0.95));

    when(heroUsageRepository.findByTeamIdAndRoundNo(teamId, 1)).thenReturn(List.of(previousUsage));

    fatigueUseCase.recordUsage(teamId, 2, List.of(1));

    @SuppressWarnings("unchecked")
    ArgumentCaptor<List<HeroUsage>> captor = ArgumentCaptor.forClass(List.class);
    verify(heroUsageRepository).saveAll(captor.capture());

    List<HeroUsage> saved = captor.getValue();
    assertThat(saved).hasSize(1);
    assertThat(saved.get(0).streak()).isEqualTo(2);
    assertThat(saved.get(0).multiplier()).isEqualByComparingTo("0.90");
  }

  @Test
  void shouldApplyFatigueToMultipleHeroes() {
    UUID teamId = UUID.randomUUID();
    Hero h1 = createHero(1, 100);
    Hero h2 = createHero(2, 200);

    HeroUsage usage1 = new HeroUsage(teamId, 1, 0, 1, BigDecimal.valueOf(0.95));

    when(heroUsageRepository.findByTeamIdAndRoundNo(teamId, 0)).thenReturn(List.of(usage1));

    List<Hero> results = fatigueUseCase.applyFatigue(teamId, List.of(h1, h2), 1);

    assertThat(results).hasSize(2);
    assertThat(results.get(0).powerstats().durability()).isEqualTo(95);
    assertThat(results.get(1).powerstats().durability()).isEqualTo(200); // No streak for h2
  }

  private Hero createHero(int id, int durability) {
    return Hero.builder()
        .id(id)
        .name("Hero " + id)
        .slug("hero-" + id)
        .powerstats(
            Hero.PowerStats.builder()
                .durability(durability)
                .strength(10)
                .power(10)
                .speed(10)
                .intelligence(10)
                .combat(10)
                .build())
        .role("Fighter")
        .alignment("good")
        .publisher("Marvel")
        .build();
  }
}
