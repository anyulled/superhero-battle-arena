package org.barcelonajug.superherobattlearena.domain.json;

/**
 * Represents the summary result of a match.
 */
public record MatchResult(
                String winner,
                int durationSeconds,
                int totalDamageDealt) {
}
