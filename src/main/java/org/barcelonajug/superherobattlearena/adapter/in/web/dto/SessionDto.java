package org.barcelonajug.superherobattlearena.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.barcelonajug.superherobattlearena.domain.Session;

@Schema(name = "Session", description = "A tournament session")
public record SessionDto(
    @Schema(
            description = "Unique ID of the session",
            example = "550e8400-e29b-41d4-a716-446655440000")
        UUID sessionId,
    @Schema(description = "Session creation timestamp") OffsetDateTime createdAt,
    @Schema(description = "Whether the session is currently active", example = "true")
        boolean active) {
  public static SessionDto from(Session value) {
    return new SessionDto(value.getSessionId(), value.getCreatedAt(), value.isActive());
  }
}
