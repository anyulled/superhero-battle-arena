package org.barcelonajug.superherobattlearena.adapter.out.persistence.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.barcelonajug.superherobattlearena.adapter.out.persistence.entity.RoundEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface SpringDataRoundRepository extends JpaRepository<RoundEntity, UUID> {
  List<RoundEntity> findBySessionId(UUID sessionId);

  Optional<RoundEntity> findBySessionIdAndRoundNo(UUID sessionId, Integer roundNo);

  @Query("SELECT MAX(r.roundNo) FROM RoundEntity r WHERE r.sessionId = :sessionId")
  Optional<Integer> findMaxRoundNo(UUID sessionId);
}
