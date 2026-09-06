package org.barcelonajug.superherobattlearena.domain;

import java.util.UUID;
import org.barcelonajug.superherobattlearena.domain.json.MatchResult;
import org.jspecify.annotations.Nullable;

/** Represents a battle match between two teams in a tournament session. */
public class Match {

  /** Builder for creating Match instances. */
  public static class Builder {
    // We suppress init because these will be set by builder
    @SuppressWarnings("NullAway.Init")
    private UUID matchId;

    private @Nullable UUID sessionId;

    @SuppressWarnings("NullAway.Init")
    private Integer roundNo;

    @SuppressWarnings("NullAway.Init")
    private UUID teamA;

    @SuppressWarnings("NullAway.Init")
    private UUID teamB;

    private @Nullable UUID winnerTeam;

    @SuppressWarnings("NullAway.Init")
    private MatchStatus status;

    private @Nullable MatchResult resultJson;

    public Builder matchId(UUID matchId) {
      this.matchId = matchId;
      return this;
    }

    public Builder sessionId(@Nullable UUID sessionId) {
      this.sessionId = sessionId;
      return this;
    }

    public Builder roundNo(Integer roundNo) {
      this.roundNo = roundNo;
      return this;
    }

    public Builder teamA(UUID teamA) {
      this.teamA = teamA;
      return this;
    }

    public Builder teamB(UUID teamB) {
      this.teamB = teamB;
      return this;
    }

    public Builder winnerTeam(@Nullable UUID winnerTeam) {
      this.winnerTeam = winnerTeam;
      return this;
    }

    public Builder status(MatchStatus status) {
      this.status = status;
      return this;
    }

    public Builder resultJson(@Nullable MatchResult resultJson) {
      this.resultJson = resultJson;
      return this;
    }

    public Match build() {
      Match match = new Match();
      match.matchId = this.matchId;
      match.sessionId = this.sessionId;
      match.roundNo = this.roundNo;
      match.teamA = this.teamA;
      match.teamB = this.teamB;
      match.winnerTeam = this.winnerTeam;
      match.status = this.status;
      match.resultJson = this.resultJson;
      return match;
    }
  }

  public static Builder builder() {
    return new Builder();
  }

  @SuppressWarnings("NullAway.Init")
  private UUID matchId;

  private @Nullable UUID sessionId;

  @SuppressWarnings("NullAway.Init")
  private Integer roundNo;

  @SuppressWarnings("NullAway.Init")
  private UUID teamA;

  @SuppressWarnings("NullAway.Init")
  private UUID teamB;

  private @Nullable UUID winnerTeam;

  @SuppressWarnings("NullAway.Init")
  private MatchStatus status;

  private @Nullable MatchResult resultJson;

  public UUID getMatchId() {
    return matchId;
  }

  public void setMatchId(UUID matchId) {
    this.matchId = matchId;
  }

  public @Nullable UUID getSessionId() {
    return sessionId;
  }

  public void setSessionId(@Nullable UUID sessionId) {
    this.sessionId = sessionId;
  }

  public Integer getRoundNo() {
    return roundNo;
  }

  public void setRoundNo(Integer roundNo) {
    this.roundNo = roundNo;
  }

  public UUID getTeamA() {
    return teamA;
  }

  public void setTeamA(UUID teamA) {
    this.teamA = teamA;
  }

  public UUID getTeamB() {
    return teamB;
  }

  public void setTeamB(UUID teamB) {
    this.teamB = teamB;
  }

  public @Nullable UUID getWinnerTeam() {
    return winnerTeam;
  }

  public void setWinnerTeam(@Nullable UUID winnerTeam) {
    this.winnerTeam = winnerTeam;
  }

  public MatchStatus getStatus() {
    return status;
  }

  public void setStatus(MatchStatus status) {
    this.status = status;
  }

  public @Nullable MatchResult getResultJson() {
    return resultJson;
  }

  public void setResultJson(@Nullable MatchResult resultJson) {
    this.resultJson = resultJson;
  }
}
