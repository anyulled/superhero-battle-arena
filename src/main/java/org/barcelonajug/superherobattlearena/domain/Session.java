package org.barcelonajug.superherobattlearena.domain;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Represents a tournament session.
 */
public class Session {
    private UUID sessionId;
    private OffsetDateTime createdAt;
    private boolean active;

    /**
     * Default constructor.
     */
    public Session() {
    }

    /**
     * Creates a new session with current timestamp and active status.
     *
     * @return a new Session instance.
     */
    public static Session create() {
        return new Session(UUID.randomUUID(), OffsetDateTime.now(), true);
    }

    /**
     * Constructs a new Session with the specified details.
     *
     * @param sessionId the session ID.
     * @param createdAt the creation timestamp.
     * @param active    whether the session is active.
     */
    public Session(UUID sessionId, OffsetDateTime createdAt, boolean active) {
        this.sessionId = sessionId;
        this.createdAt = createdAt;
        this.active = active;
    }

    /**
     * Gets the session ID.
     *
     * @return the session ID.
     */
    public UUID getSessionId() {
        return sessionId;
    }

    /**
     * Sets the session ID.
     *
     * @param sessionId the session ID.
     */
    public void setSessionId(UUID sessionId) {
        this.sessionId = sessionId;
    }

    /**
     * Gets the creation timestamp.
     *
     * @return the creation timestamp.
     */
    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Sets the creation timestamp.
     *
     * @param createdAt the creation timestamp.
     */
    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Checks if the session is active.
     *
     * @return true if active, false otherwise.
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Sets whether the session is active.
     *
     * @param active whether the session is active.
     */
    public void setActive(boolean active) {
        this.active = active;
    }
}
