package org.barcelonajug.superherobattlearena.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/** Represents a team participated in the arena. */
@Schema(description = "Team information and list of selected heroes")
public record Team(
    @Schema(description = "Unique ID of the team", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID teamId,
    @Schema(
            description = "ID of the session this team belongs to",
            example = "550e8400-e29b-41d4-a716-446655440001")
        UUID sessionId,
    @Schema(description = "Name of the team", example = "The Avengers") String name,
    @Schema(description = "Team registration timestamp") OffsetDateTime createdAt,
    @Schema(description = "List of hero IDs in the team", example = "[\"1\", \"2\", \"3\"]")
        List<String> members) {
  /**
   * Constructs a new Team.
   *
   * @param teamId the team ID.
   * @param sessionId the session ID.
   * @param name the team name.
   * @param createdAt the creation timestamp.
   * @param members the list of hero IDs.
   */
  public Team {
    if (members == null || members.size() < 2) {
      throw new IllegalArgumentException("A team must have at least two members.");
    }
  }
}
