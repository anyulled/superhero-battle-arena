package org.barcelonajug.superherobattlearena.e2e;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.barcelonajug.superherobattlearena.testconfig.PostgresTestContainerConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@AutoConfigureMockMvc
class AdminRoundConstraintOptionsIT extends PostgresTestContainerConfig {

  private static final String ADMIN_USER = "admin";
  private static final String ADMIN_PASS = "test";

  @Autowired private MockMvc mockMvc;

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Test
  void shouldExposeConstraintOptionsFromHeroDataset() throws Exception {
    createSession();

    MvcResult result =
        mockMvc
            .perform(
                get("/api/admin/round-constraints/options")
                    .with(httpBasic(ADMIN_USER, ADMIN_PASS))
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();

    JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
    assertThat(response.get("roles")).isNotNull();
    assertThat(response.get("genders")).isNotNull();
    assertThat(response.get("races")).isNotNull();
    assertThat(response.get("publishers")).isNotNull();
    assertThat(response.get("alignments")).isNotNull();
    assertThat(response.get("roles").toString()).contains("Fighter");
    assertThat(response.get("roles").toString()).contains("Tank");
    assertThat(response.get("genders").toString()).contains("Male");
    assertThat(response.get("races").toString()).contains("Human");
    assertThat(response.get("publishers").toString()).contains("Marvel Comics");
    assertThat(response.get("alignments").toString()).contains("good");
  }

  private void createSession() throws Exception {
    mockMvc
        .perform(
            post("/api/admin/sessions/start")
                .with(httpBasic(ADMIN_USER, ADMIN_PASS))
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());
  }
}
