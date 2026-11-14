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
    private static final long serialVersionUID = 1L;

    private @Nullable UUID sessionId;
    private @Nullable UUID teamId;

    public EliminationId() {}

    public EliminationId(@Nullable UUID sessionId, @Nullable UUID teamId) {
      this.sessionId = sessionId;
      this.teamId = teamId;
    }

    @Override
    public boolean equals(Object o) {
      if (!(o instanceof EliminationId that)) {
        return false;
      }
      return Objects.equals(sessionId, that.sessionId) && Objects.equals(teamId, that.teamId);
    }

    @Override
    public int hashCode() {
      return Objects.hash(sessionId, teamId);
    }
  }

  /** Returns the session in which the team was eliminated. */
  public UUID getSessionId() {
    return sessionId;
  }

  /** Sets the session in which the team was eliminated. */
  public void setSessionId(UUID sessionId) {
    this.sessionId = sessionId;
  }

  /** Returns the eliminated team identifier. */
  public UUID getTeamId() {
    return teamId;
  }

  /** Sets the eliminated team identifier. */
  public void setTeamId(UUID teamId) {
    this.teamId = teamId;
  }

  /** Returns the round in which elimination occurred. */
  public Integer getRoundNo() {
    return roundNo;
  }

  /** Sets the round in which elimination occurred. */
  public void setRoundNo(Integer roundNo) {
    this.roundNo = roundNo;
  }

  /** Returns the reason for elimination. */
  public EliminationReason getReason() {
    return reason;
  }

  /** Sets the reason for elimination. */
  public void setReason(EliminationReason reason) {
    this.reason = reason;
  }

  /** Returns the timestamp of elimination. */
  public OffsetDateTime getEliminatedAt() {
    return eliminatedAt;
  }

  /** Sets the timestamp of elimination. */
  public void setEliminatedAt(OffsetDateTime eliminatedAt) {
    this.eliminatedAt = eliminatedAt;
  }
}
