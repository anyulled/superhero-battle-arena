package org.barcelonajug.superherobattlearena.domain.exception;

/** Base exception for all validation errors in the superhero battle arena. */
public class ValidationException extends RuntimeException {

  public ValidationException(String message) {
    super(message);
  }

  public ValidationException(String message, Throwable cause) {
    super(message, cause);
  }
}
