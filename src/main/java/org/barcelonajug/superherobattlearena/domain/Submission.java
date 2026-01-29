package org.barcelonajug.superherobattlearena.domain;

import java.time.OffsetDateTime;
import java.util.UUID;
import org.barcelonajug.superherobattlearena.domain.json.DraftSubmission;

/** Represents a team's submission for a round. */
public class Submission {
  private UUID teamId;
  private Integer roundNo;
  private DraftSubmission submissionJson;
  private Boolean accepted;
  private String rejectedReason;
  private OffsetDateTime submittedAt;

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
