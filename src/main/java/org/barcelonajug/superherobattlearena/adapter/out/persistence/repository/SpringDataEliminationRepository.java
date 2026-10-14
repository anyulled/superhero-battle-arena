package org.barcelonajug.superherobattlearena.adapter.out.persistence.repository;

import java.util.List;
import java.util.UUID;
import org.barcelonajug.superherobattlearena.adapter.out.persistence.entity.EliminationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpringDataEliminationRepository
    extends JpaRepository<EliminationEntity, EliminationEntity.EliminationId> {

  /** Returns whether an elimination exists for the session and team identifier pair. */
  boolean existsBySessionIdAndTeamId(UUID sessionId, UUID teamId);

  /** Returns all elimination entities belonging to the specified session. */
  List<EliminationEntity> findBySessionId(UUID sessionId);
}
