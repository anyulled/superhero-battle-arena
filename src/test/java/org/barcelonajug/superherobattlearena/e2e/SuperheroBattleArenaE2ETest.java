package org.barcelonajug.superherobattlearena.e2e;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.barcelonajug.superherobattlearena.domain.json.DraftSubmission;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@SpringBootTest
@AutoConfigureMockMvc
class SuperheroBattleArenaE2ETest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @Test
        void completeTournamentFlow() throws Exception {
                // 1. Create Session
                String sessionResponse = mockMvc.perform(post("/api/sessions"))
                                .andExpect(status().isOk())
                                .andReturn().getResponse().getContentAsString();

                // Remove quotes if present (UUID might be returned as raw string "xxxx-...")
                // Actually Objectmapper should handle it if it's JSON string, but if controller
                // returns raw string body:
                // UUID.fromString(...) might be better if response is just the UUID string.
                // But let's stick to ObjectMapper if it's a UUID serialized as JSON string.
                UUID sessionId = objectMapper.readValue(sessionResponse, UUID.class);
                assertThat(sessionId).isNotNull();

                // 2. Register Teams
                UUID teamAId = registerTeam("Avengers", List.of("Iron Man", "Thor"), sessionId);
                UUID teamBId = registerTeam("Justice League", List.of("Superman", "Batman"), sessionId);

                // 3. Create Round
                String roundResponse = mockMvc.perform(post("/api/rounds")
                                .param("sessionId", sessionId.toString())
                                .param("roundNo", "1"))
                                .andExpect(status().isOk())
                                .andReturn().getResponse().getContentAsString();

                Integer roundNo = Integer.parseInt(roundResponse);
                assertThat(roundNo).isEqualTo(1);

                // 4. Submit Squads
                List<Integer> heroIds = List.of(1, 2, 3, 4, 5);

                submitSquad(roundNo, teamAId, new DraftSubmission(heroIds, "BALANCED"));
                submitSquad(roundNo, teamBId, new DraftSubmission(heroIds, "AGGRESSIVE"));

                // 5. Create Match
                String matchResponse = mockMvc.perform(post("/api/matches/create")
                                .param("teamA", teamAId.toString())
                                .param("teamB", teamBId.toString())
                                .param("roundNo", roundNo.toString())
                                .param("sessionId", sessionId.toString()))
                                .andExpect(status().isOk())
                                .andReturn().getResponse().getContentAsString();

                UUID matchId = objectMapper.readValue(matchResponse, UUID.class);
                assertThat(matchId).isNotNull();

                // 6. Run Match
                mockMvc.perform(post("/api/matches/" + matchId + "/run"))
                                .andExpect(status().isOk())
                                .andExpect(content().string(org.hamcrest.Matchers.containsString("Match completed")));
        }

        private UUID registerTeam(String name, List<String> members, UUID sessionId) throws Exception {
                String response = mockMvc.perform(post("/api/teams/register")
                                .param("name", name)
                                .param("sessionId", sessionId.toString())
                                .param("members", String.join(",", members)))
                                .andExpect(status().isOk())
                                .andReturn().getResponse().getContentAsString();
                return objectMapper.readValue(response, UUID.class);
        }

        private void submitSquad(Integer roundNo, UUID teamId, DraftSubmission draft) throws Exception {
                mockMvc.perform(post("/api/rounds/" + roundNo + "/submit")
                                .param("teamId", teamId.toString())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(draft)))
                                .andExpect(status().isOk());
        }
}
