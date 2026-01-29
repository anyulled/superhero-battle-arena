package org.barcelonajug.superherobattlearena.adapter.out.persistence.mapper;

import org.barcelonajug.superherobattlearena.adapter.out.persistence.entity.TeamEntity;
import org.barcelonajug.superherobattlearena.domain.Team;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TeamMapper {
  Team toDomain(TeamEntity entity);

  TeamEntity toEntity(Team domain);
}
