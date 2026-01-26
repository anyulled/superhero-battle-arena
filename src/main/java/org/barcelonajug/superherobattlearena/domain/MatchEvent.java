package org.barcelonajug.superherobattlearena.domain;

import java.util.UUID;

public record MatchEvent(
        UUID matchId,
        Integer seq,
        org.barcelonajug.superherobattlearena.domain.json.MatchEvent eventJson) {
}
