package org.barcelonajug.superherobattlearena.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.barcelonajug.superherobattlearena.domain.json.DraftSubmission;
import org.barcelonajug.superherobattlearena.domain.json.RoundSpec;
import org.barcelonajug.superherobattlearena.domain.mother.MatchMother;
import org.barcelonajug.superherobattlearena.domain.mother.RoundSpecMother;
import org.junit.jupiter.api.Test;

class DomainCoverageTest {

  @Test
  void testSession() {
    UUID id = UUID.randomUUID();
    OffsetDateTime now = OffsetDateTime.now(ZoneId.systemDefault());
    Session session = new Session(id, now, true);

    assertThat(session.getSessionId()).isEqualTo(id);
    assertThat(session.getCreatedAt()).isEqualTo(now);
    assertThat(session.isActive()).isTrue();

    UUID id2 = UUID.randomUUID();
    OffsetDateTime now2 = OffsetDateTime.now(ZoneId.systemDefault());
    session.setSessionId(id2);
    session.setCreatedAt(now2);
    session.setActive(false);

    assertThat(session.getSessionId()).isEqualTo(id2);
    assertThat(session.getCreatedAt()).isEqualTo(now2);
    assertThat(session.isActive()).isFalse();
  }

  @Test
  void testRound() {
    UUID roundId = UUID.randomUUID();
    UUID sessionId = UUID.randomUUID();
    RoundSpec spec = RoundSpecMother.aStandardRoundSpec();

    Round round = new Round();
    round.setRoundId(roundId);
    round.setRoundNo(1);
    round.setSessionId(sessionId);
    round.setStatus(RoundStatus.OPEN);
    round.setSpecJson(spec);

    assertThat(round.getRoundId()).isEqualTo(roundId);
    assertThat(round.getRoundNo()).isEqualTo(1);
    assertThat(round.getSessionId()).isEqualTo(sessionId);
    assertThat(round.getStatus()).isEqualTo(RoundStatus.OPEN);
    assertThat(round.getSpecJson()).isEqualTo(spec);
  }

  @Test
  void testSubmission() {
    UUID teamId = UUID.randomUUID();
    OffsetDateTime now = OffsetDateTime.now(ZoneId.systemDefault());

    Submission.Builder builder = Submission.builder();
    DraftSubmission draft = new DraftSubmission(List.of(1, 2, 3), "ATTACK");
    Submission sub =
        builder
            .teamId(teamId)
            .roundNo(1)
            .submissionJson(draft)
            .accepted(true)
            .submittedAt(now)
            .build();

    assertThat(sub.getTeamId()).isEqualTo(teamId);
    assertThat(sub.getRoundNo()).isEqualTo(1);
    assertThat(sub.getSubmissionJson()).isEqualTo(draft);
    assertThat(sub.getAccepted()).isTrue();
    assertThat(sub.getSubmittedAt()).isEqualTo(now);

    UUID teamId2 = UUID.randomUUID();
    OffsetDateTime now2 = OffsetDateTime.now(ZoneId.systemDefault());
    DraftSubmission draft2 = new DraftSubmission(List.of(4, 5), "DEFEND");
    sub.setTeamId(teamId2);
    sub.setRoundNo(2);
    sub.setSubmissionJson(draft2);
    sub.setAccepted(false);
    sub.setSubmittedAt(now2);
    sub.setRejectedReason("Too many");

    assertThat(sub.getTeamId()).isEqualTo(teamId2);
    assertThat(sub.getRoundNo()).isEqualTo(2);
    assertThat(sub.getSubmissionJson()).isEqualTo(draft2);
    assertThat(sub.getAccepted()).isFalse();
    assertThat(sub.getSubmittedAt()).isEqualTo(now2);
    assertThat(sub.getRejectedReason()).isEqualTo("Too many");
  }

  @Test
  void testMatch() {
    UUID matchId = UUID.randomUUID();
    UUID sessionId = UUID.randomUUID();
    UUID teamA = UUID.randomUUID();
    UUID teamB = UUID.randomUUID();

    Match match = MatchMother.aMatch(matchId, sessionId, teamA, teamB, 1, MatchStatus.PENDING);
    match.setWinnerTeam(teamA);

    assertThat(match.getMatchId()).isEqualTo(matchId);
    assertThat(match.getSessionId()).isEqualTo(sessionId);
    assertThat(match.getRoundNo()).isEqualTo(1);
    assertThat(match.getTeamA()).isEqualTo(teamA);
    assertThat(match.getTeamB()).isEqualTo(teamB);
    assertThat(match.getStatus()).isEqualTo(MatchStatus.PENDING);
    assertThat(match.getWinnerTeam()).isEqualTo(teamA);
    assertThat(match.getResultJson()).isNull();

    UUID matchId2 = UUID.randomUUID();
    match.setMatchId(matchId2);
    match.setSessionId(matchId);
    match.setRoundNo(2);
    match.setTeamA(teamB);
    match.setTeamB(teamA);
    match.setStatus(MatchStatus.COMPLETED);
    match.setWinnerTeam(teamB);

    assertThat(match.getMatchId()).isEqualTo(matchId2);
    assertThat(match.getSessionId()).isEqualTo(matchId);
    assertThat(match.getRoundNo()).isEqualTo(2);
    assertThat(match.getTeamA()).isEqualTo(teamB);
    assertThat(match.getTeamB()).isEqualTo(teamA);
    assertThat(match.getStatus()).isEqualTo(MatchStatus.COMPLETED);
    assertThat(match.getWinnerTeam()).isEqualTo(teamB);
  }

  @Test
  void testHero() {
    Hero.Appearance appearance =
        Hero.Appearance.builder()
            .gender("Male")
            .race("Human")
            .heightCm(178)
            .weightKg(80)
            .eyeColor("Blue")
            .hairColor("Black")
            .build();

    Hero.Biography biography =
        Hero.Biography.builder()
            .fullName("Bruce Wayne")
            .aliases(List.of("Dark Knight"))
            .placeOfBirth("Gotham")
            .firstAppearance("Detective Comics #27")
            .build();

    Hero.Images images =
        Hero.Images.builder().xs("xs.jpg").sm("sm.jpg").md("md.jpg").lg("lg.jpg").build();

    Hero hero =
        Hero.builder()
            .id(1)
            .name("Batman")
            .slug("1-batman")
            .powerstats(Hero.PowerStats.builder().intelligence(100).build())
            .appearance(appearance)
            .biography(biography)
            .images(images)
            .role("Brawler")
            .tags(List.of("DC"))
            .cost(500)
            .build();

    assertThat(Objects.requireNonNull(hero.appearance()).gender()).isEqualTo("Male");
    assertThat(Objects.requireNonNull(hero.biography()).fullName()).isEqualTo("Bruce Wayne");
    assertThat(Objects.requireNonNull(hero.images()).lg()).isEqualTo("lg.jpg");

    // Branch test for Hero null role/cost/tags
    @SuppressWarnings("NullAway")
    Hero defaultHero =
        new Hero(
            2,
            "Default",
            "default",
            Hero.PowerStats.builder().build(),
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null);
    assertThat(defaultHero.role()).isEqualTo("Fighter");
    assertThat(defaultHero.cost()).isEqualTo(10);
    assertThat(defaultHero.tags()).isEmpty();
  }
}
