package org.barcelonajug.superherobattlearena.domain;

import java.util.List;

/** Represents a superhero with stats and role. */
public record Hero(
    int id,
    String name,
    String slug,
    PowerStats powerstats,
    String role,
    Integer cost,
    String alignment,
    String publisher,
    Appearance appearance,
    Biography biography,
    List<String> tags,
    Images images) {
  /** Constructs a new Hero with default values if needed. */
  public Hero {
    if (role == null) {
      role = "Fighter";
    }
    if (cost == null) {
      cost = 10;
    }
    if (tags == null) {
      tags = List.of();
    }
  }

  public record PowerStats(
      int durability, int strength, int power, int speed, int intelligence, int combat) {}

  public record Appearance(
      String gender,
      String race,
      Integer heightCm,
      Integer weightKg,
      String eyeColor,
      String hairColor) {}

  public record Biography(
      String fullName, String placeOfBirth, String firstAppearance, List<String> aliases) {}

  public record Images(String xs, String sm, String md, String lg) {}
}
