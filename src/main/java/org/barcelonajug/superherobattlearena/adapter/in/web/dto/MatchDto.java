package org.barcelonajug.superherobattlearena.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import org.barcelonajug.superherobattlearena.domain.Match;
import org.barcelonajug.superherobattlearena.domain.MatchStatus;
import org.barcelonajug.superherobattlearena.domain.json.MatchResult;
import org.jspecify.annotations.Nullable;

@Schema(name = "Match", description = "Details of a battle match between two teams")
public record MatchDto(
    @Schema(
            description = "Unique ID of the match",
            example = "550e8400-e29b-41d4-a716-446655440000")
        UUID matchId,
    @Schema(
            description = "ID of the tournament session",
            example = "550e8400-e29b-41d4-a716-446655440001")
        @Nullable UUID sessionId,
    @Schema(description = "Number of the round", example = "1") Integer roundNo,
    @Schema(description = "ID of the first team", example = "550e8400-e29b-41d4-a716-446655440002")
        UUID teamA,
    @Schema(description = "ID of the second team", example = "550e8400-e29b-41d4-a716-446655440003")
        UUID teamB,
    @Schema(
            description = "ID of the winning team",
            example = "550e8400-e29b-41d4-a716-446655440002")
        @Nullable UUID winnerTeam,
    @Schema(description = "Current status of the match", example = "COMPLETED") MatchStatus status,
    @Schema(description = "Detailed JSON result of the match simulation")
        @Nullable MatchResultDto resultJson) {
  public static MatchDto from(Match value) {
    return new MatchDto(
        value.getMatchId(),
        value.getSessionId(),
        value.getRoundNo(),
        value.getTeamA(),
        value.getTeamB(),
        value.getWinnerTeam(),
        value.getStatus(),
        matchResultFrom(value.getResultJson()));
  }

  private static @Nullable MatchResultDto matchResultFrom(@Nullable MatchResult value) {
    if (value == null) {
      return null;
    }
    return MatchResultDto.from(value);
  }
}
