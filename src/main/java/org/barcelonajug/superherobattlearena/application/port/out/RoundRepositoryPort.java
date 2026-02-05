package org.barcelonajug.superherobattlearena.application.port.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.barcelonajug.superherobattlearena.domain.Round;

public interface RoundRepositoryPort {
  Optional<Round> findById(Integer roundNo);

  List<Round> findBySessionId(UUID sessionId);

  Round save(Round round);
}
