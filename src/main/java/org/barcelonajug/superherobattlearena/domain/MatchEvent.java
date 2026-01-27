package org.barcelonajug.superherobattlearena.domain;

import java.util.UUID;

/**
 * Represents an event occurring during a match.
 */
public record MatchEvent(
                UUID matchId,
                Integer seq,
                org.barcelonajug.superherobattlearena.domain.json.MatchEvent eventJson) {
}
