package org.barcelonajug.superherobattlearena.domain.json;

import java.util.UUID;

import org.jspecify.annotations.Nullable;

import io.swagger.v3.oas.annotations.media.Schema;

/** Represents a specific event during a match in JSON format. */
@Schema(description = "An event that occurred during a match simulation")
public record MatchEventSnapshot(
    @Schema(description = "Type of the event", example = "HIT") Type type,
    @Schema(description = "Timestamp when the event occurred", example = "1706784000000") long timestamp,
    @Schema(description = "Human-readable description of the event", example = "Superman hits Batman for 25 damage") String description,
    @Schema(description = "ID of the actor (e.g., attacker)", example = "1") @Nullable String actorId,
    @Schema(description = "ID of the target", example = "2") @Nullable String targetId,
    @Schema(description = "Numeric value associated with the event (e.g., damage)", example = "25") int value) {

  public enum Type {
    MATCH_START,
    MATCH_END,
    TURN_START,
    HIT,
    KO,
    DODGE,
    CRITICAL_HIT,
    ROUND_START,
    ROUND_END,
    ATTACK_PERFORMED,
    HERO_KNOCKED_OUT,
    HEALTH_CHANGED
  }

  public static MatchEventSnapshot matchStart(long timestamp) {
    return new MatchEventSnapshot(Type.MATCH_START, timestamp, "Match started", null, null, 0);
  }

  public static MatchEventSnapshot matchEnd(UUID winnerId, long timestamp) {
    return new MatchEventSnapshot(Type.MATCH_END, timestamp, "Winner: " + winnerId, null, null, 0);
  }

  public static MatchEventSnapshot draw(long timestamp) {
    return new MatchEventSnapshot(Type.MATCH_END, timestamp, "Draw - Max turns reached", null, null, 0);
  }

  public static MatchEventSnapshot turnStart(int turnNumber, long timestamp) {
    return new MatchEventSnapshot(
        Type.TURN_START, timestamp, "Turn " + turnNumber + " started", null, null, turnNumber);
  }

  public static MatchEventSnapshot hit(
      String attackerName,
      String targetName,
      String actorId,
      String targetId,
      int damage,
      long timestamp) {
    return new MatchEventSnapshot(
        Type.HIT,
        timestamp,
        attackerName + " hits " + targetName + " for " + damage,
        actorId,
        targetId,
        damage);
  }

  public static MatchEventSnapshot ko(String targetName, String actorId, String targetId, long timestamp) {
    return new MatchEventSnapshot(Type.KO, timestamp, targetName + " is KO!", actorId, targetId, 0);
  }

  public static MatchEventSnapshot dodge(
      String attackerName, String targetName, String actorId, String targetId, long timestamp) {
    return new MatchEventSnapshot(
        Type.DODGE,
        timestamp,
        targetName + " dodged an attack from " + attackerName,
        actorId,
        targetId,
        0);
  }

  public static MatchEventSnapshot criticalHit(
      String attackerName,
      String targetName,
      String actorId,
      String targetId,
      int damage,
      long timestamp) {
    return new MatchEventSnapshot(
        Type.CRITICAL_HIT,
        timestamp,
        "Critical Hit! " + attackerName + " hits " + targetName + " for " + damage,
        actorId,
        targetId,
        damage);
  }
}
