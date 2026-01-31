package org.barcelonajug.superherobattlearena.adapter.out.persistence.mapper;

import org.barcelonajug.superherobattlearena.adapter.out.persistence.entity.SuperheroAppearanceEntity;
import org.barcelonajug.superherobattlearena.adapter.out.persistence.entity.SuperheroBiographyEntity;
import org.barcelonajug.superherobattlearena.adapter.out.persistence.entity.SuperheroEntity;
import org.barcelonajug.superherobattlearena.adapter.out.persistence.entity.SuperheroImagesEntity;
import org.barcelonajug.superherobattlearena.adapter.out.persistence.entity.SuperheroPowerStatsEntity;
import org.barcelonajug.superherobattlearena.domain.Hero;
import org.jspecify.annotations.Nullable;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SuperheroMapper {

  @Mapping(target = "role", constant = "Fighter")
  @Mapping(target = "tags", ignore = true)
  @Mapping(target = "cost", source = "powerStats.cost")
  @Mapping(target = "powerstats", source = "powerStats")
  @Mapping(target = "images", source = "images")
  @Mapping(target = "appearance", source = "appearance")
  @Mapping(target = "biography", source = "biography")
  @Nullable Hero toDomain(SuperheroEntity entity);

  @Mapping(source = "intelligence", target = "intelligence")
  @Mapping(source = "strength", target = "strength")
  @Mapping(source = "speed", target = "speed")
  @Mapping(source = "durability", target = "durability")
  @Mapping(source = "power", target = "power")
  @Mapping(source = "combat", target = "combat")
  Hero.@Nullable PowerStats toPowerStats(SuperheroPowerStatsEntity entity);

  @Mapping(source = "heightCm", target = "heightCm")
  @Mapping(source = "weightKg", target = "weightKg")
  Hero.@Nullable Appearance toAppearance(SuperheroAppearanceEntity entity);

  Hero.@Nullable Biography toBiography(SuperheroBiographyEntity entity);

  @Mapping(source = "xsUrl", target = "xs")
  @Mapping(source = "smUrl", target = "sm")
  @Mapping(source = "mdUrl", target = "md")
  @Mapping(source = "lgUrl", target = "lg")
  Hero.@Nullable Images toImages(SuperheroImagesEntity entity);
}
