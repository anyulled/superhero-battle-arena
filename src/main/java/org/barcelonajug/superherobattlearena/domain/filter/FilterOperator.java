package org.barcelonajug.superherobattlearena.domain.filter;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Operator to use for the filter condition")
public enum FilterOperator {
  EQUALS,
  NOT_EQUALS,
  GREATER_THAN,
  LESS_THAN,
  GREATER_THAN_OR_EQUALS,
  LESS_THAN_OR_EQUALS,
  LIKE,
  IN,
  BETWEEN
}
