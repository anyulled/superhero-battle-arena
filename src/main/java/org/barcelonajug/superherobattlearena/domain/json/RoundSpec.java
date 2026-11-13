package org.barcelonajug.superherobattlearena.domain.json;

import java.util.List;
import java.util.Map;

/** Represents the specification and constraints for a round. */
public record RoundSpec(
    String description,
    int teamSize,
    int budgetCap,
    Map<String, Integer> requiredRoles,
    Map<String, Integer> maxSameRole,
    List<String> bannedTags,
    Map<String, Double> tagModifiers,
    String mapType,
    List<String> allowedRoles,
    List<String> allowedGenders,
    List<String> allowedRaces,
    List<String> allowedPublishers,
    List<String> allowedAlignments) {}
