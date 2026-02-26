package org.barcelonajug.superherobattlearena.adapter.out.persistence.mapper;

import org.barcelonajug.superherobattlearena.adapter.out.persistence.entity.SessionEntity;
import org.barcelonajug.superherobattlearena.domain.Session;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

@Component
public class SessionMapper {

  public @Nullable Session toDomain(@Nullable SessionEntity entity) {
    if (entity == null) {
      return null;
    }
    return new Session(entity.getSessionId(), entity.getCreatedAt(), entity.isActive());
  }

  public @Nullable SessionEntity toEntity(@Nullable Session session) {
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
