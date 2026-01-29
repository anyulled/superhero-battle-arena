package org.barcelonajug.superherobattlearena.domain.exception;

/**
 * Exception thrown when a hero is not found.
 */
public class HeroNotFoundException extends ValidationException {

    public HeroNotFoundException(int heroId) {
        super("Hero not found: " + heroId);
    }
}
