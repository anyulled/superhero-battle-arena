package org.barcelonajug.superherobattlearena.domain.exception;

import java.util.List;
import org.jspecify.annotations.Nullable;

public class RoundConstraintException extends ValidationException {

  public RoundConstraintException(
      String fieldName, String heroName, @Nullable String actualValue, List<String> allowedValues) {
    super(
        "Hero "
            + heroName
            + " has "
            + fieldName
            + " "
            + valueLabel(actualValue)
            + " but allowed values are "
            + allowedValues);
  }

  private static String valueLabel(@Nullable String value) {
    return value == null ? "null" : "'" + value + "'";
  }
}
