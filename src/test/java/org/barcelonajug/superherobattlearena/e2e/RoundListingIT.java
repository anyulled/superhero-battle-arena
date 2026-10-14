package org.barcelonajug.superherobattlearena.e2e;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import java.util.UUID;
import java.util.stream.StreamSupport;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

class RoundListingIT extends AbstractTournamentE2eIT {

  @Test
  void shouldListRoundsForSession() throws Exception {
    UUID sessionId = createSession();
    assertThat(sessionId).isNotNull();

    List<Integer> rounds = listRounds(sessionId);
    assertThat(rounds).isEmpty();

    int round1 = createRound(sessionId);
    assertThat(round1).isOne();

    int round2 = createRound(sessionId);
    assertThat(round2).isEqualTo(2);

    rounds = listRounds(sessionId);
    assertThat(rounds).containsExactlyInAnyOrder(1, 2);
  }

  @Test
  void shouldReturnEmptyListForSessionWithNoRounds() throws Exception {
    UUID sessionId = createSession();

    List<Integer> rounds = listRounds(sessionId);
    assertThat(rounds).isEmpty();
  }

  @Test
  void shouldOnlyReturnRoundsForSpecificSession() throws Exception {
    UUID sessionId1 = createSession();

    createRound(sessionId1);
    createRound(sessionId1);

    UUID sessionId2 = createSession();

    createRound(sessionId2);

    List<Integer> rounds1 = listRounds(sessionId1);
    assertThat(rounds1).containsExactlyInAnyOrder(1, 2);

    List<Integer> rounds2 = listRounds(sessionId2);
    assertThat(rounds2).containsExactly(1);
  }

  private List<Integer> listRounds(UUID sessionId) throws Exception {
    MvcResult result =
        mockMvc
            .perform(get("/api/rounds").param("sessionId", sessionId.toString()))
            .andExpect(status().isOk())
            .andReturn();

    JsonNode rounds = objectMapper.readTree(result.getResponse().getContentAsString());
    return StreamSupport.stream(rounds.spliterator(), false)
        .map(r -> r.get("roundNo").asInt())
        .toList();
  }
}
