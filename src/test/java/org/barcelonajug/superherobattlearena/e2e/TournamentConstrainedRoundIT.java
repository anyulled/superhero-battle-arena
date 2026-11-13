package org.barcelonajug.superherobattlearena.e2e;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.StreamSupport;
import org.barcelonajug.superherobattlearena.domain.json.RoundSpec;
import org.junit.jupiter.api.Test;

class TournamentConstrainedRoundIT extends AbstractTournamentE2eIT {
  @Test
  void shouldCompleteTournamentWithRoleGenderAndRaceConstrainedRoundSpec() throws Exception {
    UUID sessionId = createSession();
    assertThat(sessionId).isNotNull();

    UUID teamAId = registerRandomTeam(sessionId);
    UUID teamBId = registerRandomTeam(sessionId);
    assertThat(teamAId).isNotNull();
    assertThat(teamBId).isNotNull();

    int roundNo = createConstrainedRound(sessionId);
    assertThat(roundNo).isOne();

    JsonNode roundSpec = getRoundSpec(sessionId, roundNo);
    assertThat(roundSpec.get("allowedRoles").toString()).contains("Fighter");
    assertThat(roundSpec.get("allowedGenders").toString()).contains("Male");
    assertThat(roundSpec.get("allowedRaces").toString()).contains("Human");

    submitSquad(teamAId, roundNo, List.of(10, 17, 144), "BALANCED");
    submitSquad(teamBId, roundNo, List.of(1, 6, 8), "DEFENSIVE");

    verifySubmissionExists(teamAId, roundNo);
    verifySubmissionExists(teamBId, roundNo);

    List<UUID> matchIds = autoMatchTeams(sessionId, roundNo);
    assertThat(matchIds).isNotEmpty();

    JsonNode battleResult = runAllBattles(sessionId, roundNo);
    verifyCompletedMatches(battleResult);

    JsonNode rounds = getRounds(sessionId);
    Optional<JsonNode> targetRound =
        StreamSupport.stream(rounds.spliterator(), false)
            .filter(roundNode -> roundNode.get("roundNo").asInt() == roundNo)
            .findFirst();
    assertThat(targetRound).isPresent();
    assertThat(targetRound.get().get("status").asText()).isEqualTo("CLOSED");
  }

  private int createConstrainedRound(UUID sessionId) throws Exception {
    RoundSpec spec =
        new RoundSpec(
            "Constrained Round",
            3,
            1000,
            Map.of("Fighter", 1),
            Map.of("Fighter", 3),
            List.of("FLYING"),
            Map.of("MAGIC", 1.2),
            "ARENA_2",
            List.of("Fighter"),
            List.of("Male"),
            List.of("Human"),
            emptyList(),
            emptyList());
    return createRound(sessionId, spec);
  }
}
