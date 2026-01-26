package org.barcelonajug.superherobattlearena.adapter.out.persistence.repository;

import java.util.UUID;
import java.util.List;
import org.barcelonajug.superherobattlearena.adapter.out.persistence.entity.MatchEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpringDataMatchEventRepository extends JpaRepository<MatchEventEntity, MatchEventEntity.MatchEventId> {
    List<MatchEventEntity> findByMatchIdOrderBySeqAsc(UUID matchId);
}
