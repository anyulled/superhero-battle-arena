package org.barcelonajug.superherobattlearena.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;
import org.barcelonajug.superherobattlearena.domain.json.DraftSubmission;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.jspecify.annotations.Nullable;

@Entity
@Table(name = "submissions")
@IdClass(SubmissionEntity.SubmissionId.class)
public class SubmissionEntity {

  @Id
  @Column(name = "team_id")
  @SuppressWarnings("NullAway.Init")
  private UUID teamId;

  @Id
  @Column(name = "round_no")
  @SuppressWarnings("NullAway.Init")
  private Integer roundNo;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "submission_json")
  private @Nullable DraftSubmission submissionJson;

  @Column(nullable = false)
  @SuppressWarnings("NullAway.Init")
  private Boolean accepted;

  @Column(name = "rejected_reason")
  private @Nullable String rejectedReason;

  @Column(name = "submitted_at")
  private @Nullable OffsetDateTime submittedAt;

  public static class SubmissionId implements Serializable {
    private @Nullable UUID teamId;
    private @Nullable Integer roundNo;

    public SubmissionId() {}

    public SubmissionId(@Nullable UUID teamId, @Nullable Integer roundNo) {
      this.teamId = teamId;
      this.roundNo = roundNo;
    }

    @Override
    public boolean equals(@Nullable Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      SubmissionId that = (SubmissionId) o;
      return Objects.equals(teamId, that.teamId) && Objects.equals(roundNo, that.roundNo);
    }

    @Override
    public int hashCode() {
      return Objects.hash(teamId, roundNo);
    }
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

  public @Nullable DraftSubmission getSubmissionJson() {
    return submissionJson;
  }

  public void setSubmissionJson(@Nullable DraftSubmission submissionJson) {
    this.submissionJson = submissionJson;
  }

  public Boolean getAccepted() {
    return accepted;
  }

  public void setAccepted(Boolean accepted) {
    this.accepted = accepted;
  }

  public @Nullable String getRejectedReason() {
    return rejectedReason;
  }

  public void setRejectedReason(@Nullable String rejectedReason) {
    this.rejectedReason = rejectedReason;
  }

  public @Nullable OffsetDateTime getSubmittedAt() {
    return submittedAt;
  }

  public void setSubmittedAt(@Nullable OffsetDateTime submittedAt) {
    this.submittedAt = submittedAt;
  }
}
