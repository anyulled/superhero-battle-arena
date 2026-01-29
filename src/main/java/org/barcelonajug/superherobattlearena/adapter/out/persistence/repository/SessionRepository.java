package org.barcelonajug.superherobattlearena.adapter.out.persistence.repository;

import java.util.Optional;
import java.util.UUID;
import org.barcelonajug.superherobattlearena.adapter.out.persistence.entity.SessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SessionRepository extends JpaRepository<SessionEntity, UUID> {
  Optional<SessionEntity> findByActiveTrue();
}
