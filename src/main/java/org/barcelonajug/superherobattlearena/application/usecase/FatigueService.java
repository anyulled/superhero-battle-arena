package org.barcelonajug.superherobattlearena.application.usecase;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.barcelonajug.superherobattlearena.application.port.out.HeroUsageRepositoryPort;
import org.barcelonajug.superherobattlearena.domain.Hero;
import org.barcelonajug.superherobattlearena.domain.HeroUsage;
import org.springframework.stereotype.Service;

@Service
public class FatigueService {

  private final HeroUsageRepositoryPort heroUsageRepository;

  public FatigueService(HeroUsageRepositoryPort heroUsageRepository) {
    this.heroUsageRepository = heroUsageRepository;
  }

  public Hero applyFatigue(UUID teamId, Hero hero, int currentRoundNo) {
    List<HeroUsage> usageHistory = heroUsageRepository.findByTeamId(teamId);

    // Find current streak by looking at the previous round's usage
    int currentStreak = calculateStreak(usageHistory, hero.id(), currentRoundNo);

    if (currentStreak == 0) {
      return hero;
    }

    BigDecimal multiplier = calculateMultiplier(currentStreak);

    return new Hero(
        hero.id(),
        hero.name(),
        hero.slug(),
        new Hero.PowerStats(
            (int) (hero.powerstats().durability() * multiplier.doubleValue()),
            (int) (hero.powerstats().strength() * multiplier.doubleValue()),
            (int) (hero.powerstats().power() * multiplier.doubleValue()),
            (int) (hero.powerstats().speed() * multiplier.doubleValue()),
            (int) (hero.powerstats().intelligence() * multiplier.doubleValue()),
            (int) (hero.powerstats().combat() * multiplier.doubleValue())),
        hero.role(),
        hero.cost(),
        hero.alignment(),
        hero.publisher(),
        hero.appearance(),
        hero.biography(),
        hero.tags(),
        hero.images());
  }

  private int calculateStreak(List<HeroUsage> history, int heroId, int currentRoundNo) {
    return history.stream()
        .filter(u -> u.heroId().equals(heroId) && u.roundNo().equals(currentRoundNo - 1))
        .map(HeroUsage::streak)
        .findFirst()
        .orElse(0);
  }

  private BigDecimal calculateMultiplier(int streak) {
    // Stats decrease by 5% for each round in a streak, capped at 30% reduction (0.7
    // multiplier)
    double reduction = Math.min(0.3, streak * 0.05);
    return BigDecimal.valueOf(1.0 - reduction);
  }

  public void recordUsage(UUID teamId, int roundNo, List<Integer> heroIds) {
    List<HeroUsage> previousRoundHistory =
        heroUsageRepository.findByTeamIdAndRoundNo(teamId, roundNo - 1);

    Map<Integer, Integer> heroIdToStreakMap =
        previousRoundHistory.stream()
            .collect(Collectors.toMap(HeroUsage::heroId, HeroUsage::streak));

    List<HeroUsage> usages =
        heroIds.stream()
            .map(
                heroId -> {
                  int previousStreak = heroIdToStreakMap.getOrDefault(heroId, 0);
                  int newStreak = previousStreak + 1;
                  BigDecimal multiplier = calculateMultiplier(newStreak);
                  return new HeroUsage(teamId, heroId, roundNo, newStreak, multiplier);
                })
            .toList();

    heroUsageRepository.saveAll(usages);
  }
}
