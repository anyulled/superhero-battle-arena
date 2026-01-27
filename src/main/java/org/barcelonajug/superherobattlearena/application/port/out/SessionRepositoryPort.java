package org.barcelonajug.superherobattlearena.application.port.out;

import java.util.Optional;
import java.util.UUID;
import org.barcelonajug.superherobattlearena.domain.Session;

public interface SessionRepositoryPort {
    Session save(Session session);

    Optional<Session> findById(UUID sessionId);
}
