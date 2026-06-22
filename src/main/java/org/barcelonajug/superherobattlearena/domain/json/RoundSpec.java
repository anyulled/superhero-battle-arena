package org.barcelonajug.superherobattlearena.domain.json;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Map;

/** Represents the specification and constraints for a round. */
@Schema(description = "Specification and constraints for a round")
public record RoundSpec(
    @Schema(description = "Description of the round", example = "The first round of the battle")
        String description,
    @Schema(description = "Size of the team", example = "3") int teamSize,
    @Schema(description = "Budget cap for the team", example = "100") int budgetCap,
    @Schema(
            description = "Required roles and their minimum counts",
            example = "{\"TANK\": 1, \"HEALER\": 1}")
        Map<String, Integer> requiredRoles,
    @Schema(
            description = "Maximum number of heroes allowed for each role",
            example = "{\"DPS\": 2}")
        Map<String, Integer> maxSameRole,
    @Schema(description = "List of banned tags", example = "[\"FLYING\"]") List<String> bannedTags,
    @Schema(description = "Tag modifiers affecting damage", example = "{\"TECH\": 1.1}")
        Map<String, Double> tagModifiers,
    @Schema(description = "Type of the map", example = "ARENA") String mapType,
    @Schema(description = "Allowed roles for the round", example = "[\"Fighter\", \"Tank\"]")
        List<String> allowedRoles,
    @Schema(description = "Allowed genders for the round", example = "[\"Male\", \"Female\"]")
        List<String> allowedGenders,
    @Schema(description = "Allowed races for the round", example = "[\"Human\", \"Kryptonian\"]")
        List<String> allowedRaces,
    @Schema(
            description = "Allowed publishers for the round",
            example = "[\"DC Comics\", \"Marvel Comics\"]")
        List<String> allowedPublishers,
    @Schema(description = "Allowed alignments for the round", example = "[\"good\", \"neutral\"]")
        List<String> allowedAlignments) {}
