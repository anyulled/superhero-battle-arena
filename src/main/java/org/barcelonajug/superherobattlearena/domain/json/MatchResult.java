package org.barcelonajug.superherobattlearena.domain.json;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Summary result of a match simulation")
public record MatchResult(
    @Schema(description = "Name of the winning team", example = "Team A") String winner,
    @Schema(description = "Duration of the match in seconds", example = "120") int durationSeconds,
    @Schema(description = "Total damage dealt during the match", example = "500")
        int totalDamageDealt) {}
