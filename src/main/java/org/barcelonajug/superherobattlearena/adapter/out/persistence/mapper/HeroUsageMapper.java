package org.barcelonajug.superherobattlearena.adapter.out.persistence.mapper;

import org.barcelonajug.superherobattlearena.adapter.out.persistence.entity.HeroUsageEntity;
import org.barcelonajug.superherobattlearena.domain.HeroUsage;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface HeroUsageMapper {
    HeroUsage toDomain(HeroUsageEntity entity);

    HeroUsageEntity toEntity(HeroUsage domain);
}
