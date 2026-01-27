package org.barcelonajug.superherobattlearena.application.usecase;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.UUID;

import org.barcelonajug.superherobattlearena.application.port.out.HeroUsageRepositoryPort;
import org.barcelonajug.superherobattlearena.domain.Hero;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FatigueServiceTest {

    private HeroUsageRepositoryPort heroUsageRepository;
    private FatigueService fatigueService;

    @BeforeEach
    void setUp() {
        heroUsageRepository = mock(HeroUsageRepositoryPort.class);
        fatigueService = new FatigueService(heroUsageRepository);
    }

    @Test
    void shouldReturnOriginalHeroIfNoStreak() {
        UUID teamId = UUID.randomUUID();
        Hero hero = new Hero(1, "Hero", new Hero.PowerStats(100, 10, 10, 10), "Fighter", 10, Collections.emptyList(),
                new Hero.Images(null, null, null, null));

        when(heroUsageRepository.findByTeamId(any())).thenReturn(Collections.emptyList());

        Hero result = fatigueService.applyFatigue(teamId, hero, 1);

        org.assertj.core.api.Assertions.assertThat(result.powerstats().durability()).isEqualTo(100);
        org.assertj.core.api.Assertions.assertThat(result.powerstats().strength()).isEqualTo(10);
    }
}
