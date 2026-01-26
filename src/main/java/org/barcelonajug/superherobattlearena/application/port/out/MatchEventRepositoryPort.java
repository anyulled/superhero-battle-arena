package org.barcelonajug.superherobattlearena.application.port.out;

import org.barcelonajug.superherobattlearena.domain.MatchEvent;
import java.util.List;
import java.util.UUID;

public interface MatchEventRepositoryPort {
    MatchEvent save(MatchEvent matchEvent);

    List<MatchEvent> findByMatchId(UUID matchId);
}
