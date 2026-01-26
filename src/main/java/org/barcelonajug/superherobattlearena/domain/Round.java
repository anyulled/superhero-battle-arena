package org.barcelonajug.superherobattlearena.domain;

import org.barcelonajug.superherobattlearena.domain.json.RoundSpec;

public class Round {
    private Integer roundNo;
    private Long seed;
    private RoundSpec specJson;
    private java.time.OffsetDateTime submissionDeadline;

    public Integer getRoundNo() {
        return roundNo;
    }

    public void setRoundNo(Integer roundNo) {
        this.roundNo = roundNo;
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

    public java.time.OffsetDateTime getSubmissionDeadline() {
        return submissionDeadline;
    }

    public void setSubmissionDeadline(java.time.OffsetDateTime submissionDeadline) {
        this.submissionDeadline = submissionDeadline;
    }
}
