package org.barcelonajug.superherobattlearena.adapter.out.persistence.mapper;

import org.barcelonajug.superherobattlearena.adapter.out.persistence.entity.MatchEventEntity;
import org.barcelonajug.superherobattlearena.domain.MatchEvent;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MatchEventMapper {
    MatchEvent toDomain(MatchEventEntity entity);

    MatchEventEntity toEntity(MatchEvent domain);
}
