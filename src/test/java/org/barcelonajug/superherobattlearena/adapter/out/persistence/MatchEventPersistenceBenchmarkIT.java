package org.barcelonajug.superherobattlearena.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.barcelonajug.superherobattlearena.domain.json.MatchEventSnapshot.hit;

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
@ActiveProfiles({ "h2", "test" })
@Transactional
class MatchEventPersistenceBenchmarkIT {

  private static final Logger log = LoggerFactory.getLogger(MatchEventPersistenceBenchmarkIT.class);

  @Autowired
  private MatchEventRepositoryPort matchEventRepository;

  @Test
  void benchmarkSave() {
    UUID matchId = UUID.randomUUID();
    int eventCount = 1000;
    List<MatchEvent> events = new ArrayList<>();

    for (int i = 0; i < eventCount; i++) {
      var eventJson = hit("Attacker", "Defender", "1", "2", 10, System.currentTimeMillis());
      events.add(new MatchEvent(matchId, i + 1, eventJson));
    }

    long startTime = System.nanoTime();

    for (MatchEvent event : events) {
      matchEventRepository.save(event);
    }

    long endTime = System.nanoTime();
    long duration = (endTime - startTime) / 1_000_000; // ms

    log.info("Benchmark - sequential save of {} events took {} ms", eventCount, duration);

    assertThat(matchEventRepository.findByMatchId(matchId)).hasSize(eventCount);
  }

  @Test
  void benchmarkSaveAll() {
    UUID matchId = UUID.randomUUID();
    int eventCount = 1000;
    List<MatchEvent> events = new ArrayList<>();

    for (int i = 0; i < eventCount; i++) {
      var eventJson = hit("Attacker", "Defender", "1", "2", 10, System.currentTimeMillis());
      events.add(new MatchEvent(matchId, i + 1, eventJson));
    }

    long startTime = System.nanoTime();

    matchEventRepository.saveAll(events);

    long endTime = System.nanoTime();
    long duration = (endTime - startTime) / 1_000_000; // ms

    log.info("Benchmark - batch saveAll of {} events took {} ms", eventCount, duration);

    assertThat(matchEventRepository.findByMatchId(matchId)).hasSize(eventCount);
  }
}
