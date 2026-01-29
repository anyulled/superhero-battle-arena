package org.barcelonajug.superherobattlearena.adapter.in.web;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

import java.net.URI;
import org.barcelonajug.superherobattlearena.domain.exception.ValidationException;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

  @ExceptionHandler(ValidationException.class)
  public ProblemDetail handleValidationException(ValidationException ex) {
    ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(BAD_REQUEST, ex.getMessage());
    problemDetail.setTitle("Validation Error");
    problemDetail.setType(URI.create("https://barcelonajug.org/errors/validation-error"));
    return problemDetail;
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ProblemDetail handleIllegalArgumentException(IllegalArgumentException ex) {
    ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(BAD_REQUEST, ex.getMessage());
    problemDetail.setTitle("Invalid Argument");
    return problemDetail;
  }

  @ExceptionHandler(IllegalStateException.class)
  public ProblemDetail handleIllegalStateException(IllegalStateException ex) {
    ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(BAD_REQUEST, ex.getMessage());
    problemDetail.setTitle("Invalid State");
    return problemDetail;
  }
}
