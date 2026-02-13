package org.barcelonajug.superherobattlearena.application.port.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.barcelonajug.superherobattlearena.domain.Match;
import org.barcelonajug.superherobattlearena.domain.MatchStatus;

public interface MatchRepositoryPort {
  Match save(Match match);

  List<Match> saveAll(List<Match> matches);

  Optional<Match> findById(UUID matchId);

  List<Match> findByStatus(MatchStatus status);

  List<Match> findAll();

  List<Match> findByRoundNoAndSessionId(Integer roundNo, UUID sessionId);

  List<Match> findPendingMatches(
      Integer roundNo, @org.jspecify.annotations.Nullable UUID sessionId);
}
