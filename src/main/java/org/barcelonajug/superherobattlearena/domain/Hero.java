package org.barcelonajug.superherobattlearena.domain;

import java.util.List;

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

        public record PowerStats(
                        int durability,
                        int strength,
                        int power,
                        int speed) {
        }

        public record Images(
                        String xs,
                        String sm,
                        String md,
                        String lg) {
        }
}
