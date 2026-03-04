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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser
class HeroControllerTest {

  @Autowired private MockMvc mockMvc;

  private final ObjectMapper objectMapper = new ObjectMapper();

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
                .with(
                    org.springframework.security.test.web.servlet.request
                        .SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(criteria)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].name").value("Superman"));
  }

  @Test
  void testFilterHeroesV2_VariousOperators() throws Exception {
    Hero hero =
        Hero.builder()
            .id(2)
            .name("Batman")
            .slug("batman")
            .powerstats(
                Hero.PowerStats.builder()
                    .durability(50)
                    .strength(50)
                    .power(50)
                    .speed(50)
                    .intelligence(100)
                    .combat(100)
                    .build())
            .role("Fighter")
            .build();

    when(rosterUseCase.filterHeroes(any())).thenReturn(List.of(hero));

    List<FilterCriteria> criteria =
        List.of(
            new FilterCriteria("name", FilterOperator.EQUALS, "Batman", null, null),
            new FilterCriteria(
                "powerStats.intelligence", FilterOperator.BETWEEN, "90", "110", null),
            new FilterCriteria("role", FilterOperator.LIKE, "Fight", null, null),
            new FilterCriteria("powerStats.strength", FilterOperator.LESS_THAN, "60", null, null),
            new FilterCriteria(
                "powerStats.speed", FilterOperator.LESS_THAN_OR_EQUALS, "50", null, null),
            new FilterCriteria(
                "powerStats.combat", FilterOperator.GREATER_THAN_OR_EQUALS, "100", null, null),
            new FilterCriteria("publisher", FilterOperator.NOT_EQUALS, "Marvel", null, null),
            new FilterCriteria(
                "alignment", FilterOperator.IN, null, null, List.of("good", "neutral")));

    mockMvc
        .perform(
            post("/api/heroes/v2/filter")
                .with(
                    org.springframework.security.test.web.servlet.request
                        .SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(criteria)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].name").value("Batman"));
  }
}
