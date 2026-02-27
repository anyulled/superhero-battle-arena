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
import org.barcelonajug.superherobattlearena.domain.exception.HeroNotFoundException;
import org.barcelonajug.superherobattlearena.domain.exception.RoleCompositionException;
import org.barcelonajug.superherobattlearena.domain.exception.TeamSizeException;
import org.barcelonajug.superherobattlearena.domain.json.DraftSubmission;
import org.barcelonajug.superherobattlearena.domain.json.RoundSpec;
import org.barcelonajug.superherobattlearena.domain.mother.RoundSpecMother;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SubmissionValidatorUseCaseTest {

  private RosterUseCase rosterUseCase;
  private SubmissionValidatorUseCase validator;

  @BeforeEach
  void setUp() {
    rosterUseCase = mock(RosterUseCase.class);

    List<ValidationRule> validationRules =
        List.of(
            new CostValidationRule(),
            new BannedTagValidationRule(),
            new RoleCompositionValidationRule());

    validator = new SubmissionValidatorUseCase(rosterUseCase, validationRules);

    Hero h1 =
        Hero.builder()
            .id(1)
            .name("H1")
            .slug("h1")
            .role("Tank")
            .cost(10)
            .powerstats(Hero.PowerStats.builder().build())
            .tags(List.of("A"))
            .build();
    Hero h2 =
        Hero.builder()
            .id(2)
            .name("H2")
            .slug("h2")
            .role("Dps")
            .cost(20)
            .powerstats(Hero.PowerStats.builder().build())
            .tags(List.of("B"))
            .build();
    Hero h3 =
        Hero.builder()
            .id(3)
            .name("H3")
            .slug("h3")
            .role("Heal")
            .cost(15)
            .powerstats(Hero.PowerStats.builder().build())
            .tags(List.of("C", "Banned"))
            .build();

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
    RoundSpec spec = RoundSpecMother.aStandardRoundSpec(); // Default size is 5
    DraftSubmission submission = new DraftSubmission(List.of(1, 2), "Attack");

    assertThatThrownBy(() -> validator.validate(submission, spec))
        .isInstanceOf(TeamSizeException.class)
        .hasMessageContaining("Team size");
  }

  @Test
  void shouldFailBudgetExceeded() {
    // 25 budget, cost is 10 + 20 = 30
    RoundSpec baseSpec = RoundSpecMother.aStandardRoundSpec();
    RoundSpec spec =
        new RoundSpec(
            baseSpec.description(),
            2,
            25,
            baseSpec.requiredRoles(),
            baseSpec.maxSameRole(),
            baseSpec.bannedTags(),
            baseSpec.tagModifiers(),
            baseSpec.mapType());
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

  @Test
  void shouldFailHeroNotFound() {
    RoundSpec spec = new RoundSpec("Test", 2, 50, Map.of(), Map.of(), List.of(), Map.of(), "Basic");
    DraftSubmission submission =
        new DraftSubmission(List.of(1, 999), "Attack"); // Hero 999 does not exist

    assertThatThrownBy(() -> validator.validate(submission, spec))
        .isInstanceOf(HeroNotFoundException.class)
        .hasMessageContaining("Hero not found");
  }

  @Test
  void shouldFailTooManySameRole() {
    RoundSpec spec =
        new RoundSpec("Test", 2, 50, Map.of(), Map.of("Tank", 1), List.of(), Map.of(), "Basic");
    DraftSubmission submission = new DraftSubmission(List.of(1, 100), "Attack");

    Hero h4 =
        Hero.builder()
            .id(100)
            .name("H4")
            .slug("h4")
            .role("Tank")
            .cost(10)
            .powerstats(Hero.PowerStats.builder().build())
            .build();
    when(rosterUseCase.getHeroes(List.of(1, 100)))
        .thenReturn(
            List.of(
                Hero.builder()
                    .id(1)
                    .name("H1")
                    .slug("h1")
                    .role("Tank")
                    .cost(10)
                    .powerstats(Hero.PowerStats.builder().build())
                    .tags(List.of("A"))
                    .build(),
                h4));

    assertThatThrownBy(() -> validator.validate(submission, spec))
        .isInstanceOf(RoleCompositionException.class)
        .hasMessageContaining("Too many of role");
  }
}
