package org.barcelonajug.superherobattlearena.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.barcelonajug.superherobattlearena.domain.RoundStatus;
import org.barcelonajug.superherobattlearena.domain.json.RoundSpec;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.jspecify.annotations.Nullable;

@Entity
@Table(name = "rounds")
public class RoundEntity {

  @Id
  @Column(name = "round_id")
  @SuppressWarnings("NullAway.Init")
  private UUID roundId;

  @Column(name = "round_no")
  @SuppressWarnings("NullAway.Init")
  private Integer roundNo;

  @Column(name = "session_id")
  private @Nullable UUID sessionId;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "spec_json")
  private @Nullable RoundSpec specJson;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  @SuppressWarnings("NullAway.Init")
  private RoundStatus status;

  @Column(name = "submission_deadline")
  private @Nullable OffsetDateTime submissionDeadline;

  private @Nullable Long seed;

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

  public @Nullable UUID getSessionId() {
    return sessionId;
  }

  public void setSessionId(@Nullable UUID sessionId) {
    this.sessionId = sessionId;
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

  public @Nullable Long getSeed() {
    return seed;
  }

  public void setSeed(@Nullable Long seed) {
    this.seed = seed;
  }
}
