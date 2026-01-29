package org.barcelonajug.superherobattlearena.adapter.out.persistence.mapper;

import org.barcelonajug.superherobattlearena.adapter.out.persistence.entity.SuperheroEntity;
import org.barcelonajug.superherobattlearena.domain.Hero;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SuperheroMapper {

    @Mapping(target = "role", constant = "Fighter") // Default value in Hero constructor logic, but explicit here is
                                                    // good too
    @Mapping(target = "tags", ignore = true) // Not in DB yet
    @Mapping(target = "cost", source = "powerStats.cost")
    @Mapping(target = "powerstats", source = "powerStats")
    @Mapping(target = "images", source = "images") // Assuming structure matches
    @Mapping(target = "appearance", source = "appearance")
    @Mapping(target = "biography", source = "biography")
    Hero toDomain(SuperheroEntity entity);

    @Mapping(source = "intelligence", target = "intelligence")
    @Mapping(source = "strength", target = "strength")
    @Mapping(source = "speed", target = "speed")
    @Mapping(source = "durability", target = "durability")
    @Mapping(source = "power", target = "power")
    @Mapping(source = "combat", target = "combat")
    Hero.PowerStats toPowerStats(
            org.barcelonajug.superherobattlearena.adapter.out.persistence.entity.SuperheroPowerStatsEntity entity);

    @Mapping(source = "heightCm", target = "heightCm")
    @Mapping(source = "weightKg", target = "weightKg")
    Hero.Appearance toAppearance(
            org.barcelonajug.superherobattlearena.adapter.out.persistence.entity.SuperheroAppearanceEntity entity);

    Hero.Biography toBiography(
            org.barcelonajug.superherobattlearena.adapter.out.persistence.entity.SuperheroBiographyEntity entity);

    @Mapping(source = "xsUrl", target = "xs")
    @Mapping(source = "smUrl", target = "sm")
    @Mapping(source = "mdUrl", target = "md")
    @Mapping(source = "lgUrl", target = "lg")
    Hero.Images toImages(
            org.barcelonajug.superherobattlearena.adapter.out.persistence.entity.SuperheroImagesEntity entity);
}
