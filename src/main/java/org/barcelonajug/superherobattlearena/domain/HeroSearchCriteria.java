package org.barcelonajug.superherobattlearena.domain;

import org.jspecify.annotations.Nullable;

public record HeroSearchCriteria(
    @Nullable String name,
    @Nullable String alignment,
    @Nullable String publisher,
    @Nullable String role,
    @Nullable String gender,
    @Nullable String race,
    @Nullable Integer minCost,
    @Nullable Integer maxCost,
    @Nullable Integer minPower,
    @Nullable Integer maxPower,
    @Nullable Integer minStrength,
    @Nullable Integer maxStrength,
    @Nullable Integer minSpeed,
    @Nullable Integer maxSpeed,
    @Nullable Integer minIntelligence,
    @Nullable Integer maxIntelligence,
    @Nullable Integer minDurability,
    @Nullable Integer maxDurability,
    @Nullable Integer minCombat,
    @Nullable Integer maxCombat,
    int page,
    int size,
    @Nullable String sortBy,
    @Nullable SortDirection sortDirection) {

  public HeroSearchCriteria {
    if (page < 0) {
      page = 0;
    }
    if (size < 1 || size > 100) {
      size = 20;
    }
    if (sortBy == null || sortBy.isBlank()) {
      sortBy = "id";
    }
    if (sortDirection == null) {
      sortDirection = SortDirection.ASC;
    }
  }

  public enum SortDirection {
    ASC,
    DESC
  }

  public boolean hasNameFilter() {
    return name != null && !name.isBlank();
  }

  public boolean hasAlignmentFilter() {
    return alignment != null && !alignment.isBlank();
  }

  public boolean hasPublisherFilter() {
    return publisher != null && !publisher.isBlank();
  }

  public boolean hasRoleFilter() {
    return role != null && !role.isBlank();
  }

  public boolean hasGenderFilter() {
    return gender != null && !gender.isBlank();
  }

  public boolean hasRaceFilter() {
    return race != null && !race.isBlank();
  }

  public boolean hasCostRange() {
    return minCost != null || maxCost != null;
  }

  public boolean hasPowerStatsFilter() {
    return minPower != null
        || maxPower != null
        || minStrength != null
        || maxStrength != null
        || minSpeed != null
        || maxSpeed != null
        || minIntelligence != null
        || maxIntelligence != null
        || minDurability != null
        || maxDurability != null
        || minCombat != null
        || maxCombat != null;
  }
}
