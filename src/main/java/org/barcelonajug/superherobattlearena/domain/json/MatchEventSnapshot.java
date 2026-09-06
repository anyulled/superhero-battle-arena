package org.barcelonajug.superherobattlearena.domain.json;

import java.util.UUID;
import org.jspecify.annotations.Nullable;

/** Represents a specific event during a match in JSON format. */
public record MatchEventSnapshot(
    Type type,
    long timestamp,
    String description,
    @Nullable String actorId,
    @Nullable String targetId,
    int value) {

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
    return new MatchEventSnapshot(
        Type.MATCH_END, timestamp, "Draw - Max turns reached", null, null, 0);
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

  public static MatchEventSnapshot ko(
      String targetName, String actorId, String targetId, long timestamp) {
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
