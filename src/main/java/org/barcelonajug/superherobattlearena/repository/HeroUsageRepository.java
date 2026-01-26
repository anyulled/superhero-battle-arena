package org.barcelonajug.superherobattlearena.repository;

import org.barcelonajug.superherobattlearena.domain.HeroUsage;
import org.barcelonajug.superherobattlearena.domain.HeroUsageId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface HeroUsageRepository extends JpaRepository<HeroUsage, HeroUsageId> {

    @Query("SELECT h FROM HeroUsage h WHERE h.teamId = :teamId AND h.heroId = :heroId AND h.roundNo = :roundNo")
    Optional<HeroUsage> findByTeamIdAndHeroIdAndRoundNo(@Param("teamId") UUID teamId, @Param("heroId") Integer heroId,
            @Param("roundNo") Integer roundNo);
}
