package org.barcelonajug.superherobattlearena.application.usecase.validation;

import java.util.List;
import java.util.Map;
import org.barcelonajug.superherobattlearena.domain.Hero;
import org.barcelonajug.superherobattlearena.domain.exception.RoundConstraintException;
import org.barcelonajug.superherobattlearena.domain.json.RoundSpec;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

@Component
public class RoundConstraintValidationRule implements ValidationRule {

  @Override
  public void validate(List<Hero> heroes, RoundSpec roundSpec) {
    validateAllowedValues(heroes, roundSpec.allowedRoles(), Hero::role, "role");
    validateAllowedValues(heroes, roundSpec.allowedGenders(), this::genderOf, "gender");
    validateAllowedValues(heroes, roundSpec.allowedRaces(), this::raceOf, "race");
    validateAllowedValues(heroes, roundSpec.allowedPublishers(), Hero::publisher, "publisher");
    validateAllowedValues(heroes, roundSpec.allowedAlignments(), Hero::alignment, "alignment");
  }

  private void validateAllowedValues(
      List<Hero> heroes,
      @Nullable List<String> allowedValues,
      ValueSelector selector,
      String fieldName) {
    if (allowedValues == null || allowedValues.isEmpty()) {
      return;
    }

    heroes.stream()
        .map(hero -> Map.entry(hero, selector.valueOf(hero)))
        .filter(entry -> entry.getValue() == null || !allowedValues.contains(entry.getValue()))
        .findFirst()
        .ifPresent(
            entry -> {
              throw new RoundConstraintException(
                  fieldName, entry.getKey().name(), entry.getValue(), allowedValues);
            });
  }

  private @Nullable String genderOf(Hero hero) {
    return hero.appearance() != null ? hero.appearance().gender() : null;
  }

  private @Nullable String raceOf(Hero hero) {
    return hero.appearance() != null ? hero.appearance().race() : null;
  }

  @FunctionalInterface
  private interface ValueSelector {
    @Nullable String valueOf(Hero hero);
  }
}
