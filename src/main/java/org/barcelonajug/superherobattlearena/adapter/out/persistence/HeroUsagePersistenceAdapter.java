package org.barcelonajug.superherobattlearena.adapter.out.persistence;

import java.util.List;
import java.util.Objects;
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
    return Objects.requireNonNull(
        mapper.toDomain(
            repository.save(Objects.requireNonNull(mapper.toEntity(heroUsage)))));
  }

  @Override
  public void saveAll(final List<HeroUsage> heroUsages) {
    repository.saveAll(
        heroUsages.stream()
            .map(mapper::toEntity)
            .filter(Objects::nonNull)
            .map(Objects::requireNonNull)
            .toList());
  }

  @Override
  public List<HeroUsage> findByTeamIdAndRoundNo(UUID teamId, Integer roundNo) {
    return repository.findByTeamIdAndRoundNo(teamId, roundNo).stream()
        .map(mapper::toDomain)
        .filter(Objects::nonNull)
        .map(Objects::requireNonNull)
        .toList();
  }

  @Override
  public List<HeroUsage> findByTeamId(UUID teamId) {
    return repository.findByTeamId(teamId).stream()
        .map(mapper::toDomain)
        .filter(Objects::nonNull)
        .map(Objects::requireNonNull)
        .toList();
  }

  @Override
  public void deleteAll() {
    repository.deleteAll();
  }
}
