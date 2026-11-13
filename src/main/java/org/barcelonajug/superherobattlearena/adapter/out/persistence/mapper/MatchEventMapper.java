package org.barcelonajug.superherobattlearena.adapter.out.persistence.mapper;

import org.barcelonajug.superherobattlearena.adapter.out.persistence.entity.MatchEventEntity;
import org.barcelonajug.superherobattlearena.domain.MatchEvent;
import org.jspecify.annotations.Nullable;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MatchEventMapper {
  @Nullable MatchEvent toDomain(MatchEventEntity entity);

  @Nullable MatchEventEntity toEntity(MatchEvent domain);
}
