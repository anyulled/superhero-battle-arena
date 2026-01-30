package org.barcelonajug.superherobattlearena.application.port.out;

import java.util.List;
import java.util.UUID;
import org.barcelonajug.superherobattlearena.domain.MatchEvent;

public interface MatchEventRepositoryPort {
  /**
   * Save a single match event.
   *
   * @param matchEvent the event to save
   * @return the saved event
   */
  MatchEvent save(MatchEvent matchEvent);

  /**
   * Save a list of match events.
   *
   * @param matchEvents the list of events to save
   */
  void saveAll(List<MatchEvent> matchEvents);

  /**
   * Find events by match ID.
   *
   * @param matchId the match ID
   * @return list of events
   */
  List<MatchEvent> findByMatchId(UUID matchId);
}
