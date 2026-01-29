package org.barcelonajug.superherobattlearena.adapter.out.persistence.mapper;

import org.barcelonajug.superherobattlearena.adapter.out.persistence.entity.MatchEntity;
import org.barcelonajug.superherobattlearena.domain.Match;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface MatchMapper {
  Match toDomain(MatchEntity entity);

  MatchEntity toEntity(Match domain);
}
