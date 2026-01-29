package org.barcelonajug.superherobattlearena.domain;

import java.math.BigDecimal;
import java.util.UUID;

/** Represents the usage of heroes by a team in a round. */
public record HeroUsage(
    UUID teamId, Integer heroId, Integer roundNo, Integer streak, BigDecimal multiplier) {}
