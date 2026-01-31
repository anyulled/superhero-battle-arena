package org.barcelonajug.superherobattlearena.application.usecase;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import org.barcelonajug.superherobattlearena.application.port.out.HeroUsageRepositoryPort;
import org.barcelonajug.superherobattlearena.domain.Hero;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FatigueUseCasePerformanceTest {

  private HeroUsageRepositoryPort heroUsageRepository;
  private FatigueUseCase fatigueService;

  @BeforeEach
  void setUp() {
    heroUsageRepository = mock(HeroUsageRepositoryPort.class);
    fatigueService = new FatigueUseCase(heroUsageRepository);
  }

  @Test
  void testRedundantCalls() {
    UUID teamId = UUID.randomUUID();
    int teamSize = 5;
    List<Hero> heroes = IntStream.range(0, teamSize)
        .mapToObj(
            i -> new Hero(
                i,
                "Hero" + i,
                "hero-" + i,
                new Hero.PowerStats(100, 10, 10, 10, 10, 10),
                "Fighter",
                10,
                "good",
                "Marvel",
                null,
                null,
                Collections.emptyList(),
                new Hero.Images(null, null, null, null)))
        .toList();

    when(heroUsageRepository.findByTeamIdAndRoundNo(any(), anyInt()))
        .thenReturn(Collections.emptyList());

    // Simulate usage in MatchController
    for (Hero hero : heroes) {
      fatigueService.applyFatigue(teamId, hero, 1);
    }

    // Verify repository was called N times
    verify(heroUsageRepository, times(teamSize)).findByTeamIdAndRoundNo(any(), anyInt());
  }

  @Test
  void testOptimizedCall() {
    UUID teamId = UUID.randomUUID();
    int teamSize = 5;
    List<Hero> heroes = IntStream.range(0, teamSize)
        .mapToObj(
            i -> new Hero(
                i,
                "Hero" + i,
                "hero-" + i,
                new Hero.PowerStats(100, 10, 10, 10, 10, 10),
                "Fighter",
                10,
                "good",
                "Marvel",
                null,
                null,
                Collections.emptyList(),
                new Hero.Images(null, null, null, null)))
        .toList();

    when(heroUsageRepository.findByTeamIdAndRoundNo(any(), anyInt()))
        .thenReturn(Collections.emptyList());

    // Use the batch method
    fatigueService.applyFatigue(teamId, heroes, 1);

    // Verify repository was called 1 time
    verify(heroUsageRepository, times(1)).findByTeamIdAndRoundNo(any(), anyInt());
  }
}
