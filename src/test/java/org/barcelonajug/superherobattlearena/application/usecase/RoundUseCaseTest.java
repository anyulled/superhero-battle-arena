package org.barcelonajug.superherobattlearena.application.usecase;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.barcelonajug.superherobattlearena.application.port.out.RoundRepositoryPort;
import org.barcelonajug.superherobattlearena.application.port.out.SubmissionRepositoryPort;
import org.barcelonajug.superherobattlearena.application.port.out.TeamRepositoryPort;
import org.barcelonajug.superherobattlearena.domain.Round;
import org.barcelonajug.superherobattlearena.domain.RoundStatus;
import org.barcelonajug.superherobattlearena.domain.Submission;
import org.barcelonajug.superherobattlearena.domain.Team;
import org.barcelonajug.superherobattlearena.domain.json.DraftSubmission;
import org.barcelonajug.superherobattlearena.domain.json.RoundSpec;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RoundUseCaseTest {

  @Mock private RoundRepositoryPort roundRepository;
  @Mock private SubmissionRepositoryPort submissionRepository;
  @Mock private TeamRepositoryPort teamRepository;

  private RoundUseCase roundUseCase;

  private static final UUID SESSION_ID = UUID.randomUUID();
  private static final UUID TEAM_ID = UUID.randomUUID();
  private static final int ROUND_NO = 1;

  @BeforeEach
  void setUp() {
    roundUseCase = new RoundUseCase(roundRepository, submissionRepository, teamRepository);
  }

  @Nested
  class SubmitTeam {

    private Team team;

    @BeforeEach
    void arrangeTeam() {
      team =
          new Team(
              TEAM_ID,
              SESSION_ID,
              "Alpha Squad",
              OffsetDateTime.now(ZoneOffset.UTC),
              List.of("Alice", "Bob"));
      given(teamRepository.findById(TEAM_ID)).willReturn(Optional.of(team));
    }

    @Test
    void shouldThrow_whenTeamNotFound() {
      given(teamRepository.findById(TEAM_ID)).willReturn(Optional.empty());

      assertThatThrownBy(
              () ->
                  roundUseCase.submitTeam(
                      ROUND_NO, TEAM_ID, new DraftSubmission(List.of(1, 2, 3), "ATTACK")))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("Team not found");
    }

    @Test
    void shouldThrow_whenRoundNotFound() {
      given(roundRepository.findBySessionIdAndRoundNo(SESSION_ID, ROUND_NO))
          .willReturn(Optional.empty());

      assertThatThrownBy(
              () ->
                  roundUseCase.submitTeam(
                      ROUND_NO, TEAM_ID, new DraftSubmission(List.of(1, 2, 3), "ATTACK")))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("does not exist in session");
    }

    @Test
    void shouldThrow_whenTeamAlreadySubmitted() {
      Round round = roundWithSpec(3);
      given(roundRepository.findBySessionIdAndRoundNo(SESSION_ID, ROUND_NO))
          .willReturn(Optional.of(round));
      given(submissionRepository.findByTeamIdAndRoundNo(TEAM_ID, ROUND_NO))
          .willReturn(Optional.of(Submission.builder().build()));

      assertThatThrownBy(
              () ->
                  roundUseCase.submitTeam(
                      ROUND_NO, TEAM_ID, new DraftSubmission(List.of(1, 2, 3), "ATTACK")))
          .isInstanceOf(IllegalStateException.class)
          .hasMessageContaining("already submitted");
    }

    @Test
    void shouldThrow_whenHeroCountDoesNotMatchRoundSpecTeamSize() {
      Round round = roundWithSpec(3);
      given(roundRepository.findBySessionIdAndRoundNo(SESSION_ID, ROUND_NO))
          .willReturn(Optional.of(round));
      given(submissionRepository.findByTeamIdAndRoundNo(TEAM_ID, ROUND_NO))
          .willReturn(Optional.empty());

      assertThatThrownBy(
              () ->
                  roundUseCase.submitTeam(
                      ROUND_NO, TEAM_ID, new DraftSubmission(List.of(1, 2), "ATTACK")))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("exactly 3 heroes");
    }

    @Test
    void shouldAcceptSubmission_whenHeroCountMatchesRoundSpecTeamSize() {
      Round round = roundWithSpec(3);
      given(roundRepository.findBySessionIdAndRoundNo(SESSION_ID, ROUND_NO))
          .willReturn(Optional.of(round));
      given(submissionRepository.findByTeamIdAndRoundNo(TEAM_ID, ROUND_NO))
          .willReturn(Optional.empty());

      assertThatCode(
              () ->
                  roundUseCase.submitTeam(
                      ROUND_NO, TEAM_ID, new DraftSubmission(List.of(1, 2, 3), "ATTACK")))
          .doesNotThrowAnyException();

      then(submissionRepository).should().save(any(Submission.class));
    }

    @Test
    void shouldUseFiveAsDefault_whenRoundHasNoSpec() {
      Round round = roundWithNoSpec();
      given(roundRepository.findBySessionIdAndRoundNo(SESSION_ID, ROUND_NO))
          .willReturn(Optional.of(round));
      given(submissionRepository.findByTeamIdAndRoundNo(TEAM_ID, ROUND_NO))
          .willReturn(Optional.empty());

      assertThatThrownBy(
              () ->
                  roundUseCase.submitTeam(
                      ROUND_NO, TEAM_ID, new DraftSubmission(List.of(1, 2, 3), "ATTACK")))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("exactly 5 heroes");
    }
  }

  private Round roundWithSpec(int teamSize) {
    Round round = new Round();
    round.setRoundId(UUID.randomUUID());
    round.setRoundNo(ROUND_NO);
    round.setSessionId(SESSION_ID);
    round.setStatus(RoundStatus.OPEN);
    round.setSpecJson(
        new RoundSpec(
            "Test Round", teamSize, 1000, Map.of(), Map.of(), List.of(), Map.of(), "ARENA_1"));
    return round;
  }

  private Round roundWithNoSpec() {
    Round round = new Round();
    round.setRoundId(UUID.randomUUID());
    round.setRoundNo(ROUND_NO);
    round.setSessionId(SESSION_ID);
    round.setStatus(RoundStatus.OPEN);
    return round;
  }
}
