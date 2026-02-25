package org.barcelonajug.superherobattlearena.adapter.out.persistence;

import java.util.List;
import java.util.UUID;
import org.barcelonajug.superherobattlearena.adapter.out.persistence.mapper.HeroUsageMapper;
import org.barcelonajug.superherobattlearena.adapter.out.persistence.repository.SpringDataHeroUsageRepository;
import org.barcelonajug.superherobattlearena.application.port.out.HeroUsageRepositoryPort;
import org.barcelonajug.superherobattlearena.domain.HeroUsage;
import org.springframework.stereotype.Component;

@Component
public class HeroUsagePersistenceAdapter implements HeroUsageRepositoryPort {

  private final SpringDataHeroUsageRepository repository;
  private final HeroUsageMapper mapper;

  public HeroUsagePersistenceAdapter(
      SpringDataHeroUsageRepository repository, HeroUsageMapper mapper) {
    this.repository = repository;
    this.mapper = mapper;
  }

  @Override
  public HeroUsage save(HeroUsage heroUsage) {
    return java.util.Objects.requireNonNull(
        mapper.toDomain(
            repository.save(java.util.Objects.requireNonNull(mapper.toEntity(heroUsage)))));
  }

  @Override
  public void saveAll(final List<HeroUsage> heroUsages) {
    repository.saveAll(
        heroUsages.stream()
            .map(mapper::toEntity)
            .filter(java.util.Objects::nonNull)
            .map(java.util.Objects::requireNonNull)
            .toList());
  }

  @Override
  public List<HeroUsage> findByTeamIdAndRoundNo(UUID teamId, Integer roundNo) {
    return repository.findByTeamIdAndRoundNo(teamId, roundNo).stream()
        .map(mapper::toDomain)
        .filter(java.util.Objects::nonNull)
        .map(java.util.Objects::requireNonNull)
        .toList();
  }

  @Override
  public List<HeroUsage> findByTeamId(UUID teamId) {
    return repository.findByTeamId(teamId).stream()
        .map(mapper::toDomain)
        .filter(java.util.Objects::nonNull)
        .map(java.util.Objects::requireNonNull)
        .toList();
  }

  @Override
  public void deleteAll() {
    repository.deleteAll();
  }
}
