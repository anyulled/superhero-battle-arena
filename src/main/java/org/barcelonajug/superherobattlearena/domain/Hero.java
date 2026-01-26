package org.barcelonajug.superherobattlearena.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Hero(
        int id,
        String name,
        PowerStats powerstats,
        String role,
        int cost,
        List<String> tags) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PowerStats(
            int hp,
            int atk,
            int def,
            int spd) {
    }
}
