package org.barcelonajug.superherobattlearena.e2e;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.StreamSupport;
import net.datafaker.Faker;
import org.barcelonajug.superherobattlearena.adapter.in.web.dto.CreateRoundRequest;
import org.barcelonajug.superherobattlearena.adapter.in.web.dto.RoundSpecDto;
import org.barcelonajug.superherobattlearena.domain.json.RoundSpec;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

class TournamentConstrainedRoundIT extends AbstractTournamentE2eIT {

  private final Faker faker = new Faker();

  @Test
  void shouldCompleteTournamentWithRoleGenderAndRaceConstrainedRoundSpec() throws Exception {
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
    int totalMatches = battleResult.get("totalMatches").asInt();
    int successfulSimulations = battleResult.get("successfulSimulations").asInt();

    assertThat(totalMatches).isPositive();
    assertThat(successfulSimulations).isEqualTo(totalMatches);

    List<UUID> completedMatchIds = extractMatchIds(battleResult);
    assertThat(completedMatchIds).isNotEmpty();

    for (UUID matchId : completedMatchIds) {
      JsonNode match = getMatch(matchId);
      assertThat(match.get("status").asText()).isEqualTo("COMPLETED");
      assertThat(match.hasNonNull("winnerTeam")).isTrue();
    }

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

    CreateRoundRequest request = new CreateRoundRequest(sessionId, RoundSpecDto.from(spec));

    MvcResult result =
        mockMvc
            .perform(
                post("/api/admin/rounds/create")
                    .with(httpBasic(ADMIN_USER, ADMIN_PASS))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andReturn();

    return Integer.parseInt(result.getResponse().getContentAsString().trim());
  }
}
