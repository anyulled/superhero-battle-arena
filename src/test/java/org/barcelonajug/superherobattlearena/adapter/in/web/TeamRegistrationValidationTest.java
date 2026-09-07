package org.barcelonajug.superherobattlearena.adapter.in.web;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import org.barcelonajug.superherobattlearena.application.usecase.RosterUseCase;
import org.barcelonajug.superherobattlearena.application.usecase.TeamUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class TeamRegistrationValidationTest {

  @Mock private TeamUseCase teamUseCase;
  @Mock private RosterUseCase rosterUseCase;
  private MockMvc mockMvc;

  @BeforeEach
  void configureController() {
    var controller = new TeamController(teamUseCase, rosterUseCase);
    mockMvc =
        MockMvcBuilders.standaloneSetup(controller)
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
  }

  @ParameterizedTest
  @MethodSource("registrationInputsWithNullCharacters")
  void rejectsNullCharactersBeforeCallingTheUseCase(String name, List<String> members)
      throws Exception {
    var request =
        post("/api/teams/register")
            .param("name", name)
            .param("members", members.toArray(String[]::new));

    var result = mockMvc.perform(request);

    result.andExpect(status().isBadRequest());
    verifyNoInteractions(teamUseCase, rosterUseCase);
  }

  @Test
  void acceptsUnicodeRegistrationText() throws Exception {
    String name = "Equipo héroes";
    List<String> members = List.of("Capitán", "Éclair");
    UUID teamId = UUID.fromString("00000000-0000-0000-0000-000000000001");
    when(teamUseCase.registerTeam(name, members, null)).thenReturn(teamId);
    var request =
        post("/api/teams/register")
            .param("name", name)
            .param("members", members.toArray(String[]::new));

    var result = mockMvc.perform(request);

    result.andExpect(status().isOk());
    verify(teamUseCase).registerTeam(name, members, null);
  }

  private static Stream<Arguments> registrationInputsWithNullCharacters() {
    return Stream.of(
        Arguments.of("Team\0name", List.of("Captain", "Storm")),
        Arguments.of("Team name", List.of("Cap\0tain", "Storm")));
  }
}
