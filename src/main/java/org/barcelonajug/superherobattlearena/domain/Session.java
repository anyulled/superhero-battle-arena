package org.barcelonajug.superherobattlearena.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;
import java.util.UUID;

@Schema(description = "A tournament session")
public class Session {
  @Schema(
      description = "Unique ID of the session",
      example = "550e8400-e29b-41d4-a716-446655440000")
  private UUID sessionId;

  @Schema(description = "Session creation timestamp")
  private OffsetDateTime createdAt;

  @Schema(description = "Whether the session is currently active", example = "true")
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
