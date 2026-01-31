package org.barcelonajug.superherobattlearena.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import org.barcelonajug.superherobattlearena.domain.MatchStatus;
import org.barcelonajug.superherobattlearena.domain.json.MatchResult;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.jspecify.annotations.Nullable;

@Entity
@Table(name = "matches")
public class MatchEntity {

  @Id
  @Column(name = "match_id")
  @SuppressWarnings("NullAway.Init")
  private UUID matchId;

  @Column(name = "session_id")
  private @Nullable UUID sessionId;

  @Column(name = "round_no", nullable = false)
  @SuppressWarnings("NullAway.Init")
  private Integer roundNo;

  @Column(name = "team_a", nullable = false)
  @SuppressWarnings("NullAway.Init")
  private UUID teamA;

  @Column(name = "team_b", nullable = false)
  @SuppressWarnings("NullAway.Init")
  private UUID teamB;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  @SuppressWarnings("NullAway.Init")
  private MatchStatus status;

  @Column(name = "winner_team")
  private @Nullable UUID winnerTeam;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "result_json")
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

  public MatchStatus getStatus() {
    return status;
  }

  public void setStatus(MatchStatus status) {
    this.status = status;
  }

  public @Nullable UUID getWinnerTeam() {
    return winnerTeam;
  }

  public void setWinnerTeam(@Nullable UUID winnerTeam) {
    this.winnerTeam = winnerTeam;
  }

  public @Nullable MatchResult getResultJson() {
    return resultJson;
  }

  public void setResultJson(@Nullable MatchResult resultJson) {
    this.resultJson = resultJson;
  }
}
