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
    Hero hero =
        new Hero(
            1,
            "Hero",
            "hero",
            new Hero.PowerStats(100, 10, 10, 10, 10, 10),
            "Fighter",
            10,
            "good",
            "Marvel",
            null,
            null,
            Collections.emptyList(),
            new Hero.Images(null, null, null, null));

    when(heroUsageRepository.findByTeamIdAndRoundNo(any(), anyInt()))
        .thenReturn(Collections.emptyList());

    Hero result = fatigueUseCase.applyFatigue(teamId, hero, 1);

    assertThat(result.powerstats().durability()).isEqualTo(100);
    assertThat(result.powerstats().strength()).isEqualTo(10);
  }
}
