package org.barcelonajug.superherobattlearena.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class TeamTest {

  @Test
  void shouldCreateTeamWithTwoMembers() {
    List<String> members = List.of("Alice", "Bob");
    Team team =
        new Team(
            UUID.randomUUID(),
            UUID.randomUUID(),
            "Avengers",
            OffsetDateTime.now(ZoneId.of("UTC")),
            members);

    assertThat(team.members()).hasSize(2);
    assertThat(team.members()).containsExactly("Alice", "Bob");
  }

  @Test
  void shouldFailToCreateTeamWithOneMember() {
    List<String> members = List.of("Alice");

    assertThatThrownBy(
            () ->
                new Team(
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    "Solo",
                    OffsetDateTime.now(ZoneId.of("UTC")),
                    members))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("A team must have at least two members.");
  }

  @Test
  void shouldFailToCreateTeamWithNoMembers() {
    List<String> members = List.of();

    assertThatThrownBy(
            () ->
                new Team(
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    "Empty",
                    OffsetDateTime.now(ZoneId.of("UTC")),
                    members))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("A team must have at least two members.");
  }
}
