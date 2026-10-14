package org.barcelonajug.superherobattlearena.application.port.out;

import java.util.List;
import java.util.UUID;
import org.barcelonajug.superherobattlearena.domain.Elimination;

public interface EliminationRepositoryPort {
  void save(Elimination elimination);

  void saveAll(List<Elimination> eliminations);

  boolean isEliminated(UUID sessionId, UUID teamId);

  List<Elimination> findBySessionId(UUID sessionId);

  void deleteAll();
}
