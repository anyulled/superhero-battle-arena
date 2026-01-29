package org.barcelonajug.superherobattlearena.domain;

import java.util.UUID;
import org.barcelonajug.superherobattlearena.domain.json.MatchResult;

/** Represents a battle match between two teams in a tournament session. */
public class Match {

  /** Builder for creating Match instances. */
  public static class Builder {
    private UUID matchId;
    private UUID sessionId;
    private Integer roundNo;
    private UUID teamA;
    private UUID teamB;
    private UUID winnerTeam;
    private MatchStatus status;
    private MatchResult resultJson;

    public Builder matchId(UUID matchId) {
      this.matchId = matchId;
      return this;
    }

    public Builder sessionId(UUID sessionId) {
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

    public Builder winnerTeam(UUID winnerTeam) {
      this.winnerTeam = winnerTeam;
      return this;
    }

    public Builder status(MatchStatus status) {
      this.status = status;
      return this;
    }

    public Builder resultJson(MatchResult resultJson) {
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

  private UUID matchId;
  private UUID sessionId;
  private Integer roundNo;
  private UUID teamA;
  private UUID teamB;
  private UUID winnerTeam;
  private MatchStatus status;
  private MatchResult resultJson;

  public UUID getMatchId() {
    return matchId;
  }

  public void setMatchId(UUID matchId) {
    this.matchId = matchId;
  }

  public UUID getSessionId() {
    return sessionId;
  }

  public void setSessionId(UUID sessionId) {
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

  public UUID getWinnerTeam() {
    return winnerTeam;
  }

  public void setWinnerTeam(UUID winnerTeam) {
    this.winnerTeam = winnerTeam;
  }

  public MatchStatus getStatus() {
    return status;
  }

  public void setStatus(MatchStatus status) {
    this.status = status;
  }

  public MatchResult getResultJson() {
    return resultJson;
  }

  public void setResultJson(MatchResult resultJson) {
    this.resultJson = resultJson;
  }
}
