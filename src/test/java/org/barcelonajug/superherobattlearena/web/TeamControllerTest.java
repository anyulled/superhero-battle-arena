package org.barcelonajug.superherobattlearena.web;

import java.util.UUID;
import org.barcelonajug.superherobattlearena.domain.Team;
import org.barcelonajug.superherobattlearena.repository.TeamRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TeamControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @MockitoBean
    private TeamRepository teamRepository;

    @Test
    void shouldRegisterTeam() {
        given(teamRepository.existsByName("Avengers")).willReturn(false);
        given(teamRepository.save(any(Team.class))).willAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<UUID> response = restTemplate.postForEntity("/api/teams/register?name=Avengers", null,
                UUID.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void shouldRejectDuplicateName() {
        given(teamRepository.existsByName("Avengers")).willReturn(true);

        ResponseEntity<UUID> response = restTemplate.postForEntity("/api/teams/register?name=Avengers", null,
                UUID.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
