package org.barcelonajug.superherobattlearena.e2e;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import java.util.UUID;
import net.datafaker.Faker;
import org.junit.jupiter.api.Test;

class TournamentHappyPathIT extends AbstractTournamentE2eIT {

  private final Faker faker = new Faker();

  @Test
  void shouldCompleteFullTournamentHappyPath() throws Exception {
    UUID sessionId = createSession();
    assertThat(sessionId).isNotNull();

    UUID teamAId =
        registerTeam(
            faker.esports().team() + " " + UUID.randomUUID(),
            List.of(faker.name().firstName(), faker.name().firstName()),
            sessionId);
    UUID teamBId =
        registerTeam(
            faker.esports().team() + " " + UUID.randomUUID(),
            List.of(faker.name().firstName(), faker.name().firstName()),
            sessionId);
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
    int successfulSimulations = battleResult.get("successfulSimulations").asInt();
    int totalMatches = battleResult.get("totalMatches").asInt();

    assertThat(totalMatches).isPositive();
    assertThat(successfulSimulations).isEqualTo(totalMatches);

    List<UUID> completedMatchIds = extractMatchIds(battleResult);
    assertThat(completedMatchIds).isNotEmpty();

    for (UUID matchId : completedMatchIds) {
      JsonNode match = getMatch(matchId);
      assertThat(match.get("status").asText()).isEqualTo("COMPLETED");
      assertThat(match.has("winnerTeam")).isTrue();
    }

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
