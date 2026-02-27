package org.barcelonajug.superherobattlearena.domain.mother;

import java.util.UUID;
import org.barcelonajug.superherobattlearena.domain.Match;
import org.barcelonajug.superherobattlearena.domain.MatchStatus;

public final class MatchMother {

  private MatchMother() {
    // Prevent instantiation
  }

  public static Match aPendingMatch(UUID sessionId, int roundNo, UUID teamA, UUID teamB) {
    return Match.builder()
        .matchId(UUID.randomUUID())
        .sessionId(sessionId)
        .roundNo(roundNo)
        .teamA(teamA)
        .teamB(teamB)
        .status(MatchStatus.PENDING)
        .build();
  }

  public static Match aCompletedMatch(
      UUID sessionId, int roundNo, UUID teamA, UUID teamB, UUID winnerTeam) {
    return Match.builder()
        .matchId(UUID.randomUUID())
        .sessionId(sessionId)
        .roundNo(roundNo)
        .teamA(teamA)
        .teamB(teamB)
        .status(MatchStatus.COMPLETED)
        .winnerTeam(winnerTeam)
        .build();
  }

  public static Match aMatch(
      UUID matchId, UUID sessionId, UUID teamA, UUID teamB, Integer roundNo, MatchStatus status) {
    return Match.builder()
        .matchId(matchId)
        .sessionId(sessionId)
        .roundNo(roundNo)
        .teamA(teamA)
        .teamB(teamB)
        .status(status)
        .build();
  }
}
