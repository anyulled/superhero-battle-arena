package org.barcelonajug.superherobattlearena.application.usecase;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.barcelonajug.superherobattlearena.application.port.out.HeroUsageRepositoryPort;
import org.barcelonajug.superherobattlearena.domain.Hero;
import org.barcelonajug.superherobattlearena.domain.HeroUsage;

// No @Service annotation
public class FatigueService {

    private final HeroUsageRepositoryPort heroUsageRepository;

    public FatigueService(HeroUsageRepositoryPort heroUsageRepository) {
        this.heroUsageRepository = heroUsageRepository;
    }

    public Hero applyFatigue(UUID teamId, Hero hero) {
        List<HeroUsage> usageHistory = heroUsageRepository.findByTeamId(teamId);

        // Simple logic: if used in previous round, stats are reduced.
        // This is a placeholder for the actual complex logic which involves streaks.

        // Find current streak
        int currentStreak = calculateStreak(usageHistory, hero.id());

        if (currentStreak == 0) {
            return hero;
        }

        BigDecimal multiplier = calculateMultiplier(currentStreak);

        return new Hero(
                hero.id(),
                hero.name(),
                new Hero.PowerStats(
                        (int) (hero.powerstats().hp() * multiplier.doubleValue()),
                        (int) (hero.powerstats().atk() * multiplier.doubleValue()),
                        (int) (hero.powerstats().def() * multiplier.doubleValue()),
                        (int) (hero.powerstats().spd() * multiplier.doubleValue())),
                hero.role(),
                hero.cost(),
                hero.tags(),
                hero.images());
    }

    private int calculateStreak(List<HeroUsage> history, int heroId) {
        // Logic to calculate consecutive uses from history
        // This is simplified for the refactoring step
        return 0;
    }

    private BigDecimal calculateMultiplier(int streak) {
        // Logic for multiplier
        return BigDecimal.ONE;
    }

    public void recordUsage(UUID teamId, int roundNo, List<Integer> heroIds) {
        // Record usage for next rounds
    }
}
