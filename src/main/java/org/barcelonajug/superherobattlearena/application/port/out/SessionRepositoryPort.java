package org.barcelonajug.superherobattlearena.application.port.out;

import org.barcelonajug.superherobattlearena.domain.Session;
import java.util.Optional;

public interface SessionRepositoryPort {
    Session save(Session session);

    Optional<Session> findByActiveTrue();

    java.util.List<Session> findAll();

    Optional<Session> findById(java.util.UUID id);
}
