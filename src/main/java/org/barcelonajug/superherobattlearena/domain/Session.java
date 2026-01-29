package org.barcelonajug.superherobattlearena.domain;

import java.time.OffsetDateTime;
import java.util.UUID;

public class Session {
  private UUID sessionId;
  private OffsetDateTime createdAt;
  private boolean active;

  public Session(UUID sessionId, OffsetDateTime createdAt, boolean active) {
    this.sessionId = sessionId;
    this.createdAt = createdAt;
    this.active = active;
  }

  public UUID getSessionId() {
    return sessionId;
  }

  public void setSessionId(UUID sessionId) {
    this.sessionId = sessionId;
  }

  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(OffsetDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public boolean isActive() {
    return active;
  }

  public void setActive(boolean active) {
    this.active = active;
  }
}
