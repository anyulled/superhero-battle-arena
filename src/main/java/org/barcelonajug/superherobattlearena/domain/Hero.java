package org.barcelonajug.superherobattlearena.domain;

import java.util.List;

/**
 * Represents a superhero with stats and role.
 */
public record Hero(
                int id,
                String name,
                PowerStats powerstats,
                String role,
                Integer cost,
                List<String> tags,
                Images images) {
        /**
         * Constructs a new Hero with default values if needed.
         *
         * @param id         the hero ID.
         * @param name       the hero name.
         * @param powerstats the hero power stats.
         * @param role       the hero role.
         * @param cost       the hero cost.
         * @param tags       the hero tags.
         * @param images     the hero images.
         */
        public Hero {
                if (role == null) {
                        role = "Fighter";
                }
                if (cost == null) {
                        cost = 10;
                }
                if (tags == null) {
                        tags = List.of();
                }
        }

        /**
         * Represents the power statistics of a hero.
         *
         * @param durability the durability stat.
         * @param strength   the strength stat.
         * @param power      the power stat.
         * @param speed      the speed stat.
         */
        public record PowerStats(
                        int durability,
                        int strength,
                        int power,
                        int speed) {
        }

        /**
         * Represents the images of a hero in different sizes.
         *
         * @param xs the xs image URL.
         * @param sm the sm image URL.
         * @param md the md image URL.
         * @param lg the lg image URL.
         */
        public record Images(
                        String xs,
                        String sm,
                        String md,
                        String lg) {
        }
}
