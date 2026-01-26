package org.barcelonajug.superherobattlearena.web;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.barcelonajug.superherobattlearena.domain.Round;
import org.barcelonajug.superherobattlearena.domain.json.DraftSubmission;
import org.barcelonajug.superherobattlearena.domain.json.RoundSpec;
import org.barcelonajug.superherobattlearena.repository.MatchRepository;
import org.barcelonajug.superherobattlearena.repository.RoundRepository;
import org.barcelonajug.superherobattlearena.repository.SubmissionRepository;
import org.barcelonajug.superherobattlearena.service.SubmissionValidator;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
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
class RoundControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @MockitoBean
    private RoundRepository roundRepository;
    @MockitoBean
    private SubmissionRepository submissionRepository;
    @MockitoBean
    private MatchRepository matchRepository;
    @MockitoBean
    private SubmissionValidator submissionValidator;

    @Test
    void shouldReturnRoundSpec() {
        RoundSpec spec = new RoundSpec("Test", 1, 100, null, null, null, null, "Arena");
        Round round = new Round();
        round.setRoundNo(1);
        round.setSpecJson(spec);

        given(roundRepository.findById(1)).willReturn(Optional.of(round));

        ResponseEntity<RoundSpec> response = restTemplate.getForEntity("/api/rounds/1", RoundSpec.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void shouldAcceptSubmission() {
        RoundSpec spec = new RoundSpec("Test", 1, 100, null, null, null, null, "Arena");
        Round round = new Round();
        round.setRoundNo(1);
        round.setSpecJson(spec);

        given(roundRepository.findById(1)).willReturn(Optional.of(round));

        DraftSubmission draft = new DraftSubmission(List.of(1), "Attack");

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/rounds/1/submit?teamId=" + UUID.randomUUID(),
                draft,
                String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        Mockito.verify(submissionValidator).validate(any(), any());
    }
}
