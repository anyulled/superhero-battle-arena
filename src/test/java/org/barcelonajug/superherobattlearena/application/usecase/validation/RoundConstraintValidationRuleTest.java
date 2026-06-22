package org.barcelonajug.superherobattlearena.application.usecase.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
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
  void shouldReturnNullGenderWhenAppearanceIsMissing() throws Exception {
    Hero hero =
        Hero.builder()
            .id(1)
            .name("Hero")
            .slug("hero")
            .role("Fighter")
            .cost(10)
            .powerstats(Hero.PowerStats.builder().build())
            .build();

    Method method = RoundConstraintValidationRule.class.getDeclaredMethod("genderOf", Hero.class);
    method.setAccessible(true);

    assertThatCode(() -> method.invoke(rule, hero)).doesNotThrowAnyException();
    assertThat(method.invoke(rule, hero)).isNull();
  }

  @Test
  void shouldReturnNullRaceWhenAppearanceIsMissing() throws Exception {
    Hero hero =
        Hero.builder()
            .id(1)
            .name("Hero")
            .slug("hero")
            .role("Fighter")
            .cost(10)
            .powerstats(Hero.PowerStats.builder().build())
            .build();

    Method method = RoundConstraintValidationRule.class.getDeclaredMethod("raceOf", Hero.class);
    method.setAccessible(true);

    assertThatCode(() -> method.invoke(rule, hero)).doesNotThrowAnyException();
    assertThat(method.invoke(rule, hero)).isNull();
  }

  private static RoundSpec roundSpecWithNullAllowedValues() {
    try {
      Constructor<RoundSpec> constructor =
          RoundSpec.class.getDeclaredConstructor(
              String.class,
              int.class,
              int.class,
              Map.class,
              Map.class,
              List.class,
              Map.class,
              String.class,
              List.class,
              List.class,
              List.class,
              List.class,
              List.class);
      return constructor.newInstance(
          "Round", 1, 50, Map.of(), Map.of(), List.of(), Map.of(), "Basic", null, null, null, null,
          null);
    } catch (ReflectiveOperationException e) {
      throw new IllegalStateException("Failed to create round spec for coverage test", e);
    }
  }
}
