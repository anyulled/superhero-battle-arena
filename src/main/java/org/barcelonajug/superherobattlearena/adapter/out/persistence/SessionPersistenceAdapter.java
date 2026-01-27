package org.barcelonajug.superherobattlearena.adapter.out.persistence;

import java.util.Optional;
import java.util.UUID;
import org.barcelonajug.superherobattlearena.adapter.out.persistence.entity.SessionEntity;
import org.barcelonajug.superherobattlearena.adapter.out.persistence.repository.SpringDataSessionRepository;
import org.barcelonajug.superherobattlearena.application.port.out.SessionRepositoryPort;
import org.barcelonajug.superherobattlearena.domain.Session;
import org.springframework.stereotype.Component;

@Component
public class SessionPersistenceAdapter implements SessionRepositoryPort {

    private final SpringDataSessionRepository springDataSessionRepository;

    public SessionPersistenceAdapter(SpringDataSessionRepository springDataSessionRepository) {
        this.springDataSessionRepository = springDataSessionRepository;
    }

    @Override
    public Session save(Session session) {
        SessionEntity entity = toEntity(session);
        SessionEntity savedEntity = springDataSessionRepository.save(entity);
        return toDomain(savedEntity);
    }

    @Override
    public Optional<Session> findById(UUID sessionId) {
        return springDataSessionRepository.findById(sessionId)
                .map(this::toDomain);
    }

    private SessionEntity toEntity(Session session) {
        SessionEntity entity = new SessionEntity();
        entity.setSessionId(session.getSessionId());
        entity.setCreatedAt(session.getCreatedAt());
        entity.setActive(session.isActive());
        return entity;
    }

    private Session toDomain(SessionEntity entity) {
        return new Session(
                entity.getSessionId(),
                entity.getCreatedAt(),
                entity.isActive());
    }
}
