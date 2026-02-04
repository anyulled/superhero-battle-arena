package org.barcelonajug.superherobattlearena.domain.json;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/** Represents a draft submission of heroes for a round. */
@Schema(description = "Draft submission details including selected heroes and strategy")
public record DraftSubmission(
    @Schema(
            description = "List of IDs of the selected heroes",
            example = "[101, 102, 103]",
            requiredMode = Schema.RequiredMode.REQUIRED)
        List<Integer> heroIds,
    @Schema(
            description = "Strategy description for the battle",
            example = "Focus on tanking and healing",
            requiredMode = Schema.RequiredMode.REQUIRED)
        String strategy) {}
