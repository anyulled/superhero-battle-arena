package org.barcelonajug.superherobattlearena.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.barcelonajug.superherobattlearena.adapter.out.persistence.entity.RoundEntity;
import org.barcelonajug.superherobattlearena.adapter.out.persistence.mapper.RoundMapper;
import org.barcelonajug.superherobattlearena.adapter.out.persistence.repository.SpringDataRoundRepository;
import org.barcelonajug.superherobattlearena.domain.Round;
import org.barcelonajug.superherobattlearena.domain.RoundStatus;
import org.barcelonajug.superherobattlearena.domain.json.RoundSpec;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RoundPersistenceAdapterTest {

  private SpringDataRoundRepository repository;
  private RoundMapper mapper;
  private RoundPersistenceAdapter adapter;

  @BeforeEach
  void setUp() {
    repository = mock(SpringDataRoundRepository.class);
    mapper = mock(RoundMapper.class);
    adapter = new RoundPersistenceAdapter(repository, mapper);
  }

  @Test
  void findById_shouldReturnRound_whenRoundExists() {
    // Given
    Integer roundNo = 1;
    RoundEntity entity = createRoundEntity(roundNo);
    Round expectedRound = createRound(roundNo);

    when(repository.findById(roundNo)).thenReturn(Optional.of(entity));
    when(mapper.toDomain(entity)).thenReturn(expectedRound);

    // When
    Optional<Round> result = adapter.findById(roundNo);

    // Then
    assertThat(result).isPresent();
    assertThat(result.get()).isEqualTo(expectedRound);
    verify(repository).findById(roundNo);
    verify(mapper).toDomain(entity);
  }

  @Test
  void findById_shouldReturnEmpty_whenRoundDoesNotExist() {
    // Given
    Integer roundNo = 1;
    when(repository.findById(roundNo)).thenReturn(Optional.empty());

    // When
    Optional<Round> result = adapter.findById(roundNo);

    // Then
    assertThat(result).isEmpty();
    verify(repository).findById(roundNo);
  }

  @Test
  void findBySessionId_shouldReturnAllRoundsForSession() {
    // Given
    UUID sessionId = UUID.randomUUID();
    RoundEntity entity1 = createRoundEntity(1);
    RoundEntity entity2 = createRoundEntity(2);
    Round round1 = createRound(1);
    Round round2 = createRound(2);

    when(repository.findBySessionId(sessionId)).thenReturn(List.of(entity1, entity2));
    when(mapper.toDomain(entity1)).thenReturn(round1);
    when(mapper.toDomain(entity2)).thenReturn(round2);

    // When
    List<Round> result = adapter.findBySessionId(sessionId);

    // Then
    assertThat(result).hasSize(2);
    assertThat(result).contains(round1, round2);
    verify(repository).findBySessionId(sessionId);
    verify(mapper).toDomain(entity1);
    verify(mapper).toDomain(entity2);
  }

  @Test
  void findBySessionId_shouldReturnEmptyList_whenNoRoundsExist() {
    // Given
    UUID sessionId = UUID.randomUUID();
    when(repository.findBySessionId(sessionId)).thenReturn(Collections.emptyList());

    // When
    List<Round> result = adapter.findBySessionId(sessionId);

    // Then
    assertThat(result).isEmpty();
    verify(repository).findBySessionId(sessionId);
  }

  @Test
  void save_shouldPersistRound() {
    // Given
    Round round = createRound(1);
    RoundEntity entity = createRoundEntity(1);
    RoundEntity savedEntity = createRoundEntity(1);
    Round savedRound = createRound(1);

    when(mapper.toEntity(round)).thenReturn(entity);
    when(repository.save(entity)).thenReturn(savedEntity);
    when(mapper.toDomain(savedEntity)).thenReturn(savedRound);

    // When
    Round result = adapter.save(round);

    // Then
    assertThat(result).isEqualTo(savedRound);
    verify(mapper).toEntity(round);
    verify(repository).save(entity);
    verify(mapper).toDomain(savedEntity);
  }

  @Test
  void save_shouldHandleNullableFieldsCorrectly() {
    // Given: Round with minimal data
    Round round = new Round();
    round.setRoundNo(1);
    round.setSessionId(UUID.randomUUID());
    round.setStatus(RoundStatus.OPEN);
    round.setSeed(null);
    round.setSpecJson(null);

    RoundEntity entity = new RoundEntity();
    entity.setRoundNo(1);
    entity.setStatus(RoundStatus.OPEN);

    when(mapper.toEntity(round)).thenReturn(entity);
    when(repository.save(entity)).thenReturn(entity);
    when(mapper.toDomain(entity)).thenReturn(round);

    // When
    Round result = adapter.save(round);

    // Then
    assertThat(result).isEqualTo(round);
    verify(mapper).toEntity(round);
    verify(repository).save(entity);
  }

  @Test
  void findById_shouldMapCompleteRoundWithAllFields() {
    // Given: Round with all fields populated
    Integer roundNo = 1;
    UUID sessionId = UUID.randomUUID();
    Long seed = 12345L;
    RoundSpec spec =
        new RoundSpec(
            "Test Round",
            5,
            100,
            Collections.emptyMap(),
            Collections.emptyMap(),
            Collections.emptyList(),
            Collections.emptyMap(),
            "ARENA_1");

    RoundEntity entity = new RoundEntity();
    entity.setRoundNo(roundNo);
    entity.setSessionId(sessionId);
    entity.setStatus(RoundStatus.OPEN);
    entity.setSeed(seed);
    entity.setSpecJson(spec);

    Round round = new Round();
    round.setRoundNo(roundNo);
    round.setSessionId(sessionId);
    round.setStatus(RoundStatus.OPEN);
    round.setSeed(seed);
    round.setSpecJson(spec);

    when(repository.findById(roundNo)).thenReturn(Optional.of(entity));
    when(mapper.toDomain(entity)).thenReturn(round);

    // When
    Optional<Round> result = adapter.findById(roundNo);

    // Then
    assertThat(result).isPresent();
    Round resultRound = result.get();
    assertThat(resultRound.getRoundNo()).isEqualTo(roundNo);
    assertThat(resultRound.getSessionId()).isEqualTo(sessionId);
    assertThat(resultRound.getStatus()).isEqualTo(RoundStatus.OPEN);
    assertThat(resultRound.getSeed()).isEqualTo(seed);
    assertThat(resultRound.getSpecJson()).isEqualTo(spec);
  }

  @Test
  void findBySessionId_shouldHandleMultipleRoundsWithDifferentStatuses() {
    // Given: Multiple rounds with different statuses
    UUID sessionId = UUID.randomUUID();

    RoundEntity entity1 = createRoundEntity(1);
    entity1.setStatus(RoundStatus.OPEN);

    RoundEntity entity2 = createRoundEntity(2);
    entity2.setStatus(RoundStatus.CLOSED);

    RoundEntity entity3 = createRoundEntity(3);
    entity3.setStatus(RoundStatus.PROCESSED);

    Round round1 = createRound(1);
    round1.setStatus(RoundStatus.OPEN);

    Round round2 = createRound(2);
    round2.setStatus(RoundStatus.CLOSED);

    Round round3 = createRound(3);
    round3.setStatus(RoundStatus.PROCESSED);

    when(repository.findBySessionId(sessionId)).thenReturn(List.of(entity1, entity2, entity3));
    when(mapper.toDomain(entity1)).thenReturn(round1);
    when(mapper.toDomain(entity2)).thenReturn(round2);
    when(mapper.toDomain(entity3)).thenReturn(round3);

    // When
    List<Round> result = adapter.findBySessionId(sessionId);

    // Then
    assertThat(result).hasSize(3);
    assertThat(result.get(0).getStatus()).isEqualTo(RoundStatus.OPEN);
    assertThat(result.get(1).getStatus()).isEqualTo(RoundStatus.CLOSED);
    assertThat(result.get(2).getStatus()).isEqualTo(RoundStatus.PROCESSED);
  }

  @Test
  void save_shouldUpdateExistingRound() {
    // Given: Existing round being updated
    Round existingRound = createRound(1);
    existingRound.setStatus(RoundStatus.OPEN);

    Round updatedRound = createRound(1);
    updatedRound.setStatus(RoundStatus.CLOSED);

    RoundEntity entity = createRoundEntity(1);
    RoundEntity savedEntity = createRoundEntity(1);
    savedEntity.setStatus(RoundStatus.CLOSED);

    when(mapper.toEntity(updatedRound)).thenReturn(entity);
    when(repository.save(entity)).thenReturn(savedEntity);
    when(mapper.toDomain(savedEntity)).thenReturn(updatedRound);

    // When
    Round result = adapter.save(updatedRound);

    // Then
    assertThat(result.getStatus()).isEqualTo(RoundStatus.CLOSED);
    verify(repository).save(any(RoundEntity.class));
  }

  // Helper methods
  private RoundEntity createRoundEntity(Integer roundNo) {
    RoundEntity entity = new RoundEntity();
    entity.setRoundNo(roundNo);
    entity.setSessionId(UUID.randomUUID());
    entity.setStatus(RoundStatus.OPEN);
    entity.setSeed(12345L);
    entity.setSpecJson(
        new RoundSpec(
            "Test Round",
            5,
            100,
            Collections.emptyMap(),
            Collections.emptyMap(),
            Collections.emptyList(),
            Collections.emptyMap(),
            "ARENA_1"));
    return entity;
  }

  private Round createRound(Integer roundNo) {
    Round round = new Round();
    round.setRoundNo(roundNo);
    round.setSessionId(UUID.randomUUID());
    round.setStatus(RoundStatus.OPEN);
    round.setSeed(12345L);
    round.setSpecJson(
        new RoundSpec(
            "Test Round",
            5,
            100,
            Collections.emptyMap(),
            Collections.emptyMap(),
            Collections.emptyList(),
            Collections.emptyMap(),
            "ARENA_1"));
    return round;
  }
}