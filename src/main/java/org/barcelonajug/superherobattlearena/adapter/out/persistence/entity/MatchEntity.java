package org.barcelonajug.superherobattlearena.adapter.out.persistence.entity;

import org.barcelonajug.superherobattlearena.domain.json.MatchResult;
import org.barcelonajug.superherobattlearena.domain.MatchStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "matches")
public class MatchEntity {

    @Id
    @Column(name = "match_id")
    private UUID matchId;

    @Column(name = "session_id")
    private UUID sessionId;

    @Column(name = "round_no")
    private Integer roundNo;

    @Column(name = "team_a")
    private UUID teamA;

    @Column(name = "team_b")
    private UUID teamB;

    @Enumerated(EnumType.STRING)
    private MatchStatus status;

    @Column(name = "winner_team")
    private UUID winnerTeam;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "result_json")
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

    public MatchStatus getStatus() {
        return status;
    }

    public void setStatus(MatchStatus status) {
        this.status = status;
    }

    public UUID getWinnerTeam() {
        return winnerTeam;
    }

    public void setWinnerTeam(UUID winnerTeam) {
        this.winnerTeam = winnerTeam;
    }

    public MatchResult getResultJson() {
        return resultJson;
    }

    public void setResultJson(MatchResult resultJson) {
        this.resultJson = resultJson;
    }
}
