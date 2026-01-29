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

@Entity
@Table(name = "rounds")
public class RoundEntity {

  @Id
  @Column(name = "round_no")
  private Integer roundNo;

  @Column(name = "session_id")
  private UUID sessionId;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "spec_json")
  private RoundSpec specJson;

  @Enumerated(EnumType.STRING)
  private RoundStatus status;

  @Column(name = "submission_deadline")
  private OffsetDateTime submissionDeadline;

  private Long seed;

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

  public OffsetDateTime getSubmissionDeadline() {
    return submissionDeadline;
  }

  public void setSubmissionDeadline(OffsetDateTime submissionDeadline) {
    this.submissionDeadline = submissionDeadline;
  }

  public Long getSeed() {
    return seed;
  }

  public void setSeed(Long seed) {
    this.seed = seed;
  }
}
