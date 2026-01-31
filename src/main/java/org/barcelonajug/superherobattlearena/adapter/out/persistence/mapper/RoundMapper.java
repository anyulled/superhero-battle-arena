package org.barcelonajug.superherobattlearena.adapter.out.persistence.mapper;

import org.barcelonajug.superherobattlearena.adapter.out.persistence.entity.RoundEntity;
import org.barcelonajug.superherobattlearena.domain.Round;
import org.jspecify.annotations.Nullable;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RoundMapper {
  @Nullable Round toDomain(RoundEntity entity);

  @Nullable RoundEntity toEntity(Round domain);
}
