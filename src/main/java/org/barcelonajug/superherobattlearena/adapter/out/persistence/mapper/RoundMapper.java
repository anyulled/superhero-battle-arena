package org.barcelonajug.superherobattlearena.adapter.out.persistence.mapper;

import org.barcelonajug.superherobattlearena.adapter.out.persistence.entity.RoundEntity;
import org.barcelonajug.superherobattlearena.domain.Round;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RoundMapper {
  Round toDomain(RoundEntity entity);

  RoundEntity toEntity(Round domain);
}
