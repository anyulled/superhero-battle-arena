package org.barcelonajug.superherobattlearena.domain;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record Team(UUID teamId, UUID sessionId, String name, OffsetDateTime createdAt, List<String> members) {
    public Team {
        if (members == null || members.size() < 2) {
            throw new IllegalArgumentException("A team must have at least two members.");
        }
    }
}
