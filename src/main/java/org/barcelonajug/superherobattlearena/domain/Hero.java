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
                List<String> tags,
                Images images) {
        @JsonIgnoreProperties(ignoreUnknown = true)
        public record PowerStats(
                        int hp,
                        int atk,
                        int def,
                        int spd) {
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        public record Images(
                        String xs,
                        String sm,
                        String md,
                        String lg) {
        }
}
