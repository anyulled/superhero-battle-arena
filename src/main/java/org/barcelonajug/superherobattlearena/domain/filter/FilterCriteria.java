package org.barcelonajug.superherobattlearena.domain.filter;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import org.jspecify.annotations.Nullable;

@Schema(description = "Criteria for filtering superheroes dynamically")
public record FilterCriteria(
    @Schema(
            description =
                "The field to filter on (e.g., 'powerStats.strength', 'appearance.gender', 'publisher')",
            example = "powerStats.strength",
            requiredMode = Schema.RequiredMode.REQUIRED)
        String field,
    @Schema(
            description = "The operation to perform",
            example = "GREATER_THAN",
            requiredMode = Schema.RequiredMode.REQUIRED)
        FilterOperator operator,
    @Schema(description = "The value to filter by", example = "50") @Nullable String value,
    @Schema(description = "Secondary value, only used for 'BETWEEN' operator", example = "100")
        @Nullable String value2,
    @Schema(description = "List of values, used for 'IN' operator")
        @Nullable List<String> values) {}
