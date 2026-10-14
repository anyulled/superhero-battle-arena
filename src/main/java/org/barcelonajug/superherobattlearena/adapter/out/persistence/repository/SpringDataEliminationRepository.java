package org.barcelonajug.superherobattlearena.adapter.out.persistence.repository;

import java.util.List;
import java.util.UUID;
import org.barcelonajug.superherobattlearena.adapter.out.persistence.entity.EliminationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpringDataEliminationRepository
    extends JpaRepository<EliminationEntity, EliminationEntity.EliminationId> {

  boolean existsBySessionIdAndTeamId(UUID sessionId, UUID teamId);

  List<EliminationEntity> findBySessionId(UUID sessionId);
}
