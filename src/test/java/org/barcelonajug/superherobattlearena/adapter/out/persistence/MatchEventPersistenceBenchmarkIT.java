package org.barcelonajug.superherobattlearena.adapter.out.persistence;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.barcelonajug.superherobattlearena.application.port.out.MatchEventRepositoryPort;
import org.barcelonajug.superherobattlearena.domain.MatchEvent;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles({"h2", "test"})
@Transactional
class MatchEventPersistenceBenchmarkTest {

  private static final Logger log =
      LoggerFactory.getLogger(MatchEventPersistenceBenchmarkTest.class);

  @Autowired private MatchEventRepositoryPort matchEventRepository;

  @Test
  void benchmarkSave() {
    UUID matchId = UUID.randomUUID();
    int eventCount = 1000;
    List<MatchEvent> events = new ArrayList<>();

    for (int i = 0; i < eventCount; i++) {
      org.barcelonajug.superherobattlearena.domain.json.MatchEvent eventJson =
          new org.barcelonajug.superherobattlearena.domain.json.MatchEvent(
              "ATTACK", System.currentTimeMillis(), "Hero attacks", "1", "2", 10);
      events.add(new MatchEvent(matchId, i + 1, eventJson));
    }

    long startTime = System.nanoTime();

    for (MatchEvent event : events) {
      matchEventRepository.save(event);
    }

    long endTime = System.nanoTime();
    long duration = (endTime - startTime) / 1_000_000; // ms

    log.info("Benchmark - sequential save of {} events took {} ms", eventCount, duration);
  }

  @Test
  void benchmarkSaveAll() {
    UUID matchId = UUID.randomUUID();
    int eventCount = 1000;
    List<MatchEvent> events = new ArrayList<>();

    for (int i = 0; i < eventCount; i++) {
      org.barcelonajug.superherobattlearena.domain.json.MatchEvent eventJson =
          new org.barcelonajug.superherobattlearena.domain.json.MatchEvent(
              "ATTACK", System.currentTimeMillis(), "Hero attacks", "1", "2", 10);
      events.add(new MatchEvent(matchId, i + 1, eventJson));
    }

    long startTime = System.nanoTime();

    matchEventRepository.saveAll(events);

    long endTime = System.nanoTime();
    long duration = (endTime - startTime) / 1_000_000; // ms

    log.info("Benchmark - batch saveAll of {} events took {} ms", eventCount, duration);
  }
}
