package org.barcelonajug.superherobattlearena.domain.json;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class MatchEventSnapshotTest {

  @Test
  void matchStart_shouldCreateMatchStartEvent() {
    long timestamp = System.currentTimeMillis();

    MatchEventSnapshot snapshot = MatchEventSnapshot.matchStart(timestamp);

    assertThat(snapshot.type()).isEqualTo(MatchEventSnapshot.Type.MATCH_START);
    assertThat(snapshot.timestamp()).isEqualTo(timestamp);
    assertThat(snapshot.description()).isEqualTo("Match started");
    assertThat(snapshot.actorId()).isNull();
    assertThat(snapshot.targetId()).isNull();
    assertThat(snapshot.value()).isZero();
  }

  @Test
  void matchEnd_shouldCreateMatchEndEvent() {
    UUID winnerId = UUID.randomUUID();
    long timestamp = System.currentTimeMillis();

    MatchEventSnapshot snapshot = MatchEventSnapshot.matchEnd(winnerId, timestamp);

    assertThat(snapshot.type()).isEqualTo(MatchEventSnapshot.Type.MATCH_END);
    assertThat(snapshot.timestamp()).isEqualTo(timestamp);
    assertThat(snapshot.description()).contains(winnerId.toString());
  }

  @Test
  void draw_shouldCreateDrawEvent() {
    long timestamp = System.currentTimeMillis();

    MatchEventSnapshot snapshot = MatchEventSnapshot.draw(timestamp);

    assertThat(snapshot.type()).isEqualTo(MatchEventSnapshot.Type.MATCH_END);
    assertThat(snapshot.timestamp()).isEqualTo(timestamp);
    assertThat(snapshot.description()).contains("Draw");
  }

  @Test
  void turnStart_shouldCreateTurnStartEvent() {
    long timestamp = System.currentTimeMillis();

    MatchEventSnapshot snapshot = MatchEventSnapshot.turnStart(5, timestamp);

    assertThat(snapshot.type()).isEqualTo(MatchEventSnapshot.Type.TURN_START);
    assertThat(snapshot.value()).isEqualTo(5);
    assertThat(snapshot.description()).contains("Turn 5");
  }

  @Test
  void hit_shouldCreateHitEvent() {
    long timestamp = System.currentTimeMillis();

    MatchEventSnapshot snapshot =
        MatchEventSnapshot.hit("Superman", "Batman", "teamA_1", "teamB_2", 45, timestamp);

    assertThat(snapshot.type()).isEqualTo(MatchEventSnapshot.Type.HIT);
    assertThat(snapshot.actorId()).isEqualTo("teamA_1");
    assertThat(snapshot.targetId()).isEqualTo("teamB_2");
    assertThat(snapshot.value()).isEqualTo(45);
    assertThat(snapshot.description()).contains("Superman hits Batman for 45");
  }

  @Test
  void ko_shouldCreateKoEvent() {
    long timestamp = System.currentTimeMillis();

    MatchEventSnapshot snapshot = MatchEventSnapshot.ko("Batman", "teamA_1", "teamB_2", timestamp);

    assertThat(snapshot.type()).isEqualTo(MatchEventSnapshot.Type.KO);
    assertThat(snapshot.actorId()).isEqualTo("teamA_1");
    assertThat(snapshot.targetId()).isEqualTo("teamB_2");
    assertThat(snapshot.description()).isEqualTo("Batman is KO!");
  }

  @Test
  void dodge_shouldCreateDodgeEvent() {
    long timestamp = System.currentTimeMillis();

    MatchEventSnapshot snapshot =
        MatchEventSnapshot.dodge("Superman", "Batman", "teamA_1", "teamB_2", timestamp);

    assertThat(snapshot.type()).isEqualTo(MatchEventSnapshot.Type.DODGE);
    assertThat(snapshot.actorId()).isEqualTo("teamA_1");
    assertThat(snapshot.targetId()).isEqualTo("teamB_2");
    assertThat(snapshot.value()).isZero();
    assertThat(snapshot.description()).contains("Batman dodged an attack from Superman");
  }

  @Test
  void criticalHit_shouldCreateCriticalHitEvent() {
    long timestamp = System.currentTimeMillis();

    MatchEventSnapshot snapshot =
        MatchEventSnapshot.criticalHit("Superman", "Batman", "teamA_1", "teamB_2", 90, timestamp);

    assertThat(snapshot.type()).isEqualTo(MatchEventSnapshot.Type.CRITICAL_HIT);
    assertThat(snapshot.actorId()).isEqualTo("teamA_1");
    assertThat(snapshot.targetId()).isEqualTo("teamB_2");
    assertThat(snapshot.value()).isEqualTo(90);
    assertThat(snapshot.description()).contains("Critical Hit! Superman hits Batman for 90");
  }
}
