package org.barcelonajug.superherobattlearena.application.usecase;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.barcelonajug.superherobattlearena.application.port.out.HeroUsageRepositoryPort;
import org.barcelonajug.superherobattlearena.domain.Hero;
import org.barcelonajug.superherobattlearena.domain.HeroUsage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

@Service
public class FatigueUseCase {

  private static final Logger log = LoggerFactory.getLogger(FatigueUseCase.class);

  private final HeroUsageRepositoryPort heroUsageRepository;

  public FatigueUseCase(HeroUsageRepositoryPort heroUsageRepository) {
    this.heroUsageRepository = heroUsageRepository;
  }

  public List<Hero> applyFatigue(UUID teamId, List<Hero> heroes, int currentRoundNo) {
    List<HeroUsage> previousRoundUsage =
        heroUsageRepository.findByTeamIdAndRoundNo(teamId, currentRoundNo - 1);
    return heroes.stream()
        .map(hero -> applyFatigueWithHistory(hero, previousRoundUsage, currentRoundNo))
        .toList();
  }

  public Hero applyFatigue(UUID teamId, Hero hero, int currentRoundNo) {
    log.debug(
        "Applying fatigue to single hero - teamId={}, heroId={}, roundNo={}",
        teamId,
        hero.id(),
        currentRoundNo);
    return applyFatigue(teamId, List.of(hero), currentRoundNo).getFirst();
  }

  private Hero applyFatigueWithHistory(
      Hero hero, List<HeroUsage> usageHistory, int currentRoundNo) {
    // Find current streak by looking at the previous round's usage
    int currentStreak = calculateStreak(usageHistory, hero.id(), currentRoundNo);

    if (currentStreak == 0) {
      log.debug("No fatigue for hero {} - no previous usage", hero.id());
      return hero;
    }

    BigDecimal multiplier = calculateMultiplier(currentStreak);
    log.debug(
        "Applying fatigue to hero {} - streak={}, multiplier={}",
        hero.id(),
        currentStreak,
        multiplier);

    return Hero.builder()
        .id(hero.id())
        .name(hero.name())
        .slug(hero.slug())
        .powerstats(
            Hero.PowerStats.builder()
                .durability((int) (hero.powerstats().durability() * multiplier.doubleValue()))
                .strength((int) (hero.powerstats().strength() * multiplier.doubleValue()))
                .power((int) (hero.powerstats().power() * multiplier.doubleValue()))
                .speed((int) (hero.powerstats().speed() * multiplier.doubleValue()))
                .intelligence((int) (hero.powerstats().intelligence() * multiplier.doubleValue()))
                .combat((int) (hero.powerstats().combat() * multiplier.doubleValue()))
                .build())
        .role(hero.role())
        .cost(hero.cost())
        .alignment(hero.alignment())
        .publisher(hero.publisher())
        .appearance(hero.appearance())
        .biography(hero.biography())
        .tags(hero.tags())
        .images(hero.images())
        .build();
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
    MDC.put("teamId", teamId.toString());
    MDC.put("roundNo", String.valueOf(roundNo));

    try {
      log.debug(
          "Recording hero usage - teamId={}, roundNo={}, heroes={}",
          teamId,
          roundNo,
          heroIds.size());

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
                    log.debug(
                        "Hero {} usage - previousStreak={}, newStreak={}, multiplier={}",
                        heroId,
                        previousStreak,
                        newStreak,
                        multiplier);
                    return new HeroUsage(teamId, heroId, roundNo, newStreak, multiplier);
                  })
              .toList();

      heroUsageRepository.saveAll(usages);
      log.info(
          "Recorded usage for {} heroes - teamId={}, roundNo={}", usages.size(), teamId, roundNo);
    } finally {
      MDC.remove("teamId");
      MDC.remove("roundNo");
    }
  }
}
