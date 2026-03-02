package org.barcelonajug.superherobattlearena.adapter.in.web;

import static org.hamcrest.Matchers.lessThan;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.code_intelligence.jazzer.api.FuzzedDataProvider;
import com.code_intelligence.jazzer.junit.FuzzTest;
import java.util.UUID;
import org.barcelonajug.superherobattlearena.testconfig.PostgresTestContainerConfig;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
@Tag("fuzz")
class DraftSubmissionFuzzTest extends PostgresTestContainerConfig {

  @Autowired private MockMvc mockMvc;

  @FuzzTest
  void fuzzDraftSubmissionEndpoint(FuzzedDataProvider data) throws Exception {
    String body = data.consumeRemainingAsString();

    mockMvc
        .perform(
            post("/api/rounds/1/submit")
                .param("teamId", UUID.randomUUID().toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
        .andExpect(status().is(lessThan(500)));
  }
}
