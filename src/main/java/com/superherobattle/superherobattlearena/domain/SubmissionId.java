package com.superherobattle.superherobattlearena.domain;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class SubmissionId implements Serializable {
    private UUID teamId;
    private Integer roundNo;

    public SubmissionId() {
    }

    public SubmissionId(UUID teamId, Integer roundNo) {
        this.teamId = teamId;
        this.roundNo = roundNo;
    }

    public UUID getTeamId() {
        return teamId;
    }

    public void setTeamId(UUID teamId) {
        this.teamId = teamId;
    }

    public Integer getRoundNo() {
        return roundNo;
    }

    public void setRoundNo(Integer roundNo) {
        this.roundNo = roundNo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        SubmissionId that = (SubmissionId) o;
        return Objects.equals(teamId, that.teamId) && Objects.equals(roundNo, that.roundNo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(teamId, roundNo);
    }
}
