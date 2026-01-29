package org.barcelonajug.superherobattlearena.domain.exception;

/**
 * Exception thrown when a hero has a banned tag.
 */
public class BannedTagException extends ValidationException {

    public BannedTagException(String heroName, String tag) {
        super("Hero " + heroName + " has banned tag: " + tag);
    }
}
