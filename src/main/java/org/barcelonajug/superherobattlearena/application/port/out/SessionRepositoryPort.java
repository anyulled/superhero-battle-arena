package org.barcelonajug.superherobattlearena.application.port.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.barcelonajug.superherobattlearena.domain.Session;

public interface SessionRepositoryPort {
  Session save(Session session);

  Optional<Session> findByActiveTrue();

  List<Session> findAll();

  Optional<Session> findById(UUID id);

  void deleteAll();
}
