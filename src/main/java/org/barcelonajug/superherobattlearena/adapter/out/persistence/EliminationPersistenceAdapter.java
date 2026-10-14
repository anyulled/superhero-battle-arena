package org.barcelonajug.superherobattlearena.adapter.out.persistence;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.barcelonajug.superherobattlearena.adapter.out.persistence.entity.EliminationEntity;
import org.barcelonajug.superherobattlearena.adapter.out.persistence.mapper.EliminationMapper;
import org.barcelonajug.superherobattlearena.adapter.out.persistence.repository.SpringDataEliminationRepository;
import org.barcelonajug.superherobattlearena.application.port.out.EliminationRepositoryPort;
import org.barcelonajug.superherobattlearena.domain.Elimination;
import org.springframework.stereotype.Component;

@Component
public class EliminationPersistenceAdapter implements EliminationRepositoryPort {

  private final SpringDataEliminationRepository repository;
  private final EliminationMapper mapper;

  /** Creates an elimination adapter using the persistence repository and domain mapper. */
  public EliminationPersistenceAdapter(
      SpringDataEliminationRepository repository, EliminationMapper mapper) {
    this.repository = repository;
    this.mapper = mapper;
  }

  /** Maps and saves a team elimination. */
  @Override
  public void save(Elimination elimination) {
    repository.save(Objects.requireNonNull(mapper.toEntity(elimination)));
  }

  /** Maps and saves the supplied eliminations, omitting null mapping results. */
  @Override
  public void saveAll(List<Elimination> eliminations) {
    List<EliminationEntity> entities =
        eliminations.stream()
            .map(mapper::toEntity)
            .filter(Objects::nonNull)
            .map(Objects::requireNonNull)
            .toList();
    repository.saveAll(entities);
  }

  /** Returns whether the team has an elimination record in the specified session. */
  @Override
  public boolean isEliminated(UUID sessionId, UUID teamId) {
    return repository.existsBySessionIdAndTeamId(sessionId, teamId);
  }

  /** Returns the session eliminations as domain records, omitting null mapping results. */
  @Override
  public List<Elimination> findBySessionId(UUID sessionId) {
    return repository.findBySessionId(sessionId).stream()
        .map(mapper::toDomain)
        .filter(Objects::nonNull)
        .map(Objects::requireNonNull)
        .toList();
  }

  /** Deletes all stored elimination records. */
  @Override
  public void deleteAll() {
    repository.deleteAll();
  }
}
