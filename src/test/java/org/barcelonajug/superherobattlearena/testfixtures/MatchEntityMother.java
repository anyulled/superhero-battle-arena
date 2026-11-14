package org.barcelonajug.superherobattlearena.testfixtures;

import java.util.UUID;
import org.barcelonajug.superherobattlearena.adapter.out.persistence.entity.MatchEntity;
import org.barcelonajug.superherobattlearena.domain.MatchStatus;
import org.jspecify.annotations.Nullable;

public final class MatchEntityMother {
  private MatchEntityMother() {}

  public static MatchEntity pendingMatch() {
    return match(UUID.randomUUID(), UUID.randomUUID(), 1, MatchStatus.PENDING, null);
  }

  public static MatchEntity pendingMatch(UUID sessionId, int roundNo) {
    return match(UUID.randomUUID(), sessionId, roundNo, MatchStatus.PENDING, null);
  }

  public static MatchEntity pendingMatch(UUID matchId, UUID sessionId, int roundNo) {
    return match(matchId, sessionId, roundNo, MatchStatus.PENDING, null);
  }

  public static MatchEntity completedMatch(UUID sessionId, int roundNo, UUID winner) {
    return match(UUID.randomUUID(), sessionId, roundNo, MatchStatus.COMPLETED, winner);
  }

  public static MatchEntity inProgressMatch(UUID sessionId, int roundNo) {
    return match(UUID.randomUUID(), sessionId, roundNo, MatchStatus.IN_PROGRESS, null);
  }

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

  public static MatchEntityBuilder builder() {
    return new MatchEntityBuilder();
  }

  public static final class MatchEntityBuilder {
    private UUID matchId = UUID.randomUUID();
    private UUID sessionId = UUID.randomUUID();
    private int roundNo = 1;
    private UUID teamA = UUID.randomUUID();
    private UUID teamB = UUID.randomUUID();
    private MatchStatus status = MatchStatus.PENDING;
    private @Nullable UUID winner = null;

    MatchEntityBuilder() {}

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
