package org.barcelonajug.superherobattlearena.domain;

import java.time.OffsetDateTime;
import java.util.UUID;
import org.barcelonajug.superherobattlearena.domain.json.DraftSubmission;
import org.jspecify.annotations.Nullable;

/** Represents a team's submission for a round. */
public class Submission {
  @SuppressWarnings("NullAway.Init")
  private UUID teamId;

  @SuppressWarnings("NullAway.Init")
  private Integer roundNo;

  private @Nullable DraftSubmission submissionJson;

  // accepted is primitive boolean in DB (not null), so Boolean here is fine
  // (non-null)
  // Assuming strict domain
  @SuppressWarnings("NullAway.Init")
  private Boolean accepted;

  private @Nullable String rejectedReason;
  private @Nullable OffsetDateTime submittedAt;

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
