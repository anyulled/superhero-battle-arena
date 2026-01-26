package org.barcelonajug.superherobattlearena.service;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import org.barcelonajug.superherobattlearena.domain.HeroUsage;
import org.barcelonajug.superherobattlearena.repository.HeroUsageRepository;
import org.springframework.stereotype.Service;

@Service
public class FatigueService {

    private static final BigDecimal PER_STREAK_PENALTY = new BigDecimal("0.10"); // 10% penalty per streak
    private static final BigDecimal MAX_PENALTY = new BigDecimal("0.50"); // Max 50% penalty
    private static final BigDecimal BASE_MULTIPLIER = BigDecimal.ONE;

    private final HeroUsageRepository heroUsageRepository;

    public FatigueService(HeroUsageRepository heroUsageRepository) {
        this.heroUsageRepository = heroUsageRepository;
    }

    public BigDecimal calculateFatigueMultiplier(UUID teamId, int heroId, int currentRoundNo) {
        // Look at the usage in the previous round to check for streak
        int previousRoundNo = currentRoundNo - 1;
        if (previousRoundNo < 1) {
            return BASE_MULTIPLIER;
        }

        Optional<HeroUsage> lastUsageOpt = heroUsageRepository.findByTeamIdAndHeroIdAndRoundNo(teamId, heroId,
                previousRoundNo);

        int currentStreak = lastUsageOpt.map(HeroUsage::getStreak).orElse(0);
        // If they played last round, the streak continues (we add 1 to calculate
        // current penalty impact if they play again)
        // Actually, the requirement says "check previous rounds... to determine the
        // current streak".
        // Usually streak implies consecutive usage up to now.
        // If they are used NOW, the streak increases.
        // The multiplier usually applies to the CURRENT match.
        // Assuming if I used them 3 times in a row, and I use them again (4th time),
        // the penalty is based on the streak.

        // Let's assume the stored streak in previous usage includes that usage.
        // So if I used them in R1, streak=1. In R2, if I use them, I see R1 usage has
        // streak=1.
        // So for R2, effective streak for calculation is starting at 1 (consecutive)?
        // Or does the penalty apply after some threshold?
        // "Formula: multiplier = 1 - min(perStreakPenalty * streak, maxPenalty)"

        // Let's assume if I use them now, the streak becomes last_streak + 1.
        // But the penalty might be based on how tired they ARE coming INTO this match.
        // So I'll base it on the streak count from the *previous* round.

        if (lastUsageOpt.isEmpty()) {
            return BASE_MULTIPLIER;
        }

        int streakIncoming = currentStreak; // This implies they played 'streak' times consecutively ending at previous
                                            // round.

        BigDecimal penalty = PER_STREAK_PENALTY.multiply(BigDecimal.valueOf(streakIncoming));
        if (penalty.compareTo(MAX_PENALTY) > 0) {
            penalty = MAX_PENALTY;
        }

        return BASE_MULTIPLIER.subtract(penalty);
    }
}
