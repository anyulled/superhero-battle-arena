package org.barcelonajug.superherobattlearena.domain;

import java.util.UUID;
import org.barcelonajug.superherobattlearena.domain.json.RoundSpec;

public class Round {
    private Integer roundNo;
    private UUID sessionId;
    private Long seed;
    private RoundSpec specJson;
    private RoundStatus status;
    private java.time.OffsetDateTime submissionDeadline;

    public Integer getRoundNo() {
        return roundNo;
    }

    public void setRoundNo(Integer roundNo) {
        this.roundNo = roundNo;
    }

    public UUID getSessionId() {
        return sessionId;
    }

    public void setSessionId(UUID sessionId) {
        this.sessionId = sessionId;
    }

    public Long getSeed() {
        return seed;
    }

    public void setSeed(Long seed) {
        this.seed = seed;
    }

    public RoundSpec getSpecJson() {
        return specJson;
    }

    public void setSpecJson(RoundSpec specJson) {
        this.specJson = specJson;
    }

    public RoundStatus getStatus() {
        return status;
    }

    public void setStatus(RoundStatus status) {
        this.status = status;
    }

    public java.time.OffsetDateTime getSubmissionDeadline() {
        return submissionDeadline;
    }

    public void setSubmissionDeadline(java.time.OffsetDateTime submissionDeadline) {
        this.submissionDeadline = submissionDeadline;
    }
}
