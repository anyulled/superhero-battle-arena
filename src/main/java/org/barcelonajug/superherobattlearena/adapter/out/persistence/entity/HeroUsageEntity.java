package org.barcelonajug.superherobattlearena.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "hero_usage")
@IdClass(HeroUsageEntity.HeroUsageId.class)
public class HeroUsageEntity {

  @Id
  @Column(name = "team_id")
  private UUID teamId;

  @Id
  @Column(name = "hero_id")
  private Integer heroId;

  @Id
  @Column(name = "round_no")
  private Integer roundNo;

  @Column(nullable = false)
  private Integer streak;

  @Column(nullable = false)
  private BigDecimal multiplier;

  public static class HeroUsageId implements Serializable {
    private UUID teamId;
    private Integer heroId;
    private Integer roundNo;

    public HeroUsageId() {}

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      HeroUsageId that = (HeroUsageId) o;
      return Objects.equals(teamId, that.teamId)
          && Objects.equals(heroId, that.heroId)
          && Objects.equals(roundNo, that.roundNo);
    }

    @Override
    public int hashCode() {
      return Objects.hash(teamId, heroId, roundNo);
    }
  }

  public UUID getTeamId() {
    return teamId;
  }

  public void setTeamId(UUID teamId) {
    this.teamId = teamId;
  }

  public Integer getHeroId() {
    return heroId;
  }

  public void setHeroId(Integer heroId) {
    this.heroId = heroId;
  }

  public Integer getRoundNo() {
    return roundNo;
  }

  public void setRoundNo(Integer roundNo) {
    this.roundNo = roundNo;
  }

  public Integer getStreak() {
    return streak;
  }

  public void setStreak(Integer streak) {
    this.streak = streak;
  }

  public BigDecimal getMultiplier() {
    return multiplier;
  }

  public void setMultiplier(BigDecimal multiplier) {
    this.multiplier = multiplier;
  }
}
