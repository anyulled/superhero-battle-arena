package org.barcelonajug.superherobattlearena.domain.mother;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.barcelonajug.superherobattlearena.domain.json.RoundSpec;

public final class RoundSpecMother {

  private RoundSpecMother() {
    // Prevent instantiation
  }

  public static RoundSpec aStandardRoundSpec() {
    return new RoundSpec(
        "Standard 5v5 Round",
        5,
        100,
        Collections.emptyMap(),
        Collections.emptyMap(),
        Collections.emptyList(),
        Collections.emptyMap(),
        "ARENA");
  }

  public static RoundSpec aRoundSpecWithTeamSize(int teamSize) {
    return new RoundSpec(
        "Custom Size Round",
        teamSize,
        1000,
        Collections.emptyMap(),
        Collections.emptyMap(),
        Collections.emptyList(),
        Collections.emptyMap(),
        "ARENA_1");
  }

  public static RoundSpec aRoundSpecWithTags(
      Map<String, Double> tagModifiers, List<String> bannedTags) {
    return new RoundSpec(
        "Tagged Round",
        5,
        100,
        Collections.emptyMap(),
        Collections.emptyMap(),
        bannedTags,
        tagModifiers,
        "ARENA");
  }

  public static RoundSpec aRoundSpecWithRoles(
      Map<String, Integer> requiredRoles, Map<String, Integer> maxSameRole) {
    return new RoundSpec(
        "Role Constrained Round",
        5,
        100,
        requiredRoles,
        maxSameRole,
        Collections.emptyList(),
        Collections.emptyMap(),
        "ARENA");
  }
}
