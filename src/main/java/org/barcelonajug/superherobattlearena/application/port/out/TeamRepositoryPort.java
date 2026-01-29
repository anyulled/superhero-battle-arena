package org.barcelonajug.superherobattlearena.application.port.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.barcelonajug.superherobattlearena.domain.Team;

public interface TeamRepositoryPort {
  Team save(Team team);

  Optional<Team> findByName(String name);

  boolean existsByName(String name);

  Optional<Team> findById(UUID id);

  List<Team> findAll();

  List<Team> findBySessionId(UUID sessionId);
}
