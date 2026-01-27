package org.barcelonajug.superherobattlearena.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Hero(
                int id,
                String name,
                PowerStats powerstats,
                String role,
                Integer cost,
                List<String> tags,
                Images images) {
        public Hero {
                if (role == null)
                        role = "Fighter";
                if (cost == null)
                        cost = 10;
                if (tags == null)
                        tags = List.of();
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        public record PowerStats(
                        @com.fasterxml.jackson.annotation.JsonProperty("durability") int hp,
                        @com.fasterxml.jackson.annotation.JsonProperty("strength") int atk,
                        @com.fasterxml.jackson.annotation.JsonProperty("power") int def,
                        @com.fasterxml.jackson.annotation.JsonProperty("speed") int spd) {
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        public record Images(
                        String xs,
                        String sm,
                        String md,
                        String lg) {
        }
}
