package org.barcelonajug.superherobattlearena.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import org.barcelonajug.superherobattlearena.domain.json.RoundSpec;

@Schema(description = "Request to create a new round with specific constraints")
public record CreateRoundRequest(
    @Schema(
            description = "ID of the tournament session",
            example = "550e8400-e29b-41d4-a716-446655440001")
        UUID sessionId,
    @Schema(description = "Number of the round", example = "1") Integer roundNo,
    @Schema(description = "Specifications and constraints for the round") RoundSpec spec) {}
