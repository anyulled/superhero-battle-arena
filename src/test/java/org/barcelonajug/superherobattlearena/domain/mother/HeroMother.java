package org.barcelonajug.superherobattlearena.domain.mother;

import java.util.Collections;
import java.util.List;
import org.barcelonajug.superherobattlearena.domain.Hero;

public final class HeroMother {

  private HeroMother() {
    // Prevent instantiation
  }

  public static Hero aStandardHero() {
    return Hero.builder()
        .id(1)
        .name("Standard Hero")
        .slug("standard-hero")
        .role("Fighter")
        .alignment("good")
        .publisher("Marvel")
        .powerstats(
            Hero.PowerStats.builder()
                .durability(100)
                .strength(70)
                .power(80)
                .speed(60)
                .intelligence(50)
                .combat(40)
                .build())
        .tags(Collections.emptyList())
        .build();
  }

  public static Hero aWeakHero() {
    return Hero.builder()
        .id(2)
        .name("Weak Hero")
        .slug("weak-hero")
        .role("Civilian")
        .alignment("good")
        .powerstats(
            Hero.PowerStats.builder()
                .durability(10)
                .strength(5)
                .power(5)
                .speed(5)
                .intelligence(10)
                .combat(5)
                .build())
        .tags(Collections.emptyList())
        .build();
  }

  public static Hero aHeroWithRole(String role) {
    return Hero.builder()
        .id(3)
        .name(role + " Hero")
        .slug(role.toLowerCase() + "-hero")
        .role(role)
        .powerstats(
            Hero.PowerStats.builder()
                .durability(100)
                .strength(70)
                .power(80)
                .speed(60)
                .intelligence(50)
                .combat(40)
                .build())
        .tags(Collections.emptyList())
        .build();
  }

  public static Hero aHeroWithTags(List<String> tags) {
    return Hero.builder()
        .id(4)
        .name("Tagged Hero")
        .slug("tagged-hero")
        .role("Fighter")
        .powerstats(
            Hero.PowerStats.builder()
                .durability(100)
                .strength(70)
                .power(80)
                .speed(60)
                .intelligence(50)
                .combat(40)
                .build())
        .tags(tags)
        .build();
  }
}
