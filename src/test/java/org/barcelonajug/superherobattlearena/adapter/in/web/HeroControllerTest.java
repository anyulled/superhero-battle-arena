package org.barcelonajug.superherobattlearena.adapter.in.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.barcelonajug.superherobattlearena.application.usecase.RosterUseCase;
import org.barcelonajug.superherobattlearena.domain.Hero;
import org.barcelonajug.superherobattlearena.domain.filter.FilterCriteria;
import org.barcelonajug.superherobattlearena.domain.filter.FilterOperator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(HeroController.class)
class HeroControllerTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @MockitoBean private RosterUseCase rosterUseCase;

  @Test
  void testFilterHeroesV2() throws Exception {
    Hero hero =
        Hero.builder()
            .id(1)
            .name("Superman")
            .slug("superman")
            .powerstats(
                Hero.PowerStats.builder()
                    .durability(100)
                    .strength(100)
                    .power(100)
                    .speed(100)
                    .intelligence(100)
                    .combat(100)
                    .build())
            .role("Fighter")
            .build();

    when(rosterUseCase.filterHeroes(any())).thenReturn(List.of(hero));

    List<FilterCriteria> criteria =
        List.of(
            new FilterCriteria(
                "powerStats.strength", FilterOperator.GREATER_THAN, "90", null, null));

    mockMvc
        .perform(
            post("/api/heroes/v2/filter")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(criteria)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].name").value("Superman"));
  }
}
