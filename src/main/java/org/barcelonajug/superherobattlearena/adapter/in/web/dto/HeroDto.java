package org.barcelonajug.superherobattlearena.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import org.barcelonajug.superherobattlearena.domain.Hero;
import org.jspecify.annotations.Nullable;
import org.springframework.hateoas.server.core.Relation;

@Relation(collectionRelation = "heroList")
@Schema(name = "Hero", description = "Detailed information about a superhero")
public record HeroDto(
    @Schema(description = "Unique ID of the hero", example = "1") int id,
    @Schema(description = "Name of the hero", example = "Superman") String name,
    @Schema(description = "URL-friendly name of the hero", example = "superman") String slug,
    @Schema(description = "Power statistics of the hero") PowerStats powerstats,
    @Schema(description = "Combat role of the hero", example = "Tank") String role,
    @Schema(description = "Cost of selecting this hero", example = "15") Integer cost,
    @Schema(description = "Alignment of the hero", example = "good") @Nullable String alignment,
    @Schema(description = "Publisher of the hero's comics", example = "DC Comics")
        @Nullable String publisher,
    @Schema(description = "Physical appearance details") @Nullable Appearance appearance,
    @Schema(description = "Biographical details") @Nullable Biography biography,
    @Schema(
            description = "List of tags associated with the hero",
            example = "[\"flying\", \"super-strength\"]")
        List<String> tags,
    @Schema(description = "URLs to hero images") @Nullable Images images) {
  public static HeroDto from(Hero value) {
    return new HeroDto(
        value.id(),
        value.name(),
        value.slug(),
        PowerStats.from(value.powerstats()),
        value.role(),
        value.cost(),
        value.alignment(),
        value.publisher(),
        appearanceFrom(value.appearance()),
        biographyFrom(value.biography()),
        value.tags(),
        imagesFrom(value.images()));
  }

  @Schema(name = "PowerStats", description = "Power statistics for a hero")
  public record PowerStats(
      @Schema(description = "Durability stat", example = "100") int durability,
      @Schema(description = "Strength stat", example = "100") int strength,
      @Schema(description = "Power level", example = "100") int power,
      @Schema(description = "Speed stat", example = "100") int speed,
      @Schema(description = "Intelligence stat", example = "100") int intelligence,
      @Schema(description = "Combat skill level", example = "100") int combat) {
    public static PowerStats from(Hero.PowerStats value) {
      return new PowerStats(
          value.durability(),
          value.strength(),
          value.power(),
          value.speed(),
          value.intelligence(),
          value.combat());
    }
  }

  private static @Nullable Appearance appearanceFrom(Hero.@Nullable Appearance value) {
    if (value == null) {
      return null;
    }
    return Appearance.from(value);
  }

  @Schema(name = "Appearance", description = "Physical appearance of a hero")
  public record Appearance(
      @Schema(description = "Gender", example = "Male") @Nullable String gender,
      @Schema(description = "Race", example = "Kryptonian") @Nullable String race,
      @Schema(description = "Height in centimeters", example = "191") @Nullable Integer heightCm,
      @Schema(description = "Weight in kilograms", example = "101") @Nullable Integer weightKg,
      @Schema(description = "Eye color", example = "Blue") @Nullable String eyeColor,
      @Schema(description = "Hair color", example = "Black") @Nullable String hairColor) {
    public static Appearance from(Hero.Appearance value) {
      return new Appearance(
          value.gender(),
          value.race(),
          value.heightCm(),
          value.weightKg(),
          value.eyeColor(),
          value.hairColor());
    }
  }

  private static @Nullable Biography biographyFrom(Hero.@Nullable Biography value) {
    if (value == null) {
      return null;
    }
    return Biography.from(value);
  }

  @Schema(name = "Biography", description = "Biographical details of a hero")
  public record Biography(
      @Schema(description = "Full name", example = "Clark Kent") @Nullable String fullName,
      @Schema(description = "Place of birth", example = "Krypton") @Nullable String placeOfBirth,
      @Schema(description = "First appearance in comics", example = "Action Comics #1")
          @Nullable String firstAppearance,
      @Schema(description = "List of aliases", example = "[\"Man of Steel\", \"Kal-El\"]")
          @Nullable List<String> aliases) {
    public static Biography from(Hero.Biography value) {
      return new Biography(
          value.fullName(), value.placeOfBirth(), value.firstAppearance(), value.aliases());
    }
  }

  private static @Nullable Images imagesFrom(Hero.@Nullable Images value) {
    if (value == null) {
      return null;
    }
    return Images.from(value);
  }

  @Schema(name = "Images", description = "Hero images in various sizes")
  public record Images(
      @Schema(description = "Extra small image URL", example = "https://example.com/xs.jpg")
          @Nullable String xs,
      @Schema(description = "Small image URL", example = "https://example.com/sm.jpg")
          @Nullable String sm,
      @Schema(description = "Medium image URL", example = "https://example.com/md.jpg")
          @Nullable String md,
      @Schema(description = "Large image URL", example = "https://example.com/lg.jpg")
          @Nullable String lg) {
    public static Images from(Hero.Images value) {
      return new Images(value.xs(), value.sm(), value.md(), value.lg());
    }
  }
}
