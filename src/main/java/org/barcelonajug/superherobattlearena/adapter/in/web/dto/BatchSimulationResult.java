package org.barcelonajug.superherobattlearena.adapter.in.web.dto;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public record BatchSimulationResult(
        List<UUID> matchIds,
        Map<UUID, UUID> winners,
        int totalMatches,
        int successfulSimulations) {
}
