package org.barcelonajug.superherobattlearena.testfixtures;

import java.util.UUID;
import org.barcelonajug.superherobattlearena.adapter.out.persistence.entity.MatchEntity;
import org.barcelonajug.superherobattlearena.domain.MatchStatus;
import org.jspecify.annotations.Nullable;

/**
 * Object Mother pattern for creating MatchEntity test fixtures. Provides factory methods for common
 * test scenarios with sensible defaults.
 */
public final class MatchEntityMother {

  private MatchEntityMother() {
    // Utility class
  }

  /**
   * Creates a pending match with random IDs and default values.
   *
   * @return a new MatchEntity in PENDING status
   */
  public static MatchEntity pendingMatch() {
    return pendingMatch(UUID.randomUUID(), UUID.randomUUID(), 1);
  }

  /**
   * Creates a pending match for a specific session and round.
   *
   * @param sessionId the session ID
   * @param roundNo the round number
   * @return a new MatchEntity in PENDING status
   */
  public static MatchEntity pendingMatch(UUID sessionId, int roundNo) {
    return pendingMatch(UUID.randomUUID(), sessionId, roundNo);
  }

  /**
   * Creates a pending match with specific IDs.
   *
   * @param matchId the match ID
   * @param sessionId the session ID
   * @param roundNo the round number
   * @return a new MatchEntity in PENDING status
   */
  public static MatchEntity pendingMatch(UUID matchId, UUID sessionId, int roundNo) {
    return match(matchId, sessionId, roundNo, MatchStatus.PENDING, null);
  }

  /**
   * Creates a completed match with a winner.
   *
   * @param sessionId the session ID
   * @param roundNo the round number
   * @param winner the winning team ID
   * @return a new MatchEntity in COMPLETED status
   */
  public static MatchEntity completedMatch(UUID sessionId, int roundNo, UUID winner) {
    return match(UUID.randomUUID(), sessionId, roundNo, MatchStatus.COMPLETED, winner);
  }

  /**
   * Creates an in-progress match.
   *
   * @param sessionId the session ID
   * @param roundNo the round number
   * @return a new MatchEntity in IN_PROGRESS status
   */
  public static MatchEntity inProgressMatch(UUID sessionId, int roundNo) {
    return match(UUID.randomUUID(), sessionId, roundNo, MatchStatus.IN_PROGRESS, null);
  }

  /**
   * Creates a match with full control over all fields.
   *
   * @param matchId the match ID
   * @param sessionId the session ID
   * @param roundNo the round number
   * @param status the match status
   * @param winner the winning team ID (nullable)
   * @return a new MatchEntity
   */
  public static MatchEntity match(
      UUID matchId, UUID sessionId, int roundNo, MatchStatus status, @Nullable UUID winner) {
    MatchEntity match = new MatchEntity();
    match.setMatchId(matchId);
    match.setSessionId(sessionId);
    match.setRoundNo(roundNo);
    match.setTeamA(UUID.randomUUID());
    match.setTeamB(UUID.randomUUID());
    match.setStatus(status);
    match.setWinnerTeam(winner);
    return match;
  }

  /**
   * Creates a match builder for fine-grained control.
   *
   * @return a new MatchEntityBuilder
   */
  public static MatchEntityBuilder builder() {
    return new MatchEntityBuilder();
  }

  /** Builder for creating custom MatchEntity instances. */
  public static final class MatchEntityBuilder {
    private UUID matchId = UUID.randomUUID();
    private UUID sessionId = UUID.randomUUID();
    private int roundNo = 1;
    private UUID teamA = UUID.randomUUID();
    private UUID teamB = UUID.randomUUID();
    private MatchStatus status = MatchStatus.PENDING;
    private @Nullable UUID winner = null;

    private MatchEntityBuilder() {}

    public MatchEntityBuilder matchId(UUID matchId) {
      this.matchId = matchId;
      return this;
    }

    public MatchEntityBuilder sessionId(UUID sessionId) {
      this.sessionId = sessionId;
      return this;
    }

    public MatchEntityBuilder roundNo(int roundNo) {
      this.roundNo = roundNo;
      return this;
    }

    public MatchEntityBuilder teamA(UUID teamA) {
      this.teamA = teamA;
      return this;
    }

    public MatchEntityBuilder teamB(UUID teamB) {
      this.teamB = teamB;
      return this;
    }

    public MatchEntityBuilder status(MatchStatus status) {
      this.status = status;
      return this;
    }

    public MatchEntityBuilder winner(@Nullable UUID winner) {
      this.winner = winner;
      return this;
    }

    public MatchEntity build() {
      MatchEntity match = new MatchEntity();
      match.setMatchId(matchId);
      match.setSessionId(sessionId);
      match.setRoundNo(roundNo);
      match.setTeamA(teamA);
      match.setTeamB(teamB);
      match.setStatus(status);
      match.setWinnerTeam(winner);
      return match;
    }
  }
}
