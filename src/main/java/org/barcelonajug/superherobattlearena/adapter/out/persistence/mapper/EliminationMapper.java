package org.barcelonajug.superherobattlearena.adapter.out.persistence.mapper;

import java.util.List;
import org.barcelonajug.superherobattlearena.adapter.out.persistence.entity.EliminationEntity;
import org.barcelonajug.superherobattlearena.domain.Elimination;
import org.jspecify.annotations.Nullable;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface EliminationMapper {
  /** Maps an elimination entity to a domain record, returning null for a null source. */
  @Nullable Elimination toDomain(EliminationEntity entity);

  /** Maps an elimination record to a persistence entity, returning null for a null source. */
  @Nullable EliminationEntity toEntity(Elimination domain);

  /** Maps a list of elimination entities to domain records. */
  List<Elimination> toDomain(List<EliminationEntity> entities);

  /** Maps a list of elimination records to persistence entities. */
  List<EliminationEntity> toEntity(List<Elimination> domains);
}
