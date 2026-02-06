package org.barcelonajug.superherobattlearena.application.port.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.barcelonajug.superherobattlearena.domain.Round;

public interface RoundRepositoryPort {
  // Deprecating or removing findById(Integer) as it's ambiguous
  // Optional<Round> findById(Integer roundNo);

  Optional<Round> findBySessionIdAndRoundNo(UUID sessionId, Integer roundNo);

  List<Round> findBySessionId(UUID sessionId);

  Round save(Round round);

  Optional<Integer> findMaxRoundNo(UUID sessionId);
}
