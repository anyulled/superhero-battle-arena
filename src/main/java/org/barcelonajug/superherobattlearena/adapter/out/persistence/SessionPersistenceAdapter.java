package org.barcelonajug.superherobattlearena.adapter.out.persistence;

import java.util.Optional;
import org.barcelonajug.superherobattlearena.adapter.out.persistence.entity.SessionEntity;
import org.barcelonajug.superherobattlearena.adapter.out.persistence.mapper.SessionMapper;
import org.barcelonajug.superherobattlearena.adapter.out.persistence.repository.SessionRepository;
import org.barcelonajug.superherobattlearena.application.port.out.SessionRepositoryPort;
import org.barcelonajug.superherobattlearena.domain.Session;
import org.springframework.stereotype.Component;

@Component
public class SessionPersistenceAdapter implements SessionRepositoryPort {

  private final SessionRepository repository;
  private final SessionMapper mapper;

  public SessionPersistenceAdapter(SessionRepository repository, SessionMapper mapper) {
    this.repository = repository;
    this.mapper = mapper;
  }

  @Override
  public Session save(Session session) {
    SessionEntity entity = java.util.Objects.requireNonNull(mapper.toEntity(session));
    SessionEntity savedEntity = repository.save(entity);
    return java.util.Objects.requireNonNull(mapper.toDomain(savedEntity));
  }

  @Override
  public Optional<Session> findByActiveTrue() {
    return repository.findByActiveTrue().map(mapper::toDomain);
  }

  @Override
  public java.util.List<Session> findAll() {
    return repository.findAll().stream()
        .map(mapper::toDomain)
        .filter(java.util.Objects::nonNull)
        .map(java.util.Objects::requireNonNull)
        .toList();
  }

  @Override
  public Optional<Session> findById(java.util.UUID id) {
    return repository.findById(id).map(mapper::toDomain);
  }
}
