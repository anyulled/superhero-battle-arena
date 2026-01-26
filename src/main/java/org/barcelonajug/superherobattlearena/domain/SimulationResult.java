package org.barcelonajug.superherobattlearena.domain;

import java.util.List;
import java.util.UUID;
import org.barcelonajug.superherobattlearena.domain.json.MatchEvent;

public record SimulationResult(
        UUID winnerTeamId,
        int totalTurns,
        List<MatchEvent> events) {
}
