package org.barcelonajug.superherobattlearena.application.port.out;

import java.util.Optional;
import org.barcelonajug.superherobattlearena.domain.Session;

public interface SessionRepositoryPort {
  Session save(Session session);

  Optional<Session> findByActiveTrue();

  java.util.List<Session> findAll();

  Optional<Session> findById(java.util.UUID id);
}
