package org.barcelonajug.superherobattlearena.domain.json;

import java.util.List;
import java.util.Map;

public record RoundSpec(
                String description,
                int teamSize,
                int budgetCap,
                Map<String, Integer> requiredRoles, // Role -> Min Count
                Map<String, Integer> maxSameRole, // Role -> Max Count
                List<String> bannedTags,
                String mapType) {
}
