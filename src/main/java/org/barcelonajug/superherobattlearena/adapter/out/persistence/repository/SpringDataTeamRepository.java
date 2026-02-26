package org.barcelonajug.superherobattlearena.adapter.out.persistence.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.barcelonajug.superherobattlearena.adapter.out.persistence.entity.TeamEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpringDataTeamRepository extends JpaRepository<TeamEntity, UUID> {
  boolean existsByName(String name);

  Optional<TeamEntity> findByName(String name);

  List<TeamEntity> findBySessionId(UUID sessionId);
}
