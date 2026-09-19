package org.barcelonajug.superherobattlearena.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.barcelonajug.superherobattlearena.adapter.out.persistence.entity.HeroUsageEntity;
import org.barcelonajug.superherobattlearena.adapter.out.persistence.mapper.HeroUsageMapper;
import org.barcelonajug.superherobattlearena.adapter.out.persistence.repository.SpringDataHeroUsageRepository;
import org.barcelonajug.superherobattlearena.domain.HeroUsage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class HeroUsagePersistenceAdapterTest {

  private SpringDataHeroUsageRepository repository;
  private HeroUsageMapper mapper;
  private HeroUsagePersistenceAdapter adapter;

  @BeforeEach
  void setUp() {
    repository = mock(SpringDataHeroUsageRepository.class);
    mapper = mock(HeroUsageMapper.class);
    adapter = new HeroUsagePersistenceAdapter(repository, mapper);
  }

  @Test
  void shouldSaveHeroUsage() {
    UUID teamId = UUID.randomUUID();
    HeroUsage domain = new HeroUsage(teamId, 1, 1, 1, BigDecimal.valueOf(0.95));
    HeroUsageEntity entity = new HeroUsageEntity();
    entity.setTeamId(teamId);
    entity.setHeroId(1);
    entity.setRoundNo(1);
    entity.setStreak(1);
    entity.setMultiplier(BigDecimal.valueOf(0.95));

    when(mapper.toEntity(domain)).thenReturn(entity);
    when(repository.save(entity)).thenReturn(entity);
    when(mapper.toDomain(entity)).thenReturn(domain);

    HeroUsage result = adapter.save(domain);

    assertThat(result).isEqualTo(domain);
    verify(repository).save(entity);
  }

  @Test
  void shouldSaveAllHeroUsages() {
    UUID teamId = UUID.randomUUID();
    HeroUsage domain = new HeroUsage(teamId, 1, 1, 1, BigDecimal.valueOf(0.95));
    HeroUsageEntity entity = new HeroUsageEntity();

    when(mapper.toEntity(domain)).thenReturn(entity);

    adapter.saveAll(List.of(domain));

    @SuppressWarnings("unchecked")
    ArgumentCaptor<List<HeroUsageEntity>> captor = ArgumentCaptor.forClass(List.class);
    verify(repository).saveAll(captor.capture());
    assertThat(captor.getValue()).containsExactly(entity);
  }

  @Test
  void shouldFindByTeamIdAndRoundNo() {
    UUID teamId = UUID.randomUUID();
    HeroUsage domain = new HeroUsage(teamId, 1, 1, 1, BigDecimal.valueOf(0.95));
    HeroUsageEntity entity = new HeroUsageEntity();

    when(repository.findByTeamIdAndRoundNo(teamId, 1)).thenReturn(List.of(entity));
    when(mapper.toDomain(entity)).thenReturn(domain);

    List<HeroUsage> results = adapter.findByTeamIdAndRoundNo(teamId, 1);

    assertThat(results).containsExactly(domain);
    verify(repository).findByTeamIdAndRoundNo(teamId, 1);
  }

  @Test
  void shouldFindByTeamIdInAndRoundNo() {
    UUID teamA = UUID.randomUUID();
    UUID teamB = UUID.randomUUID();
    Collection<UUID> teamIds = List.of(teamA, teamB);
    HeroUsage domainA = new HeroUsage(teamA, 1, 1, 1, BigDecimal.valueOf(0.95));
    HeroUsage domainB = new HeroUsage(teamB, 2, 1, 1, BigDecimal.valueOf(0.95));
    HeroUsageEntity entityA = new HeroUsageEntity();
    HeroUsageEntity entityB = new HeroUsageEntity();

    when(repository.findByTeamIdInAndRoundNo(teamIds, 1)).thenReturn(List.of(entityA, entityB));
    when(mapper.toDomain(entityA)).thenReturn(domainA);
    when(mapper.toDomain(entityB)).thenReturn(domainB);

    List<HeroUsage> results = adapter.findByTeamIdInAndRoundNo(teamIds, 1);

    assertThat(results).containsExactly(domainA, domainB);
    verify(repository).findByTeamIdInAndRoundNo(teamIds, 1);
  }

  @Test
  void shouldFindByTeamId() {
    UUID teamId = UUID.randomUUID();
    HeroUsage domain = new HeroUsage(teamId, 1, 1, 1, BigDecimal.valueOf(0.95));
    HeroUsageEntity entity = new HeroUsageEntity();

    when(repository.findByTeamId(teamId)).thenReturn(List.of(entity));
    when(mapper.toDomain(entity)).thenReturn(domain);

    List<HeroUsage> results = adapter.findByTeamId(teamId);

    assertThat(results).containsExactly(domain);
    verify(repository).findByTeamId(teamId);
  }

  @Test
  void shouldDeleteAll() {
    adapter.deleteAll();

    verify(repository).deleteAll();
  }
}
