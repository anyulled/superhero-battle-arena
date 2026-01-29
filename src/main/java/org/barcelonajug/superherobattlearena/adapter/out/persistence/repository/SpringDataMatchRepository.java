package org.barcelonajug.superherobattlearena.adapter.out.persistence.repository;

import java.util.List;
import java.util.UUID;
import org.barcelonajug.superherobattlearena.adapter.out.persistence.entity.MatchEntity;
import org.barcelonajug.superherobattlearena.domain.MatchStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpringDataMatchRepository extends JpaRepository<MatchEntity, UUID> {
  List<MatchEntity> findByStatus(MatchStatus status);
}
