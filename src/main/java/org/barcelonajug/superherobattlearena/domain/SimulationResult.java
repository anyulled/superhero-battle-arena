package org.barcelonajug.superherobattlearena.domain;

import java.util.List;
import java.util.UUID;
import org.barcelonajug.superherobattlearena.domain.json.MatchEvent;

/** Represents the result of a match simulation. */
public record SimulationResult(UUID winnerTeamId, int totalTurns, List<MatchEvent> events) {}
