package org.barcelonajug.superherobattlearena.domain;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class HeroUsageId implements Serializable {
    private UUID teamId;
    private Integer heroId;
    private Integer roundNo;

    public HeroUsageId() {
    }

    public HeroUsageId(UUID teamId, Integer heroId, Integer roundNo) {
        this.teamId = teamId;
        this.heroId = heroId;
        this.roundNo = roundNo;
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

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        HeroUsageId that = (HeroUsageId) o;
        return Objects.equals(teamId, that.teamId) && Objects.equals(heroId, that.heroId)
                && Objects.equals(roundNo, that.roundNo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(teamId, heroId, roundNo);
    }
}
