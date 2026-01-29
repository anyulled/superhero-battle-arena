package org.barcelonajug.superherobattlearena.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.UUID;

import org.barcelonajug.superherobattlearena.application.port.out.HeroUsageRepositoryPort;
import org.barcelonajug.superherobattlearena.domain.Hero;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FatigueUseCaseTest {

  private HeroUsageRepositoryPort heroUsageRepository;
  private FatigueUseCase fatigueUseCase;

  @BeforeEach
  void setUp() {
    heroUsageRepository = mock(HeroUsageRepositoryPort.class);
    fatigueUseCase = new FatigueUseCase(heroUsageRepository);
  }

  @Test
  void shouldReturnOriginalHeroIfNoStreak() {
    UUID teamId = UUID.randomUUID();
    Hero hero = Hero.builder()
        .id(1)
        .name("Hero")
        .slug("hero")
        .powerstats(
            Hero.PowerStats.builder()
                .durability(100)
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

    when(heroUsageRepository.findByTeamIdAndRoundNo(any(), anyInt()))
        .thenReturn(Collections.emptyList());

    Hero result = fatigueUseCase.applyFatigue(teamId, hero, 1);

    assertThat(result.powerstats().durability()).isEqualTo(100);
    assertThat(result.powerstats().strength()).isEqualTo(10);
  }
}
