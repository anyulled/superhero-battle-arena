package com.superherobattle.superherobattlearena.domain.json;

public record MatchResult(
        String winner,
        int durationSeconds,
        int totalDamageDealt) {
}
