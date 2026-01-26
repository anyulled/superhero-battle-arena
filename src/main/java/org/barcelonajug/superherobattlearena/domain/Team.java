package org.barcelonajug.superherobattlearena.domain;

import java.time.OffsetDateTime;
import java.util.UUID;

public record Team(UUID teamId, String name, OffsetDateTime createdAt) {
}
