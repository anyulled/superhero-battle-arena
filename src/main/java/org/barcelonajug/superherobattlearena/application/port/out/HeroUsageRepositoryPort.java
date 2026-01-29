package org.barcelonajug.superherobattlearena.application.port.out;

import java.util.List;
import java.util.UUID;
import org.barcelonajug.superherobattlearena.domain.HeroUsage;

public interface HeroUsageRepositoryPort {
  HeroUsage save(HeroUsage heroUsage);

  List<HeroUsage> findByTeamIdAndRoundNo(UUID teamId, Integer roundNo);

  List<HeroUsage> findByTeamId(UUID teamId);
}
