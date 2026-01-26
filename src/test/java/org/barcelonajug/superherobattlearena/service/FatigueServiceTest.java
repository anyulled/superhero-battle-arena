package org.barcelonajug.superherobattlearena.service;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import org.barcelonajug.superherobattlearena.domain.HeroUsage;
import org.barcelonajug.superherobattlearena.repository.HeroUsageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

class FatigueServiceTest {

    private HeroUsageRepository heroUsageRepository;
    private FatigueService fatigueService;

    @BeforeEach
    void setUp() {
        heroUsageRepository = Mockito.mock(HeroUsageRepository.class);
        fatigueService = new FatigueService(heroUsageRepository);
    }

    @Test
    void shouldReturnBaseMultiplierForFirstRound() {
        BigDecimal multiplier = fatigueService.calculateFatigueMultiplier(UUID.randomUUID(), 1, 1);
        assertThat(multiplier).isEqualByComparingTo(BigDecimal.ONE);
    }

    @Test
    void shouldReturnBaseMultiplierIfNoPriorUsage() {
        when(heroUsageRepository.findByTeamIdAndHeroIdAndRoundNo(any(), anyInt(), anyInt()))
                .thenReturn(Optional.empty());

        BigDecimal multiplier = fatigueService.calculateFatigueMultiplier(UUID.randomUUID(), 1, 2);
        assertThat(multiplier).isEqualByComparingTo(BigDecimal.ONE);
    }

    @Test
    void shouldApplyPenaltyForStreak() {
        HeroUsage usage = new HeroUsage();
        usage.setStreak(1); // Played once last round
        when(heroUsageRepository.findByTeamIdAndHeroIdAndRoundNo(any(), anyInt(), anyInt()))
                .thenReturn(Optional.of(usage));

        BigDecimal multiplier = fatigueService.calculateFatigueMultiplier(UUID.randomUUID(), 1, 2);
        // Penalty = 0.1 * 1 = 0.1. Multiplier = 1.0 - 0.1 = 0.90
        assertThat(multiplier).isEqualByComparingTo(new BigDecimal("0.90"));
    }

    @Test
    void shouldCapPenalty() {
        HeroUsage usage = new HeroUsage();
        usage.setStreak(10); // Played 10 times!
        when(heroUsageRepository.findByTeamIdAndHeroIdAndRoundNo(any(), anyInt(), anyInt()))
                .thenReturn(Optional.of(usage));

        BigDecimal multiplier = fatigueService.calculateFatigueMultiplier(UUID.randomUUID(), 1, 11);
        // Penalty = min(0.1 * 10, 0.5) = 0.5. Multiplier = 1.0 - 0.5 = 0.50
        assertThat(multiplier).isEqualByComparingTo(new BigDecimal("0.50"));
    }
}
