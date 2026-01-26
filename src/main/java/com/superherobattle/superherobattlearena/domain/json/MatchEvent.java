package com.superherobattle.superherobattlearena.domain.json;

public record MatchEvent(
        String type,
        long timestamp,
        String description,
        Integer actorId,
        Integer targetId,
        int value) {
}
