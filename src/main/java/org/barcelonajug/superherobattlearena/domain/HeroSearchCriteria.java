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

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private @Nullable String name;
    private @Nullable String alignment;
    private @Nullable String publisher;
    private @Nullable String role;
    private @Nullable String gender;
    private @Nullable String race;
    private @Nullable Integer minCost;
    private @Nullable Integer maxCost;
    private @Nullable Integer minPower;
    private @Nullable Integer maxPower;
    private @Nullable Integer minStrength;
    private @Nullable Integer maxStrength;
    private @Nullable Integer minSpeed;
    private @Nullable Integer maxSpeed;
    private @Nullable Integer minIntelligence;
    private @Nullable Integer maxIntelligence;
    private @Nullable Integer minDurability;
    private @Nullable Integer maxDurability;
    private @Nullable Integer minCombat;
    private @Nullable Integer maxCombat;
    private int page = 0;
    private int size = 20;
    private @Nullable String sortBy;
    private @Nullable SortDirection sortDirection;

    public Builder name(@Nullable String name) {
      this.name = name;
      return this;
    }

    public Builder alignment(@Nullable String alignment) {
      this.alignment = alignment;
      return this;
    }

    public Builder publisher(@Nullable String publisher) {
      this.publisher = publisher;
      return this;
    }

    public Builder role(@Nullable String role) {
      this.role = role;
      return this;
    }

    public Builder gender(@Nullable String gender) {
      this.gender = gender;
      return this;
    }

    public Builder race(@Nullable String race) {
      this.race = race;
      return this;
    }

    public Builder minCost(@Nullable Integer minCost) {
      this.minCost = minCost;
      return this;
    }

    public Builder maxCost(@Nullable Integer maxCost) {
      this.maxCost = maxCost;
      return this;
    }

    public Builder minPower(@Nullable Integer minPower) {
      this.minPower = minPower;
      return this;
    }

    public Builder maxPower(@Nullable Integer maxPower) {
      this.maxPower = maxPower;
      return this;
    }

    public Builder minStrength(@Nullable Integer minStrength) {
      this.minStrength = minStrength;
      return this;
    }

    public Builder maxStrength(@Nullable Integer maxStrength) {
      this.maxStrength = maxStrength;
      return this;
    }

    public Builder minSpeed(@Nullable Integer minSpeed) {
      this.minSpeed = minSpeed;
      return this;
    }

    public Builder maxSpeed(@Nullable Integer maxSpeed) {
      this.maxSpeed = maxSpeed;
      return this;
    }

    public Builder minIntelligence(@Nullable Integer minIntelligence) {
      this.minIntelligence = minIntelligence;
      return this;
    }

    public Builder maxIntelligence(@Nullable Integer maxIntelligence) {
      this.maxIntelligence = maxIntelligence;
      return this;
    }

    public Builder minDurability(@Nullable Integer minDurability) {
      this.minDurability = minDurability;
      return this;
    }

    public Builder maxDurability(@Nullable Integer maxDurability) {
      this.maxDurability = maxDurability;
      return this;
    }

    public Builder minCombat(@Nullable Integer minCombat) {
      this.minCombat = minCombat;
      return this;
    }

    public Builder maxCombat(@Nullable Integer maxCombat) {
      this.maxCombat = maxCombat;
      return this;
    }

    public Builder page(int page) {
      this.page = page;
      return this;
    }

    public Builder size(int size) {
      this.size = size;
      return this;
    }

    public Builder sortBy(@Nullable String sortBy) {
      this.sortBy = sortBy;
      return this;
    }

    public Builder sortDirection(@Nullable SortDirection sortDirection) {
      this.sortDirection = sortDirection;
      return this;
    }

    public HeroSearchCriteria build() {
      return new HeroSearchCriteria(
          name,
          alignment,
          publisher,
          role,
          gender,
          race,
          minCost,
          maxCost,
          minPower,
          maxPower,
          minStrength,
          maxStrength,
          minSpeed,
          maxSpeed,
          minIntelligence,
          maxIntelligence,
          minDurability,
          maxDurability,
          minCombat,
          maxCombat,
          page,
          size,
          sortBy,
          sortDirection);
    }
  }
}
