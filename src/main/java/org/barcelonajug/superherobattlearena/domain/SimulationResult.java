package org.barcelonajug.superherobattlearena.domain;

import org.barcelonajug.superherobattlearena.domain.json.MatchEvent;

import java.util.List;
import java.util.UUID;

/**
 * Represents the result of a match simulation.
 */
public record SimulationResult(
                UUID winnerTeamId,
                int totalTurns,
                List<MatchEvent> events) {
}
