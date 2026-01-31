package org.barcelonajug.superherobattlearena.domain;

import static java.util.Objects.requireNonNull;

import java.util.List;
import org.jspecify.annotations.Nullable;

/** Represents a superhero with stats and role. */
public record Hero(
    int id,
    String name,
    String slug,
    PowerStats powerstats,
    String role,
    Integer cost,
    @org.jspecify.annotations.Nullable String alignment,
    @org.jspecify.annotations.Nullable String publisher,
    @org.jspecify.annotations.Nullable Appearance appearance,
    @org.jspecify.annotations.Nullable Biography biography,
    List<String> tags,
    @org.jspecify.annotations.Nullable Images images) {
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
          tags != null ? tags : java.util.Collections.emptyList(),
          images);
    }
  }

  public record PowerStats(
      int durability, int strength, int power, int speed, int intelligence, int combat) {
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

  public record Appearance(
      @Nullable String gender,
      @Nullable String race,
      @Nullable Integer heightCm,
      @Nullable Integer weightKg,
      @Nullable String eyeColor,
      @Nullable String hairColor) {
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

  public record Biography(
      @Nullable String fullName,
      @Nullable String placeOfBirth,
      @Nullable String firstAppearance,
      @Nullable List<String> aliases) {
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

  public record Images(
      @Nullable String xs, @Nullable String sm, @Nullable String md, @Nullable String lg) {
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
