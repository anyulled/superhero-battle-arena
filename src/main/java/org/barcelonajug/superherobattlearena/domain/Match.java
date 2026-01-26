package org.barcelonajug.superherobattlearena.domain;

import java.util.UUID;
import org.barcelonajug.superherobattlearena.domain.json.MatchResult;

public class Match {

    private UUID matchId;
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
