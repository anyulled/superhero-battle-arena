package org.barcelonajug.superherobattlearena.adapter.in.web.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import org.barcelonajug.superherobattlearena.domain.Hero;
import org.barcelonajug.superherobattlearena.domain.Match;
import org.barcelonajug.superherobattlearena.domain.MatchStatus;
import org.barcelonajug.superherobattlearena.domain.Round;
import org.barcelonajug.superherobattlearena.domain.RoundConstraintOptions;
import org.barcelonajug.superherobattlearena.domain.RoundStatus;
import org.barcelonajug.superherobattlearena.domain.Session;
import org.barcelonajug.superherobattlearena.domain.Submission;
import org.barcelonajug.superherobattlearena.domain.Team;
import org.barcelonajug.superherobattlearena.domain.json.DraftSubmission;
import org.barcelonajug.superherobattlearena.domain.json.MatchEventSnapshot;
import org.barcelonajug.superherobattlearena.domain.json.MatchResult;
import org.barcelonajug.superherobattlearena.domain.mother.HeroMother;
import org.barcelonajug.superherobattlearena.domain.mother.RoundSpecMother;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class WebDtoContractTest {

  private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

  @ParameterizedTest
  @MethodSource("responseMappings")
  void preservesResponseJson(Object domain, Object dto) throws Exception {
    var expected = objectMapper.readTree(objectMapper.writeValueAsString(domain));

    var actual = objectMapper.readTree(objectMapper.writeValueAsString(dto));

    assertThat(actual).isEqualTo(expected);
  }

  @Test
  void preservesDraftRequestValues() throws Exception {
    var expected = new DraftSubmission(List.of(1, 3, 4), "AGGRESSIVE");
    String json = objectMapper.writeValueAsString(expected);

    var request = objectMapper.readValue(json, DraftSubmissionDto.class);
    var actual = request.toDomain();

    assertThat(actual).isEqualTo(expected);
  }

  @Test
  void preservesRoundRequestValues() throws Exception {
    var expected = RoundSpecMother.aStandardRoundSpec();
    String json = objectMapper.writeValueAsString(expected);

    var request = objectMapper.readValue(json, RoundSpecDto.class);
    var actual = request.toDomain();

    assertThat(actual).isEqualTo(expected);
  }

  static Stream<Arguments> responseMappings() {
    UUID id = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
    var timestamp = OffsetDateTime.parse("2026-09-06T12:00:00Z");
    Hero minimalHero = HeroMother.aStandardHero();
    Hero detailedHero =
        new Hero(
            1,
            "Superman",
            "superman",
            minimalHero.powerstats(),
            "Tank",
            15,
            "good",
            "DC Comics",
            new Hero.Appearance("Male", "Kryptonian", 191, 101, "Blue", "Black"),
            new Hero.Biography("Clark Kent", "Krypton", "Action Comics #1", List.of("Kal-El")),
            List.of("flying"),
            new Hero.Images("xs", "sm", "md", "lg"));
    var team = new Team(id, id, "Avengers", timestamp, List.of("A", "B"));
    var session = new Session(id, timestamp, true);
    var result = new MatchResult("Avengers", 120, 500);
    var pendingMatch =
        Match.builder()
            .matchId(id)
            .roundNo(1)
            .teamA(id)
            .teamB(id)
            .status(MatchStatus.PENDING)
            .build();
    var completedMatch =
        Match.builder()
            .matchId(id)
            .sessionId(id)
            .roundNo(1)
            .teamA(id)
            .teamB(id)
            .winnerTeam(id)
            .status(MatchStatus.COMPLETED)
            .resultJson(result)
            .build();
    var spec = RoundSpecMother.aStandardRoundSpec();
    var draft = new DraftSubmission(List.of(1, 3, 4), "AGGRESSIVE");
    var round = new Round();
    round.setRoundId(id);
    round.setRoundNo(1);
    round.setSessionId(id);
    round.setStatus(RoundStatus.OPEN);
    round.setSpecJson(spec);
    var submission =
        Submission.builder()
            .teamId(id)
            .roundNo(1)
            .accepted(true)
            .submissionJson(draft)
            .submittedAt(timestamp)
            .build();
    var options =
        new RoundConstraintOptions(
            List.of("Tank"),
            List.of("Male"),
            List.of("Human"),
            List.of("DC Comics"),
            List.of("good"));
    var start = MatchEventSnapshot.matchStart(100);
    var hit = MatchEventSnapshot.hit("A", "B", "1", "2", 25, 101);
    return Stream.of(
        Arguments.of(minimalHero, HeroDto.from(minimalHero)),
        Arguments.of(detailedHero, HeroDto.from(detailedHero)),
        Arguments.of(team, TeamDto.from(team)),
        Arguments.of(session, SessionDto.from(session)),
        Arguments.of(result, MatchResultDto.from(result)),
        Arguments.of(pendingMatch, MatchDto.from(pendingMatch)),
        Arguments.of(completedMatch, MatchDto.from(completedMatch)),
        Arguments.of(spec, RoundSpecDto.from(spec)),
        Arguments.of(draft, DraftSubmissionDto.from(draft)),
        Arguments.of(round, RoundDto.from(round)),
        Arguments.of(submission, SubmissionDto.from(submission)),
        Arguments.of(options, RoundConstraintOptionsDto.from(options)),
        Arguments.of(start, MatchEventSnapshotDto.from(start)),
        Arguments.of(hit, MatchEventSnapshotDto.from(hit)));
  }
}
