package org.barcelonajug.superherobattlearena.application.usecase;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import org.barcelonajug.superherobattlearena.application.usecase.validation.BannedTagValidationRule;
import org.barcelonajug.superherobattlearena.application.usecase.validation.CostValidationRule;
import org.barcelonajug.superherobattlearena.application.usecase.validation.RoleCompositionValidationRule;
import org.barcelonajug.superherobattlearena.application.usecase.validation.ValidationRule;
import org.barcelonajug.superherobattlearena.domain.Hero;
import org.barcelonajug.superherobattlearena.domain.exception.BannedTagException;
import org.barcelonajug.superherobattlearena.domain.exception.BudgetExceededException;
import org.barcelonajug.superherobattlearena.domain.exception.DuplicateHeroException;
import org.barcelonajug.superherobattlearena.domain.exception.RoleCompositionException;
import org.barcelonajug.superherobattlearena.domain.exception.TeamSizeException;
import org.barcelonajug.superherobattlearena.domain.json.DraftSubmission;
import org.barcelonajug.superherobattlearena.domain.json.RoundSpec;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SubmissionValidatorUseCaseTest {

  private RosterUseCase rosterUseCase;
  private SubmissionValidatorUseCase validator;

  @BeforeEach
  void setUp() {
    rosterUseCase = mock(RosterUseCase.class);

    // Create validation rules
    List<ValidationRule> validationRules =
        List.of(
            new CostValidationRule(),
            new BannedTagValidationRule(),
            new RoleCompositionValidationRule());

    validator = new SubmissionValidatorUseCase(rosterUseCase, validationRules);

    Hero h1 =
        new Hero(
            1,
            "H1",
            "h1",
            new Hero.PowerStats(0, 0, 0, 0, 0, 0),
            "Tank",
            10,
            "good",
            "Marvel",
            null,
            null,
            List.of("A"),
            new Hero.Images(null, null, null, null));
    Hero h2 =
        new Hero(
            2,
            "H2",
            "h2",
            new Hero.PowerStats(0, 0, 0, 0, 0, 0),
            "Dps",
            20,
            "bad",
            "DC",
            null,
            null,
            List.of("B"),
            new Hero.Images(null, null, null, null));
    Hero h3 =
        new Hero(
            3,
            "H3",
            "h3",
            new Hero.PowerStats(0, 0, 0, 0, 0, 0),
            "Heal",
            15,
            "neutral",
            "Image",
            null,
            null,
            List.of("C", "Banned"),
            new Hero.Images(null, null, null, null));

    List<Hero> allHeroes = List.of(h1, h2, h3);

    when(rosterUseCase.getHeroes(anyList()))
        .thenAnswer(
            invocation -> {
              List<Integer> ids = invocation.getArgument(0);
              return allHeroes.stream().filter(h -> ids.contains(h.id())).toList();
            });
  }

  @Test
  void shouldValidateValidSubmission() {
    RoundSpec spec =
        new RoundSpec("Test", 2, 50, Map.of("Tank", 1), Map.of(), List.of(), Map.of(), "Basic");
    DraftSubmission submission = new DraftSubmission(List.of(1, 2), "Attack");

    assertThatCode(() -> validator.validate(submission, spec)).doesNotThrowAnyException();
  }

  @Test
  void shouldFailWrongTeamSize() {
    RoundSpec spec = new RoundSpec("Test", 3, 50, Map.of(), Map.of(), List.of(), Map.of(), "Basic");
    DraftSubmission submission = new DraftSubmission(List.of(1, 2), "Attack");

    assertThatThrownBy(() -> validator.validate(submission, spec))
        .isInstanceOf(TeamSizeException.class)
        .hasMessageContaining("Team size");
  }

  @Test
  void shouldFailBudgetExceeded() {
    RoundSpec spec = new RoundSpec("Test", 2, 25, Map.of(), Map.of(), List.of(), Map.of(), "Basic");
    DraftSubmission submission = new DraftSubmission(List.of(1, 2), "Attack"); // Cost 10+20=30 > 25

    assertThatThrownBy(() -> validator.validate(submission, spec))
        .isInstanceOf(BudgetExceededException.class)
        .hasMessageContaining("exceeds maximum");
  }

  @Test
  void shouldFailMissingRole() {
    RoundSpec spec =
        new RoundSpec("Test", 2, 50, Map.of("Heal", 1), Map.of(), List.of(), Map.of(), "Basic");
    DraftSubmission submission =
        new DraftSubmission(List.of(1, 2), "Attack"); // Tank, Dps. Missing Heal.

    assertThatThrownBy(() -> validator.validate(submission, spec))
        .isInstanceOf(RoleCompositionException.class)
        .hasMessageContaining("Missing required role");
  }

  @Test
  void shouldFailBannedTag() {
    RoundSpec spec =
        new RoundSpec("Test", 1, 50, Map.of(), Map.of(), List.of("Banned"), Map.of(), "Basic");
    DraftSubmission submission = new DraftSubmission(List.of(3), "Attack"); // Has "Banned" tag

    assertThatThrownBy(() -> validator.validate(submission, spec))
        .isInstanceOf(BannedTagException.class)
        .hasMessageContaining("banned tag");
  }

  @Test
  void shouldFailDuplicates() {
    RoundSpec spec = new RoundSpec("Test", 2, 50, Map.of(), Map.of(), List.of(), Map.of(), "Basic");
    DraftSubmission submission = new DraftSubmission(List.of(1, 1), "Attack");

    assertThatThrownBy(() -> validator.validate(submission, spec))
        .isInstanceOf(DuplicateHeroException.class)
        .hasMessageContaining("Duplicate heroes");
  }
}
