package org.barcelonajug.superherobattlearena.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Schema(description = "Result of a batch match simulation")
public record BatchSimulationResult(
    @Schema(description = "List of IDs for the matches simulated") List<UUID> matchIds,
    @Schema(description = "Map of match IDs to winner team IDs") Map<UUID, UUID> winners,
    @Schema(description = "Total number of matches in the batch", example = "10") int totalMatches,
    @Schema(description = "Number of successfully simulated matches", example = "10")
        int successfulSimulations) {}
