package org.barcelonajug.superherobattlearena.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.barcelonajug.superherobattlearena.domain.json.MatchEventSnapshot;
import org.barcelonajug.superherobattlearena.domain.json.MatchEventSnapshot.Type;
import org.jspecify.annotations.Nullable;

@Schema(
    name = "MatchEventSnapshot",
    description = "An event that occurred during a match simulation")
public record MatchEventSnapshotDto(
    @Schema(description = "Type of the event", example = "HIT") Type type,
    @Schema(description = "Timestamp when the event occurred", example = "1706784000000")
        long timestamp,
    @Schema(
            description = "Human-readable description of the event",
            example = "Superman hits Batman for 25 damage")
        String description,
    @Schema(description = "ID of the actor (e.g., attacker)", example = "1")
        @Nullable String actorId,
    @Schema(description = "ID of the target", example = "2") @Nullable String targetId,
    @Schema(description = "Numeric value associated with the event (e.g., damage)", example = "25")
        int value) {
  public static MatchEventSnapshotDto from(MatchEventSnapshot value) {
    return new MatchEventSnapshotDto(
        value.type(),
        value.timestamp(),
        value.description(),
        value.actorId(),
        value.targetId(),
        value.value());
  }
}
