package org.barcelonajug.superherobattlearena.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;
import org.barcelonajug.superherobattlearena.domain.EliminationReason;
import org.jspecify.annotations.Nullable;

@Entity
@Table(name = "eliminated_teams")
@IdClass(EliminationEntity.EliminationId.class)
public class EliminationEntity {

  @Id
  @Column(name = "session_id")
  @SuppressWarnings("NullAway.Init")
  private UUID sessionId;

  @Id
  @Column(name = "team_id")
  @SuppressWarnings("NullAway.Init")
  private UUID teamId;

  @Column(name = "round_no", nullable = false)
  @SuppressWarnings("NullAway.Init")
  private Integer roundNo;

  @Enumerated(EnumType.STRING)
  @Column(name = "reason", nullable = false)
  @SuppressWarnings("NullAway.Init")
  private EliminationReason reason;

  @Column(name = "eliminated_at", nullable = false)
  @SuppressWarnings("NullAway.Init")
  private OffsetDateTime eliminatedAt;

  public static class EliminationId implements Serializable {
    private @Nullable UUID sessionId;
    private @Nullable UUID teamId;

    public EliminationId() {}

    public EliminationId(@Nullable UUID sessionId, @Nullable UUID teamId) {
      this.sessionId = sessionId;
      this.teamId = teamId;
    }

    @Override
    public boolean equals(@Nullable Object o) {
      if (this == o) return true;
      if (!(o instanceof EliminationId that)) return false;
      return Objects.equals(sessionId, that.sessionId) && Objects.equals(teamId, that.teamId);
    }

    @Override
    public int hashCode() {
      return Objects.hash(sessionId, teamId);
    }
  }

  public UUID getSessionId() {
    return sessionId;
  }

  public void setSessionId(UUID sessionId) {
    this.sessionId = sessionId;
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

  public EliminationReason getReason() {
    return reason;
  }

  public void setReason(EliminationReason reason) {
    this.reason = reason;
  }

  public OffsetDateTime getEliminatedAt() {
    return eliminatedAt;
  }

  public void setEliminatedAt(OffsetDateTime eliminatedAt) {
    this.eliminatedAt = eliminatedAt;
  }
}
