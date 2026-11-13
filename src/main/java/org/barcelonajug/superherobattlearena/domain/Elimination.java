package org.barcelonajug.superherobattlearena.domain;

import java.time.OffsetDateTime;
import java.util.UUID;

public record Elimination(
    UUID teamId,
    UUID sessionId,
    int roundNo,
    EliminationReason reason,
    OffsetDateTime eliminatedAt) {}
