package org.barcelonajug.superherobattlearena.domain;

import java.util.List;
import java.util.UUID;

import org.barcelonajug.superherobattlearena.domain.json.MatchEventSnapshot;
import org.jspecify.annotations.Nullable;

/** Represents the result of a match simulation. */
public record SimulationResult(
        @Nullable UUID winnerTeamId,
        int totalTurns,
        List<MatchEventSnapshot> events) {
}
