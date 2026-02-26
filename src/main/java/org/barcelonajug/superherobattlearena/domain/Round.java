package org.barcelonajug.superherobattlearena.domain;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.barcelonajug.superherobattlearena.domain.json.RoundSpec;
import org.jspecify.annotations.Nullable;

/** Represents a round in a tournament session. */
public class Round {
  @SuppressWarnings("NullAway.Init")
  private UUID roundId;

  @SuppressWarnings("NullAway.Init")
  private Integer roundNo;

  @SuppressWarnings("NullAway.Init")
  private UUID sessionId;

  private @Nullable Long seed;
  private @Nullable RoundSpec specJson;

  @SuppressWarnings("NullAway.Init")
  private RoundStatus status;

  private @Nullable OffsetDateTime submissionDeadline;

  public UUID getRoundId() {
    return roundId;
  }

  public void setRoundId(UUID roundId) {
    this.roundId = roundId;
  }

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

  public @Nullable Long getSeed() {
    return seed;
  }

  public void setSeed(@Nullable Long seed) {
    this.seed = seed;
  }

  public @Nullable RoundSpec getSpecJson() {
    return specJson;
  }

  public void setSpecJson(@Nullable RoundSpec specJson) {
    this.specJson = specJson;
  }

  public RoundStatus getStatus() {
    return status;
  }

  public void setStatus(RoundStatus status) {
    this.status = status;
  }

  public @Nullable OffsetDateTime getSubmissionDeadline() {
    return submissionDeadline;
  }

  public void setSubmissionDeadline(@Nullable OffsetDateTime submissionDeadline) {
    this.submissionDeadline = submissionDeadline;
  }
}
