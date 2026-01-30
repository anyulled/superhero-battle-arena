package org.barcelonajug.superherobattlearena.application.port.out;

import java.util.List;
import java.util.UUID;
import org.barcelonajug.superherobattlearena.domain.HeroUsage;

public interface HeroUsageRepositoryPort {
  HeroUsage save(HeroUsage heroUsage);

  /**
   * Saves all hero usages.
   *
   * @param heroUsages the list of hero usages to save
   */
  void saveAll(List<HeroUsage> heroUsages);

  List<HeroUsage> findByTeamIdAndRoundNo(UUID teamId, Integer roundNo);

  List<HeroUsage> findByTeamId(UUID teamId);
}
