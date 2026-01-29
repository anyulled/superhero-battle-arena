package org.barcelonajug.superherobattlearena.domain.exception;

/**
 * Exception thrown when required role composition is not met.
 */
public class RoleCompositionException extends ValidationException {

    public RoleCompositionException(String message) {
        super(message);
    }

    public static RoleCompositionException missingRequiredRole(String role, int required) {
        return new RoleCompositionException("Missing required role: " + role + " (required " + required + ")");
    }

    public static RoleCompositionException tooManyOfRole(String role, int max) {
        return new RoleCompositionException("Too many of role: " + role + " (max " + max + ")");
    }
}
