package org.barcelonajug.superherobattlearena.domain;

import java.util.UUID;

import org.barcelonajug.superherobattlearena.domain.json.MatchEventSnapshot;

/** Represents an event occurring during a match. */
public record MatchEvent(UUID matchId, MatchEventSnapshot eventJson) {
}
