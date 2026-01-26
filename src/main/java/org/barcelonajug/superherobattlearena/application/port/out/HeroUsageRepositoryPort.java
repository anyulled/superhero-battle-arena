package org.barcelonajug.superherobattlearena.application.port.out;

import org.barcelonajug.superherobattlearena.domain.HeroUsage;
import java.util.UUID;
import java.util.List;

public interface HeroUsageRepositoryPort {
    HeroUsage save(HeroUsage heroUsage);

    List<HeroUsage> findByTeamIdAndRoundNo(UUID teamId, Integer roundNo);

    List<HeroUsage> findByTeamId(UUID teamId);
}
