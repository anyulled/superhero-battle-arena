package org.barcelonajug.superherobattlearena.domain;

import java.math.BigDecimal;
import java.util.UUID;

public record HeroUsage(
        UUID teamId,
        Integer heroId,
        Integer roundNo,
        Integer streak,
        BigDecimal multiplier) {
}
