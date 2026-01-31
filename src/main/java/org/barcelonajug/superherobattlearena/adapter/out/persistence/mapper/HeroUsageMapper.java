package org.barcelonajug.superherobattlearena.adapter.out.persistence.mapper;

import org.barcelonajug.superherobattlearena.adapter.out.persistence.entity.HeroUsageEntity;
import org.barcelonajug.superherobattlearena.domain.HeroUsage;
import org.jspecify.annotations.Nullable;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface HeroUsageMapper {
  @Nullable HeroUsage toDomain(HeroUsageEntity entity);

  @Nullable HeroUsageEntity toEntity(HeroUsage domain);
}
