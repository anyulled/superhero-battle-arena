package org.barcelonajug.superherobattlearena.domain.json;

public record MatchEvent(
                String type,
                long timestamp,
                String description,
                String actorId,
                String targetId,
                int value) {
}
