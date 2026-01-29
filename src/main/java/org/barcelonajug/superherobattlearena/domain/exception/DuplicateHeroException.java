package org.barcelonajug.superherobattlearena.domain.exception;

/** Exception thrown when duplicate heroes are detected in a team. */
public class DuplicateHeroException extends ValidationException {

  public DuplicateHeroException() {
    super("Duplicate heroes are not allowed");
  }
}
