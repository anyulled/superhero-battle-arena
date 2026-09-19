package org.barcelonajug.superherobattlearena.application.usecase;

import static java.util.Collections.emptyList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
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
  void redundantCalls() {
    UUID teamId = UUID.randomUUID();
    int teamSize = 5;
    List<Hero> heroes =
        IntStream.range(0, teamSize)
            .mapToObj(
                i ->
                    new Hero(
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
                        emptyList(),
                        new Hero.Images(null, null, null, null)))
            .toList();

    when(heroUsageRepository.findByTeamIdAndRoundNo(any(), anyInt())).thenReturn(emptyList());

    // Simulate usage in MatchController
    for (Hero hero : heroes) {
      fatigueService.applyFatigue(teamId, hero, 1);
    }

    // Verify repository was called N times
    verify(heroUsageRepository, times(teamSize)).findByTeamIdAndRoundNo(any(), anyInt());
  }

  @Test
  void optimizedCall() {
    UUID teamId = UUID.randomUUID();
    int teamSize = 5;
    List<Hero> heroes =
        IntStream.range(0, teamSize)
            .mapToObj(
                i ->
                    new Hero(
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
                        emptyList(),
                        new Hero.Images(null, null, null, null)))
            .toList();

    when(heroUsageRepository.findByTeamIdAndRoundNo(any(), anyInt())).thenReturn(emptyList());

    // Use the batch method
    fatigueService.applyFatigue(teamId, heroes, 1);

    // Verify repository was called 1 time
    verify(heroUsageRepository).findByTeamIdAndRoundNo(any(), anyInt());
  }

  @Test
  void recordUsageIndividualCallsNPlusOne() {
    int teamCount = 20;
    int roundNo = 1;
    List<UUID> teams = IntStream.range(0, teamCount).mapToObj(i -> UUID.randomUUID()).toList();
    List<Integer> heroIds = List.of(1, 2, 3, 4, 5);

    when(heroUsageRepository.findByTeamIdAndRoundNo(any(), anyInt())).thenReturn(emptyList());

    long startTime = System.nanoTime();
    for (UUID teamId : teams) {
      fatigueService.recordUsage(teamId, roundNo, heroIds);
    }
    long duration = System.nanoTime() - startTime;

    // Verify N DB queries for N teams
    verify(heroUsageRepository, times(teamCount)).findByTeamIdAndRoundNo(any(), anyInt());
    System.out.println(
        "N+1 recordUsage duration for "
            + teamCount
            + " teams: "
            + (duration / 1_000_000.0)
            + " ms");
  }

  @Test
  void recordUsageBatchCalls() {
    int teamCount = 20;
    int roundNo = 1;
    List<UUID> teams = IntStream.range(0, teamCount).mapToObj(i -> UUID.randomUUID()).toList();
    List<Integer> heroIds = List.of(1, 2, 3, 4, 5);

    Map<UUID, List<Integer>> teamHeroUsageMap =
        teams.stream().collect(Collectors.toMap(t -> t, t -> heroIds));

    when(heroUsageRepository.findByTeamIdInAndRoundNo(any(), anyInt())).thenReturn(emptyList());

    long startTime = System.nanoTime();
    fatigueService.recordUsage(teamHeroUsageMap, roundNo);
    long duration = System.nanoTime() - startTime;

    // Verify 1 DB query for all N teams
    verify(heroUsageRepository, times(1)).findByTeamIdInAndRoundNo(any(), anyInt());
    System.out.println(
        "Batched recordUsage duration for "
            + teamCount
            + " teams: "
            + (duration / 1_000_000.0)
            + " ms");
  }
}
