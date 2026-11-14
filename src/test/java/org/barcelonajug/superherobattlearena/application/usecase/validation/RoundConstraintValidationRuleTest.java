package org.barcelonajug.superherobattlearena.application.usecase.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.util.List;
import java.util.Map;
import org.barcelonajug.superherobattlearena.domain.Hero;
import org.barcelonajug.superherobattlearena.domain.json.RoundSpec;
import org.junit.jupiter.api.Test;

class RoundConstraintValidationRuleTest {
  private final RoundConstraintValidationRule rule = new RoundConstraintValidationRule();

  @Test
  void shouldIgnoreEmptyAndNullAllowedConstraints() {
    Hero hero =
        Hero.builder()
            .id(1)
            .name("Hero")
            .slug("hero")
            .role("Fighter")
            .cost(10)
            .powerstats(Hero.PowerStats.builder().build())
            .build();

    RoundSpec roundSpec = roundSpecWithNullAllowedValues();

    assertThatCode(() -> rule.validate(List.of(hero), roundSpec)).doesNotThrowAnyException();
  }

  @Test
  void shouldReturnNullGenderWhenAppearanceIsMissing() {
    Hero hero =
        Hero.builder()
            .id(1)
            .name("Hero")
            .slug("hero")
            .role("Fighter")
            .cost(10)
            .powerstats(Hero.PowerStats.builder().build())
            .build();

    assertThat(rule.genderOf(hero)).isNull();
  }

  @Test
  void shouldReturnNullRaceWhenAppearanceIsMissing() {
    Hero hero =
        Hero.builder()
            .id(1)
            .name("Hero")
            .slug("hero")
            .role("Fighter")
            .cost(10)
            .powerstats(Hero.PowerStats.builder().build())
            .build();

    assertThat(rule.raceOf(hero)).isNull();
  }

  @SuppressWarnings("NullAway")
  private static RoundSpec roundSpecWithNullAllowedValues() {
    return new RoundSpec(
        "Round", 1, 50, Map.of(), Map.of(), List.of(), Map.of(), "Basic", null, null, null, null,
        null);
  }
}
