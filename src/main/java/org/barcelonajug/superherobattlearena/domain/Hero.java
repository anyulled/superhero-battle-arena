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

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private int id;
    private String name;
    private String slug;
    private PowerStats powerstats;
    private String role;
    private Integer cost;
    private String alignment;
    private String publisher;
    private Appearance appearance;
    private Biography biography;
    private List<String> tags;
    private Images images;

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

    public Builder cost(Integer cost) {
      this.cost = cost;
      return this;
    }

    public Builder alignment(String alignment) {
      this.alignment = alignment;
      return this;
    }

    public Builder publisher(String publisher) {
      this.publisher = publisher;
      return this;
    }

    public Builder appearance(Appearance appearance) {
      this.appearance = appearance;
      return this;
    }

    public Builder biography(Biography biography) {
      this.biography = biography;
      return this;
    }

    public Builder tags(List<String> tags) {
      this.tags = tags;
      return this;
    }

    public Builder images(Images images) {
      this.images = images;
      return this;
    }

    public Hero build() {
      return new Hero(
          id,
          name,
          slug,
          powerstats,
          role,
          cost,
          alignment,
          publisher,
          appearance,
          biography,
          tags,
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
      String gender,
      String race,
      Integer heightCm,
      Integer weightKg,
      String eyeColor,
      String hairColor) {
    public static Builder builder() {
      return new Builder();
    }

    public static class Builder {
      private String gender;
      private String race;
      private Integer heightCm;
      private Integer weightKg;
      private String eyeColor;
      private String hairColor;

      public Builder gender(String gender) {
        this.gender = gender;
        return this;
      }

      public Builder race(String race) {
        this.race = race;
        return this;
      }

      public Builder heightCm(Integer heightCm) {
        this.heightCm = heightCm;
        return this;
      }

      public Builder weightKg(Integer weightKg) {
        this.weightKg = weightKg;
        return this;
      }

      public Builder eyeColor(String eyeColor) {
        this.eyeColor = eyeColor;
        return this;
      }

      public Builder hairColor(String hairColor) {
        this.hairColor = hairColor;
        return this;
      }

      public Appearance build() {
        return new Appearance(gender, race, heightCm, weightKg, eyeColor, hairColor);
      }
    }
  }

  public record Biography(
      String fullName, String placeOfBirth, String firstAppearance, List<String> aliases) {
    public static Builder builder() {
      return new Builder();
    }

    public static class Builder {
      private String fullName;
      private String placeOfBirth;
      private String firstAppearance;
      private List<String> aliases;

      public Builder fullName(String fullName) {
        this.fullName = fullName;
        return this;
      }

      public Builder placeOfBirth(String placeOfBirth) {
        this.placeOfBirth = placeOfBirth;
        return this;
      }

      public Builder firstAppearance(String firstAppearance) {
        this.firstAppearance = firstAppearance;
        return this;
      }

      public Builder aliases(List<String> aliases) {
        this.aliases = aliases;
        return this;
      }

      public Biography build() {
        return new Biography(fullName, placeOfBirth, firstAppearance, aliases);
      }
    }
  }

  public record Images(String xs, String sm, String md, String lg) {
    public static Builder builder() {
      return new Builder();
    }

    public static class Builder {
      private String xs;
      private String sm;
      private String md;
      private String lg;

      public Builder xs(String xs) {
        this.xs = xs;
        return this;
      }

      public Builder sm(String sm) {
        this.sm = sm;
        return this;
      }

      public Builder md(String md) {
        this.md = md;
        return this;
      }

      public Builder lg(String lg) {
        this.lg = lg;
        return this;
      }

      public Images build() {
        return new Images(xs, sm, md, lg);
      }
    }
  }
}
