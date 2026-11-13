package org.barcelonajug.superherobattlearena.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.barcelonajug.superherobattlearena.domain.json.MatchResult;

@Schema(name = "MatchResult", description = "Summary result of a match simulation")
public record MatchResultDto(
    @Schema(description = "Name of the winning team", example = "Team A") String winner,
    @Schema(description = "Duration of the match in seconds", example = "120") int durationSeconds,
    @Schema(description = "Total damage dealt during the match", example = "500")
        int totalDamageDealt) {
  public static MatchResultDto from(MatchResult value) {
    return new MatchResultDto(value.winner(), value.durationSeconds(), value.totalDamageDealt());
  }
}
