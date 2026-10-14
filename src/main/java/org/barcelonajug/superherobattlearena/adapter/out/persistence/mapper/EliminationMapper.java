package org.barcelonajug.superherobattlearena.adapter.out.persistence.mapper;

import java.util.List;
import org.barcelonajug.superherobattlearena.adapter.out.persistence.entity.EliminationEntity;
import org.barcelonajug.superherobattlearena.domain.Elimination;
import org.jspecify.annotations.Nullable;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface EliminationMapper {
  @Nullable Elimination toDomain(EliminationEntity entity);

  @Nullable EliminationEntity toEntity(Elimination domain);

  List<Elimination> toDomain(List<EliminationEntity> entities);

  List<EliminationEntity> toEntity(List<Elimination> domains);
}
