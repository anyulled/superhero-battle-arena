package org.barcelonajug.superherobattlearena.domain.json;

public record RoundSpec(
        String description,
        int maxHeroes,
        String mapType
// Add other fields as needed
) {
}
