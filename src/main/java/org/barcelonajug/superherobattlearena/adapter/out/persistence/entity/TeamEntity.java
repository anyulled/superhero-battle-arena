package org.barcelonajug.superherobattlearena.adapter.out.persistence.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

@Entity
@Table(name = "teams")
public class TeamEntity {

  @Id
  @SuppressWarnings("NullAway.Init")
  private UUID teamId;

  private @Nullable UUID sessionId;

  @Column(nullable = false)
  @SuppressWarnings("NullAway.Init")
  private String name;

  @Column(name = "created_at", nullable = false)
  @SuppressWarnings("NullAway.Init")
  private OffsetDateTime createdAt;

  @ElementCollection
  @CollectionTable(name = "team_members", joinColumns = @JoinColumn(name = "team_id"))
  @Column(name = "member_name", nullable = false)
  @SuppressWarnings("NullAway.Init")
  private List<String> members;

  public UUID getTeamId() {
    return teamId;
  }

  public void setTeamId(UUID teamId) {
    this.teamId = teamId;
  }

  public @Nullable UUID getSessionId() {
    return sessionId;
  }

  public void setSessionId(@Nullable UUID sessionId) {
    this.sessionId = sessionId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(OffsetDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public List<String> getMembers() {
    return members;
  }

  public void setMembers(List<String> members) {
    this.members = members;
  }
}
