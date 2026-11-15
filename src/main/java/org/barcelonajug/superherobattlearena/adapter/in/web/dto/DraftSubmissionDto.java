package org.barcelonajug.superherobattlearena.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Objects;
import org.barcelonajug.superherobattlearena.domain.json.DraftSubmission;

@Schema(
    name = "DraftSubmission",
    description = "Draft submission details including selected heroes and strategy")
public record DraftSubmissionDto(
    @Schema(
            description = "List of IDs of the selected heroes",
            example = "[101, 102, 103]",
            requiredMode = Schema.RequiredMode.REQUIRED)
        List<Integer> heroIds,
    @Schema(
            description = "Strategy description for the battle",
            example = "Focus on tanking and healing",
            requiredMode = Schema.RequiredMode.REQUIRED)
        String strategy) {
  public static DraftSubmissionDto from(DraftSubmission value) {
    return new DraftSubmissionDto(value.heroIds(), value.strategy());
  }

  public DraftSubmission toDomain() {
    if (heroIds == null || heroIds.stream().anyMatch(Objects::isNull)) {
      throw new IllegalArgumentException("heroIds must not be null or contain null elements");
    }
    if (strategy == null) {
      throw new IllegalArgumentException("strategy must not be null");
    }
    return new DraftSubmission(heroIds, strategy);
  }
}
