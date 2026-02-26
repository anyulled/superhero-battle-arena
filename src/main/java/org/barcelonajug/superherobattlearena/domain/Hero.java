package org.barcelonajug.superherobattlearena.domain;

import static java.util.Objects.requireNonNull;

import java.util.Collections;
import java.util.List;

import org.jspecify.annotations.Nullable;

import io.swagger.v3.oas.annotations.media.Schema;

/** Represents a superhero with stats and role. */
@Schema(description = "Detailed information about a superhero")
public record Hero(
    @Schema(description = "Unique ID of the hero", example = "1") int id,
    @Schema(description = "Name of the hero", example = "Superman") String name,
    @Schema(description = "URL-friendly name of the hero", example = "superman") String slug,
    @Schema(description = "Power statistics of the hero") PowerStats powerstats,
    @Schema(description = "Combat role of the hero", example = "Tank") String role,
    @Schema(description = "Cost of selecting this hero", example = "15") Integer cost,
    @Schema(description = "Alignment of the hero", example = "good") @org.jspecify.annotations.Nullable String alignment,
    @Schema(description = "Publisher of the hero's comics", example = "DC Comics") @org.jspecify.annotations.Nullable String publisher,
    @Schema(description = "Physical appearance details") @org.jspecify.annotations.Nullable Appearance appearance,
    @Schema(description = "Biographical details") @org.jspecify.annotations.Nullable Biography biography,
    @Schema(description = "List of tags associated with the hero", example = "[\"flying\", \"super-strength\"]") List<String> tags,
    @Schema(description = "URLs to hero images") @org.jspecify.annotations.Nullable Images images) {
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

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private int id;
    private @Nullable String name;
    private @Nullable String slug;
    private @Nullable PowerStats powerstats;
    private @Nullable String role;
    private @Nullable Integer cost;
    private @Nullable String alignment;
    private @Nullable String publisher;
    private @Nullable Appearance appearance;
    private @Nullable Biography biography;
    private @Nullable List<String> tags;
    private @Nullable Images images;

    public Builder id(int id) {
      this.id = id;
      return this;
    }

    public Builder name(String name) {
      this.name = name;
      return this;
    }

    public Builder slug(String slug) {
      this.slug = slug;
      return this;
    }

    public Builder powerstats(PowerStats powerstats) {
      this.powerstats = powerstats;
      return this;
    }

    public Builder role(String role) {
      this.role = role;
      return this;
    }

    public Builder cost(@Nullable Integer cost) {
      this.cost = cost;
      return this;
    }

    public Builder alignment(@Nullable String alignment) {
      this.alignment = alignment;
      return this;
    }

    public Builder publisher(@Nullable String publisher) {
      this.publisher = publisher;
      return this;
    }

    public Builder appearance(@Nullable Appearance appearance) {
      this.appearance = appearance;
      return this;
    }

    public Builder biography(@Nullable Biography biography) {
      this.biography = biography;
      return this;
    }

    public Builder tags(@Nullable List<String> tags) {
      this.tags = tags;
      return this;
    }

    public Builder images(@Nullable Images images) {
      this.images = images;
      return this;
    }

    public Hero build() {
      return new Hero(
          id,
          requireNonNull(name),
          requireNonNull(slug),
          requireNonNull(powerstats),
          requireNonNull(role),
          cost != null ? cost : 10,
          alignment,
          publisher,
          appearance,
          biography,
          tags != null ? tags : Collections.emptyList(),
          images);
    }
  }

  @Schema(description = "Power statistics for a hero")
  public record PowerStats(
      @Schema(description = "Durability stat", example = "100") int durability,
      @Schema(description = "Strength stat", example = "100") int strength,
      @Schema(description = "Power level", example = "100") int power,
      @Schema(description = "Speed stat", example = "100") int speed,
      @Schema(description = "Intelligence stat", example = "100") int intelligence,
      @Schema(description = "Combat skill level", example = "100") int combat) {
    public static Builder builder() {
      return new Builder();
    }

    public static class Builder {
      private int durability;
      private int strength;
      private int power;
      private int speed;
      private int intelligence;
      private int combat;

      public Builder durability(int durability) {
        this.durability = durability;
        return this;
      }

      public Builder strength(int strength) {
        this.strength = strength;
        return this;
      }

      public Builder power(int power) {
        this.power = power;
        return this;
      }

      public Builder speed(int speed) {
        this.speed = speed;
        return this;
      }

      public Builder intelligence(int intelligence) {
        this.intelligence = intelligence;
        return this;
      }

      public Builder combat(int combat) {
        this.combat = combat;
        return this;
      }

      public PowerStats build() {
        return new PowerStats(durability, strength, power, speed, intelligence, combat);
      }
    }
  }

  @Schema(description = "Physical appearance of a hero")
  public record Appearance(
      @Schema(description = "Gender", example = "Male") @Nullable String gender,
      @Schema(description = "Race", example = "Kryptonian") @Nullable String race,
      @Schema(description = "Height in centimeters", example = "191") @Nullable Integer heightCm,
      @Schema(description = "Weight in kilograms", example = "101") @Nullable Integer weightKg,
      @Schema(description = "Eye color", example = "Blue") @Nullable String eyeColor,
      @Schema(description = "Hair color", example = "Black") @Nullable String hairColor) {
    public static Builder builder() {
      return new Builder();
    }

    public static class Builder {
      private @Nullable String gender;
      private @Nullable String race;
      private @Nullable Integer heightCm;
      private @Nullable Integer weightKg;
      private @Nullable String eyeColor;
      private @Nullable String hairColor;

      public Builder gender(@Nullable String gender) {
        this.gender = gender;
        return this;
      }

      public Builder race(@Nullable String race) {
        this.race = race;
        return this;
      }

      public Builder heightCm(@Nullable Integer heightCm) {
        this.heightCm = heightCm;
        return this;
      }

      public Builder weightKg(@Nullable Integer weightKg) {
        this.weightKg = weightKg;
        return this;
      }

      public Builder eyeColor(@Nullable String eyeColor) {
        this.eyeColor = eyeColor;
        return this;
      }

      public Builder hairColor(@Nullable String hairColor) {
        this.hairColor = hairColor;
        return this;
      }

      public Appearance build() {
        return new Appearance(gender, race, heightCm, weightKg, eyeColor, hairColor);
      }
    }
  }

  @Schema(description = "Biographical details of a hero")
  public record Biography(
      @Schema(description = "Full name", example = "Clark Kent") @Nullable String fullName,
      @Schema(description = "Place of birth", example = "Krypton") @Nullable String placeOfBirth,
      @Schema(description = "First appearance in comics", example = "Action Comics #1") @Nullable String firstAppearance,
      @Schema(description = "List of aliases", example = "[\"Man of Steel\", \"Kal-El\"]") @Nullable List<String> aliases) {
    public static Builder builder() {
      return new Builder();
    }

    public static class Builder {
      private @Nullable String fullName;
      private @Nullable String placeOfBirth;
      private @Nullable String firstAppearance;
      private @Nullable List<String> aliases;

      public Builder fullName(@Nullable String fullName) {
        this.fullName = fullName;
        return this;
      }

      public Builder placeOfBirth(@Nullable String placeOfBirth) {
        this.placeOfBirth = placeOfBirth;
        return this;
      }

      public Builder firstAppearance(@Nullable String firstAppearance) {
        this.firstAppearance = firstAppearance;
        return this;
      }

      public Builder aliases(@Nullable List<String> aliases) {
        this.aliases = aliases;
        return this;
      }

      public Biography build() {
        return new Biography(fullName, placeOfBirth, firstAppearance, aliases);
      }
    }
  }

  @Schema(description = "Hero images in various sizes")
  public record Images(
      @Schema(description = "Extra small image URL", example = "https://example.com/xs.jpg") @Nullable String xs,
      @Schema(description = "Small image URL", example = "https://example.com/sm.jpg") @Nullable String sm,
      @Schema(description = "Medium image URL", example = "https://example.com/md.jpg") @Nullable String md,
      @Schema(description = "Large image URL", example = "https://example.com/lg.jpg") @Nullable String lg) {
    public static Builder builder() {
      return new Builder();
    }

    public static class Builder {
      private @Nullable String xs;
      private @Nullable String sm;
      private @Nullable String md;
      private @Nullable String lg;

      public Builder xs(@Nullable String xs) {
        this.xs = xs;
        return this;
      }

      public Builder sm(@Nullable String sm) {
        this.sm = sm;
        return this;
      }

      public Builder md(@Nullable String md) {
        this.md = md;
        return this;
      }

      public Builder lg(@Nullable String lg) {
        this.lg = lg;
        return this;
      }

      public Images build() {
        return new Images(xs, sm, md, lg);
      }
    }
  }
}
