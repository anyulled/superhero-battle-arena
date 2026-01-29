package org.barcelonajug.superherobattlearena.adapter.out.persistence;

import java.util.List;
import java.util.UUID;
import org.barcelonajug.superherobattlearena.adapter.out.persistence.mapper.MatchEventMapper;
import org.barcelonajug.superherobattlearena.adapter.out.persistence.repository.SpringDataMatchEventRepository;
import org.barcelonajug.superherobattlearena.application.port.out.MatchEventRepositoryPort;
import org.barcelonajug.superherobattlearena.domain.MatchEvent;
import org.springframework.stereotype.Component;

@Component
public class MatchEventPersistenceAdapter implements MatchEventRepositoryPort {

  private final SpringDataMatchEventRepository repository;
  private final MatchEventMapper mapper;

  public MatchEventPersistenceAdapter(
      SpringDataMatchEventRepository repository, MatchEventMapper mapper) {
    this.repository = repository;
    this.mapper = mapper;
  }

  @Override
  public MatchEvent save(MatchEvent matchEvent) {
    return mapper.toDomain(repository.save(mapper.toEntity(matchEvent)));
  }

  @Override
  public List<MatchEvent> findByMatchId(UUID matchId) {
    return repository.findByMatchIdOrderBySeqAsc(matchId).stream().map(mapper::toDomain).toList();
  }
}
