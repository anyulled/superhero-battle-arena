package org.barcelonajug.superherobattlearena.repository;

import java.util.List;
import java.util.UUID;
import org.barcelonajug.superherobattlearena.domain.MatchEvent;
import org.barcelonajug.superherobattlearena.domain.MatchEventId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MatchEventRepository extends JpaRepository<MatchEvent, MatchEventId> {
    List<MatchEvent> findByMatchIdOrderBySeqAsc(UUID matchId);
}
