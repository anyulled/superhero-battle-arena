package org.barcelonajug.superherobattlearena.domain.exception;

/**
 * Exception thrown when team size validation fails.
 */
public class TeamSizeException extends ValidationException {

    public TeamSizeException(int expected, int actual) {
        super("Team size must be " + expected + " (was " + actual + ")");
    }
}
