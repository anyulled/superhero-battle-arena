package org.barcelonajug.superherobattlearena.domain.mother;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;

import java.util.List;
import java.util.Map;
import org.barcelonajug.superherobattlearena.domain.json.RoundSpec;

public final class RoundSpecMother {

  private RoundSpecMother() {
    // Prevent instantiation
  }

  public static RoundSpec aStandardRoundSpec() {
    return new RoundSpec(
        "Standard 5v5 Round", 5, 100, emptyMap(), emptyMap(), emptyList(), emptyMap(), "ARENA");
  }

  public static RoundSpec aRoundSpecWithTeamSize(int teamSize) {
    return new RoundSpec(
        "Custom Size Round",
        teamSize,
        1000,
        emptyMap(),
        emptyMap(),
        emptyList(),
        emptyMap(),
        "ARENA_1");
  }

  public static RoundSpec aRoundSpecWithTags(
      Map<String, Double> tagModifiers, List<String> bannedTags) {
    return new RoundSpec(
        "Tagged Round", 5, 100, emptyMap(), emptyMap(), bannedTags, tagModifiers, "ARENA");
  }

  public static RoundSpec aRoundSpecWithRoles(
      Map<String, Integer> requiredRoles, Map<String, Integer> maxSameRole) {
    return new RoundSpec(
        "Role Constrained Round",
        5,
        100,
        requiredRoles,
        maxSameRole,
        emptyList(),
        emptyMap(),
        "ARENA");
  }
}
