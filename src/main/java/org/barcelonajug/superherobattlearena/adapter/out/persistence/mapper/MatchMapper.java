package org.barcelonajug.superherobattlearena.adapter.out.persistence.mapper;

import java.util.List;
import org.barcelonajug.superherobattlearena.adapter.out.persistence.entity.MatchEntity;
import org.barcelonajug.superherobattlearena.domain.Match;
import org.jspecify.annotations.Nullable;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface MatchMapper {
  @Nullable Match toDomain(MatchEntity entity);

  @Nullable MatchEntity toEntity(Match domain);

  List<Match> toDomain(List<MatchEntity> entities);

  List<MatchEntity> toEntity(List<Match> domains);
}
