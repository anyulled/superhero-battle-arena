package org.barcelonajug.superherobattlearena.domain.json;

import java.util.UUID;

/** Represents a specific event during a match in JSON format. */
public record MatchEvent(
    String type,
    long timestamp,
    String description,
    @org.jspecify.annotations.Nullable String actorId,
    @org.jspecify.annotations.Nullable String targetId,
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
