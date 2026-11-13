package org.barcelonajug.superherobattlearena.e2e;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class TournamentHappyPathIT extends AbstractTournamentE2eIT {
  @Test
  void shouldCompleteFullTournamentHappyPath() throws Exception {
    UUID sessionId = createSession();
    assertThat(sessionId).isNotNull();

    UUID teamAId = registerRandomTeam(sessionId);
    UUID teamBId = registerRandomTeam(sessionId);
    assertThat(teamAId).isNotNull();
    assertThat(teamBId).isNotNull();

    int roundNo = createRound(sessionId);
    assertThat(roundNo).isOne();

    submitSquad(teamAId, roundNo, List.of(1, 3, 4, 5, 6), "AGGRESSIVE");
    submitSquad(teamBId, roundNo, List.of(7, 8, 10, 11, 12), "DEFENSIVE");

    verifySubmissionExists(teamAId, roundNo);
    verifySubmissionExists(teamBId, roundNo);

    List<UUID> matchIds = autoMatchTeams(sessionId, roundNo);
    assertThat(matchIds).isNotEmpty();

    JsonNode battleResult = runAllBattles(sessionId, roundNo);
    verifyCompletedMatches(battleResult);

    JsonNode rounds = getRounds(sessionId);
    boolean foundClosedRound = false;
    for (JsonNode roundNode : rounds) {
      if (roundNode.get("roundNo").asInt() == roundNo) {
        assertThat(roundNode.get("status").asText()).isEqualTo("CLOSED");
        foundClosedRound = true;
      }
    }
    assertThat(foundClosedRound).isTrue();
  }
}
