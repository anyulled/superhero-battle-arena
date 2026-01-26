package org.barcelonajug.superherobattlearena.domain;

import java.util.List;

public record Hero(
        int id,
        String name,
        PowerStats powerstats,
        String role,
        int cost,
        List<String> tags) {
    public record PowerStats(int hp, int atk, int def, int spd) {
    }
}
