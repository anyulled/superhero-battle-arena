package org.barcelonajug.superherobattlearena.domain;

import org.barcelonajug.superherobattlearena.domain.json.DraftSubmission;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "submissions")
@IdClass(SubmissionId.class)
public class Submission {

    @Id
    @Column(name = "team_id")
    private UUID teamId;

    @Id
    @Column(name = "round_no")
    private Integer roundNo;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "submission_json")
    private DraftSubmission submissionJson;

    private Boolean accepted;

    @Column(name = "rejected_reason")
    private String rejectedReason;

    @Column(name = "submitted_at")
    private OffsetDateTime submittedAt;

    public Submission() {
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

    public DraftSubmission getSubmissionJson() {
        return submissionJson;
    }

    public void setSubmissionJson(DraftSubmission submissionJson) {
        this.submissionJson = submissionJson;
    }

    public Boolean getAccepted() {
        return accepted;
    }

    public void setAccepted(Boolean accepted) {
        this.accepted = accepted;
    }

    public String getRejectedReason() {
        return rejectedReason;
    }

    public void setRejectedReason(String rejectedReason) {
        this.rejectedReason = rejectedReason;
    }

    public OffsetDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(OffsetDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }
}
