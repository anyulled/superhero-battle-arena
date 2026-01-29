package org.barcelonajug.superherobattlearena.adapter.out.persistence.mapper;

import org.barcelonajug.superherobattlearena.adapter.out.persistence.entity.SessionEntity;
import org.barcelonajug.superherobattlearena.domain.Session;
import org.springframework.stereotype.Component;

@Component
public class SessionMapper {

    public Session toDomain(SessionEntity entity) {
        if (entity == null) {
            return null;
        }
        return new Session(
                entity.getSessionId(),
                entity.getCreatedAt(),
                entity.isActive());
    }

    public SessionEntity toEntity(Session session) {
        if (session == null) {
            return null;
        }
        SessionEntity entity = new SessionEntity();
        entity.setSessionId(session.getSessionId());
        entity.setCreatedAt(session.getCreatedAt());
        entity.setActive(session.isActive());
        return entity;
    }
}
