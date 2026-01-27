package org.barcelonajug.superherobattlearena.domain;

import org.barcelonajug.superherobattlearena.domain.json.MatchResult;

import java.util.UUID;

/**
 * Represents a battle match between two teams in a tournament session.
 */
public class Match {

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
