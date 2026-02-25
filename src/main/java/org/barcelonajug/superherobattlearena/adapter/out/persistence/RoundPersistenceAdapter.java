package org.barcelonajug.superherobattlearena.adapter.out.persistence;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.barcelonajug.superherobattlearena.adapter.out.persistence.mapper.RoundMapper;
import org.barcelonajug.superherobattlearena.adapter.out.persistence.repository.SpringDataRoundRepository;
import org.barcelonajug.superherobattlearena.application.port.out.RoundRepositoryPort;
import org.barcelonajug.superherobattlearena.domain.Round;
import org.springframework.stereotype.Component;

@Component
public class RoundPersistenceAdapter implements RoundRepositoryPort {

  private final SpringDataRoundRepository repository;
  private final RoundMapper mapper;

  public RoundPersistenceAdapter(SpringDataRoundRepository repository, RoundMapper mapper) {
    this.repository = repository;
    this.mapper = mapper;
  }

  @Override
  public Optional<Round> findBySessionIdAndRoundNo(UUID sessionId, Integer roundNo) {
    return repository.findBySessionIdAndRoundNo(sessionId, roundNo).map(mapper::toDomain);
  }

  @Override
  public List<Round> findBySessionId(UUID sessionId) {
    return repository.findBySessionId(sessionId).stream().map(mapper::toDomain).toList();
  }

  @Override
  public Round save(Round round) {
    return requireNonNull(mapper.toDomain(repository.save(requireNonNull(mapper.toEntity(round)))));
  }

  @Override
  public Optional<Integer> findMaxRoundNo(UUID sessionId) {
    return repository.findMaxRoundNo(sessionId);
  }

  @Override
  public void deleteAll() {
    repository.deleteAll();
  }
}
