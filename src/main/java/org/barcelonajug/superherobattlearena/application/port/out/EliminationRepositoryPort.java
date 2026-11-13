package org.barcelonajug.superherobattlearena.application.port.out;

import java.util.List;
import java.util.UUID;
import org.barcelonajug.superherobattlearena.domain.Elimination;

public interface EliminationRepositoryPort {
  /** Saves an elimination record for a team in a session. */
  void save(Elimination elimination);

  /** Saves the supplied elimination records. */
  void saveAll(List<Elimination> eliminations);

  /** Returns whether the team has been eliminated from the specified session. */
  boolean isEliminated(UUID sessionId, UUID teamId);

  /** Returns all elimination records for the specified session. */
  List<Elimination> findBySessionId(UUID sessionId);

  /** Deletes all elimination records. */
  void deleteAll();
}
