package org.barcelonajug.superherobattlearena.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
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
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RoundController.class)
class RoundControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RoundRepository roundRepository;
    @MockitoBean
    private SubmissionRepository submissionRepository;
    @MockitoBean
    private MatchRepository matchRepository;
    @MockitoBean
    private SubmissionValidator submissionValidator;

    @Test
    void shouldReturnRoundSpec() throws Exception {
        RoundSpec spec = new RoundSpec("Test", 1, 100, null, null, null, null, "Arena");
        Round round = new Round();
        round.setRoundNo(1);
        round.setSpecJson(spec);

        given(roundRepository.findById(1)).willReturn(Optional.of(round));

        mockMvc.perform(get("/api/rounds/1"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldAcceptSubmission() throws Exception {
        RoundSpec spec = new RoundSpec("Test", 1, 100, null, null, null, null, "Arena");
        Round round = new Round();
        round.setRoundNo(1);
        round.setSpecJson(spec);

        given(roundRepository.findById(1)).willReturn(Optional.of(round));

        DraftSubmission draft = new DraftSubmission(List.of(1), "Attack");

        mockMvc.perform(post("/api/rounds/1/submit")
                .param("teamId", UUID.randomUUID().toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(draft)))
                .andExpect(status().isOk());

        Mockito.verify(submissionValidator).validate(any(), any());
    }
}
