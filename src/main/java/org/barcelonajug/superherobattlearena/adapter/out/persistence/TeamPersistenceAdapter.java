package org.barcelonajug.superherobattlearena.adapter.out.persistence;

import java.util.Optional;
import java.util.UUID;
import org.barcelonajug.superherobattlearena.adapter.out.persistence.mapper.TeamMapper;
import org.barcelonajug.superherobattlearena.adapter.out.persistence.repository.SpringDataTeamRepository;
import org.barcelonajug.superherobattlearena.application.port.out.TeamRepositoryPort;
import org.barcelonajug.superherobattlearena.domain.Team;
import org.springframework.stereotype.Component;

@Component
public class TeamPersistenceAdapter implements TeamRepositoryPort {

  private final SpringDataTeamRepository repository;
  private final TeamMapper mapper;

  public TeamPersistenceAdapter(SpringDataTeamRepository repository, TeamMapper mapper) {
    this.repository = repository;
    this.mapper = mapper;
  }

  @Override
  public Team save(Team team) {
    return mapper.toDomain(repository.save(mapper.toEntity(team)));
  }

  @Override
  public Optional<Team> findByName(String name) {
    return repository.findByName(name).map(mapper::toDomain);
  }

  @Override
  public boolean existsByName(String name) {
    return repository.existsByName(name);
  }

  @Override
  public Optional<Team> findById(UUID id) {
    return repository.findById(id).map(mapper::toDomain);
  }

  @Override
  public java.util.List<Team> findAll() {
    return repository.findAll().stream().map(mapper::toDomain).toList();
  }

  @Override
  public java.util.List<Team> findBySessionId(UUID sessionId) {
    return repository.findBySessionId(sessionId).stream().map(mapper::toDomain).toList();
  }

  @Override
  public void deleteAll() {
    repository.deleteAll();
  }
}
