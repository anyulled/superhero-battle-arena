package org.barcelonajug.superherobattlearena.adapter.out.persistence;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.barcelonajug.superherobattlearena.adapter.out.persistence.mapper.MatchEventMapper;
import org.barcelonajug.superherobattlearena.adapter.out.persistence.repository.SpringDataMatchEventRepository;
import org.barcelonajug.superherobattlearena.application.port.out.MatchEventRepositoryPort;
import org.barcelonajug.superherobattlearena.domain.MatchEvent;
import org.springframework.stereotype.Component;

/** Persistence adapter for match events. */
@Component
public final class MatchEventPersistenceAdapter implements MatchEventRepositoryPort {

  /** The repository. */
  private final SpringDataMatchEventRepository repository;

  /** The mapper. */
  private final MatchEventMapper mapper;

  /**
   * Constructor.
   *
   * @param repo the repository
   * @param map  the mapper
   */
  public MatchEventPersistenceAdapter(
      final SpringDataMatchEventRepository repo, final MatchEventMapper map) {
    this.repository = repo;
    this.mapper = map;
  }

  @Override
  public MatchEvent save(final MatchEvent matchEvent) {
    return requireNonNull(
        mapper.toDomain(repository.save(requireNonNull(mapper.toEntity(matchEvent)))));
  }

  @Override
  public List<MatchEvent> saveAll(final List<MatchEvent> matchEvents) {
    var entities = matchEvents.stream()
        .map(mapper::toEntity)
        .filter(Objects::nonNull)
        .map(Objects::requireNonNull)
        .toList();
    return repository.saveAll(entities).stream()
        .map(mapper::toDomain)
        .filter(Objects::nonNull)
        .map(Objects::requireNonNull)
        .toList();
  }

  @Override
  public List<MatchEvent> findByMatchId(final UUID matchId) {
    var entities = repository.findByMatchIdOrderBySeqAsc(matchId);
    return entities.stream()
        .map(mapper::toDomain)
        .filter(Objects::nonNull)
        .map(Objects::requireNonNull)
        .toList();
  }

  @Override
  public void deleteAll() {
    repository.deleteAll();
  }
}
