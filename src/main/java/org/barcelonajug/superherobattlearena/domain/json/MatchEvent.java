package org.barcelonajug.superherobattlearena.domain.json;

/** Represents a specific event during a match in JSON format. */
public record MatchEvent(
    String type,
    long timestamp,
    String description,
    @org.jspecify.annotations.Nullable String actorId,
    @org.jspecify.annotations.Nullable String targetId,
    int value) {}
