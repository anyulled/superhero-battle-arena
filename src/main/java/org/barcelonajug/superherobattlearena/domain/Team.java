package org.barcelonajug.superherobattlearena.domain;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Represents a team participated in the arena.
 */
public record Team(
        UUID teamId,
        UUID sessionId,
        String name,
        OffsetDateTime createdAt,
        List<String> members) {
    /**
     * Constructs a new Team.
     *
     * @param teamId    the team ID.
     * @param sessionId the session ID.
     * @param name      the team name.
     * @param createdAt the creation timestamp.
     * @param members   the list of hero IDs.
     */
    public Team {
        if (members == null || members.size() < 2) {
            throw new IllegalArgumentException("A team must have at least two members.");
        }
    }
}
