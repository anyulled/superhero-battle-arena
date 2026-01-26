package org.barcelonajug.superherobattlearena.adapter.out.persistence.repository;

import java.util.UUID;
import java.util.List;
import org.barcelonajug.superherobattlearena.adapter.out.persistence.entity.HeroUsageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpringDataHeroUsageRepository extends JpaRepository<HeroUsageEntity, HeroUsageEntity.HeroUsageId> {
    List<HeroUsageEntity> findByTeamIdAndRoundNo(UUID teamId, Integer roundNo);

    List<HeroUsageEntity> findByTeamId(UUID teamId);
}
