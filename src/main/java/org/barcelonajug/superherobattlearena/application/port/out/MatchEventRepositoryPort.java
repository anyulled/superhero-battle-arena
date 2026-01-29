package org.barcelonajug.superherobattlearena.application.port.out;

import java.util.List;
import java.util.UUID;
import org.barcelonajug.superherobattlearena.domain.MatchEvent;

public interface MatchEventRepositoryPort {
  MatchEvent save(MatchEvent matchEvent);

  List<MatchEvent> findByMatchId(UUID matchId);
}
