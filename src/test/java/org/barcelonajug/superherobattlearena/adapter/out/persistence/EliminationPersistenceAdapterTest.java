package org.barcelonajug.superherobattlearena.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.barcelonajug.superherobattlearena.adapter.out.persistence.entity.EliminationEntity;
import org.barcelonajug.superherobattlearena.adapter.out.persistence.mapper.EliminationMapper;
import org.barcelonajug.superherobattlearena.adapter.out.persistence.repository.SpringDataEliminationRepository;
import org.barcelonajug.superherobattlearena.domain.Elimination;
import org.barcelonajug.superherobattlearena.domain.EliminationReason;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EliminationPersistenceAdapterTest {

  private SpringDataEliminationRepository repository;
  private EliminationMapper mapper;
  private EliminationPersistenceAdapter adapter;

  @BeforeEach
  void setUp() {
    repository = mock(SpringDataEliminationRepository.class);
    mapper = mock(EliminationMapper.class);
    adapter = new EliminationPersistenceAdapter(repository, mapper);
  }

  @Test
  void shouldSaveElimination() {
    UUID teamId = UUID.randomUUID();
    UUID sessionId = UUID.randomUUID();
    Elimination domain =
        new Elimination(
            teamId, sessionId, 1, EliminationReason.LOST_MATCH, OffsetDateTime.now(ZoneOffset.UTC));
    EliminationEntity entity = new EliminationEntity();
    entity.setTeamId(teamId);
    entity.setSessionId(sessionId);
    entity.setRoundNo(1);
    entity.setReason(EliminationReason.LOST_MATCH);
    entity.setEliminatedAt(domain.eliminatedAt());

    when(mapper.toEntity(domain)).thenReturn(entity);
    when(repository.save(entity)).thenReturn(entity);

    adapter.save(domain);

    verify(repository).save(entity);
  }

  @Test
  void shouldSaveAllEliminations() {
    UUID teamId = UUID.randomUUID();
    UUID sessionId = UUID.randomUUID();
    Elimination domain =
        new Elimination(
            teamId,
            sessionId,
            1,
            EliminationReason.NO_SUBMISSION,
            OffsetDateTime.now(ZoneOffset.UTC));
    EliminationEntity entity = new EliminationEntity();

    when(mapper.toEntity(domain)).thenReturn(entity);

    adapter.saveAll(List.of(domain));

    verify(repository).saveAll(List.of(entity));
  }

  @Test
  void shouldCheckIfEliminated() {
    UUID teamId = UUID.randomUUID();
    UUID sessionId = UUID.randomUUID();

    when(repository.existsBySessionIdAndTeamId(sessionId, teamId)).thenReturn(true);

    boolean result = adapter.isEliminated(sessionId, teamId);

    assertThat(result).isTrue();
    verify(repository).existsBySessionIdAndTeamId(sessionId, teamId);
  }

  @Test
  void shouldFindBySessionId() {
    UUID sessionId = UUID.randomUUID();
    EliminationEntity entity = new EliminationEntity();
    Elimination domain =
        new Elimination(
            UUID.randomUUID(),
            sessionId,
            1,
            EliminationReason.LOST_MATCH,
            OffsetDateTime.now(ZoneOffset.UTC));

    when(repository.findBySessionId(sessionId)).thenReturn(List.of(entity));
    when(mapper.toDomain(entity)).thenReturn(domain);

    List<Elimination> results = adapter.findBySessionId(sessionId);

    assertThat(results).containsExactly(domain);
  }

  @Test
  void shouldDeleteAll() {
    adapter.deleteAll();

    verify(repository).deleteAll();
  }
}
