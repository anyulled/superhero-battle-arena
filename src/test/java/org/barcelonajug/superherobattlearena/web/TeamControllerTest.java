package org.barcelonajug.superherobattlearena.web;

import java.util.UUID;
import org.barcelonajug.superherobattlearena.domain.Team;
import org.barcelonajug.superherobattlearena.repository.TeamRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TeamController.class)
class TeamControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TeamRepository teamRepository;

    @Test
    void shouldRegisterTeam() throws Exception {
        given(teamRepository.existsByName("Avengers")).willReturn(false);
        given(teamRepository.save(any(Team.class))).willAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(post("/api/teams/register")
                .param("name", "Avengers"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldRejectDuplicateName() throws Exception {
        given(teamRepository.existsByName("Avengers")).willReturn(true);

        mockMvc.perform(post("/api/teams/register")
                .param("name", "Avengers"))
                .andExpect(status().isBadRequest());
    }
}
