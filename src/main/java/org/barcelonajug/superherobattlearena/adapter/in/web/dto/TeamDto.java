package org.barcelonajug.superherobattlearena.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.barcelonajug.superherobattlearena.domain.Team;

@Schema(name = "Team", description = "Team information and list of selected heroes")
public record TeamDto(
    @Schema(description = "Unique ID of the team", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID teamId,
    @Schema(
            description = "ID of the session this team belongs to",
            example = "550e8400-e29b-41d4-a716-446655440001")
        UUID sessionId,
    @Schema(description = "Name of the team", example = "The Avengers") String name,
    @Schema(description = "Team registration timestamp") OffsetDateTime createdAt,
    @Schema(description = "List of members in the team", example = "[\"Member 1\", \"Member 2\"]")
        List<String> members) {
  public static TeamDto from(Team value) {
    return new TeamDto(
        value.teamId(), value.sessionId(), value.name(), value.createdAt(), value.members());
  }
}
