package org.barcelonajug.superherobattlearena.domain.json;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

/** Represents a specific event during a match in JSON format. */
@Schema(description = "An event that occurred during a match simulation")
public record MatchEvent(
    @Schema(description = "Type of the event", example = "HIT") String type,
    @Schema(description = "Timestamp when the event occurred", example = "1706784000000")
        long timestamp,
    @Schema(
            description = "Human-readable description of the event",
            example = "Superman hits Batman for 25 damage")
        String description,
    @Schema(description = "ID of the actor (e.g., attacker)", example = "1")
        @org.jspecify.annotations.Nullable
        String actorId,
    @Schema(description = "ID of the target", example = "2") @org.jspecify.annotations.Nullable
        String targetId,
    @Schema(description = "Numeric value associated with the event (e.g., damage)", example = "25")
        int value) {

  public static MatchEvent matchStart(long timestamp) {
    return new MatchEvent("MATCH_START", timestamp, "Match started", null, null, 0);
  }

  public static MatchEvent matchEnd(UUID winnerId, long timestamp) {
    return new MatchEvent("MATCH_END", timestamp, "Winner: " + winnerId, null, null, 0);
  }

  public static MatchEvent draw(long timestamp) {
    return new MatchEvent("MATCH_END", timestamp, "Draw - Max turns reached", null, null, 0);
  }

  public static MatchEvent turnStart(int turnNumber, long timestamp) {
    return new MatchEvent(
        "TURN_START", timestamp, "Turn " + turnNumber + " started", null, null, turnNumber);
  }

  public static MatchEvent hit(
      String attackerName,
      String targetName,
      String actorId,
      String targetId,
      int damage,
      long timestamp) {
    return new MatchEvent(
        "HIT",
        timestamp,
        attackerName + " hits " + targetName + " for " + damage,
        actorId,
        targetId,
        damage);
  }

  public static MatchEvent ko(String targetName, String actorId, String targetId, long timestamp) {
    return new MatchEvent("KO", timestamp, targetName + " is KO!", actorId, targetId, 0);
  }
}
