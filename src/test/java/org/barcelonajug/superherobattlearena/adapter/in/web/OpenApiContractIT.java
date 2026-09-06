package org.barcelonajug.superherobattlearena.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.barcelonajug.superherobattlearena.testconfig.PostgresTestContainerConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
class OpenApiContractIT extends PostgresTestContainerConfig {

  @Autowired private MockMvc mockMvc;

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Test
  void preservesThePublishedOpenApiContract() throws Exception {
    JsonNode expected;
    try (var contract = new ClassPathResource("contracts/openapi.json").getInputStream()) {
      expected = objectMapper.readTree(contract);
    }

    var response = mockMvc.perform(get("/v3/api-docs")).andExpect(status().isOk()).andReturn();
    JsonNode actual = objectMapper.readTree(response.getResponse().getContentAsString());

    assertThat(actual).isEqualTo(expected);
  }
}
