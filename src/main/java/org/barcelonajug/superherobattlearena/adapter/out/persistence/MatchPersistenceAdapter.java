package org.barcelonajug.superherobattlearena.adapter.out.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.barcelonajug.superherobattlearena.adapter.out.persistence.mapper.MatchMapper;
import org.barcelonajug.superherobattlearena.adapter.out.persistence.repository.SpringDataMatchRepository;
import org.barcelonajug.superherobattlearena.application.port.out.MatchRepositoryPort;
import org.barcelonajug.superherobattlearena.domain.Match;
import org.barcelonajug.superherobattlearena.domain.MatchStatus;
import org.springframework.stereotype.Component;

@Component
public class MatchPersistenceAdapter implements MatchRepositoryPort {

  private final SpringDataMatchRepository repository;
  private final MatchMapper mapper;

  public MatchPersistenceAdapter(SpringDataMatchRepository repository, MatchMapper mapper) {
    this.repository = repository;
    this.mapper = mapper;
  }

  @Override
  public Match save(Match match) {
    return mapper.toDomain(repository.save(mapper.toEntity(match)));
  }

  @Override
  public List<Match> saveAll(List<Match> matches) {
    return mapper.toDomain(repository.saveAll(mapper.toEntity(matches)));
  }

  @Override
  public Optional<Match> findById(UUID matchId) {
    return repository.findById(matchId).map(mapper::toDomain);
  }

  @Override
  public List<Match> findByStatus(MatchStatus status) {
    return repository.findByStatus(status).stream().map(mapper::toDomain).toList();
  }

  @Override
  public List<Match> findAll() {
    return repository.findAll().stream().map(mapper::toDomain).toList();
  }

  @Override
  public List<Match> findPendingMatches(Integer roundNo, UUID sessionId) {
    if (sessionId == null) {
      return repository.findByRoundNoAndStatus(roundNo, MatchStatus.PENDING).stream()
          .map(mapper::toDomain)
          .toList();
    } else {
      return repository
          .findByRoundNoAndStatusAndSessionId(roundNo, MatchStatus.PENDING, sessionId)
          .stream()
          .map(mapper::toDomain)
          .toList();
    }
  }
}
