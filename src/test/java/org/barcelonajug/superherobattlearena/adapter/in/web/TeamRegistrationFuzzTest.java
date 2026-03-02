package org.barcelonajug.superherobattlearena.adapter.in.web;

import static org.hamcrest.Matchers.lessThan;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.code_intelligence.jazzer.api.FuzzedDataProvider;
import com.code_intelligence.jazzer.junit.FuzzTest;
import java.util.stream.IntStream;
import org.barcelonajug.superherobattlearena.testconfig.PostgresTestContainerConfig;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
@Tag("fuzz")
class TeamRegistrationFuzzTest extends PostgresTestContainerConfig {

  @Autowired private MockMvc mockMvc;

  @FuzzTest
  void fuzzTeamRegistrationEndpoint(FuzzedDataProvider data) throws Exception {
    String name = data.consumeString(200);
    String[] members =
        IntStream.range(0, data.consumeInt(0, 20))
            .mapToObj(i -> data.consumeString(50))
            .toArray(String[]::new);

    mockMvc
        .perform(post("/api/teams/register").param("name", name).param("members", members))
        .andExpect(status().is(lessThan(500)));
  }
}
