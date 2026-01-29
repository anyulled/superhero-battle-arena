package org.barcelonajug.superherobattlearena.application.usecase;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.barcelonajug.superherobattlearena.domain.Hero;
import org.barcelonajug.superherobattlearena.domain.json.DraftSubmission;
import org.barcelonajug.superherobattlearena.domain.json.RoundSpec;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

class SubmissionValidatorTest {

        private RosterService rosterService;
        private SubmissionValidator validator;

        @BeforeEach
        void setUp() {
                rosterService = Mockito.mock(RosterService.class);
                validator = new SubmissionValidator(rosterService);

                // Mock some heroes
                when(rosterService.getHero(1))
                                .thenReturn(Optional.of(
                                                new Hero(1, "H1", "h1", new Hero.PowerStats(0, 0, 0, 0, 0, 0), "Tank",
                                                                10, "good", "Marvel", null, null, List.of("A"),
                                                                new Hero.Images(null, null, null, null))));
                when(rosterService.getHero(2))
                                .thenReturn(Optional.of(
                                                new Hero(2, "H2", "h2", new Hero.PowerStats(0, 0, 0, 0, 0, 0), "Dps",
                                                                20, "bad", "DC", null, null, List.of("B"),
                                                                new Hero.Images(null, null, null, null))));
                when(rosterService.getHero(3))
                                .thenReturn(Optional.of(new Hero(3, "H3", "h3", new Hero.PowerStats(0, 0, 0, 0, 0, 0),
                                                "Heal", 15, "neutral", "Image",
                                                null, null, List.of("C", "Banned"),
                                                new Hero.Images(null, null, null, null))));
        }

        @Test
        void shouldValidateValidSubmission() {
                RoundSpec spec = new RoundSpec("Test", 2, 50, Map.of("Tank", 1), null, null, null, "Basic");
                DraftSubmission submission = new DraftSubmission(List.of(1, 2), "Attack");

                assertThatCode(() -> validator.validate(submission, spec))
                                .doesNotThrowAnyException();
        }

        @Test
        void shouldFailWrongTeamSize() {
                RoundSpec spec = new RoundSpec("Test", 3, 50, null, null, null, null, "Basic");
                DraftSubmission submission = new DraftSubmission(List.of(1, 2), "Attack");

                assertThatThrownBy(() -> validator.validate(submission, spec))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessageContaining("Team size");
        }

        @Test
        void shouldFailBudgetExceeded() {
                RoundSpec spec = new RoundSpec("Test", 2, 25, null, null, null, null, "Basic");
                DraftSubmission submission = new DraftSubmission(List.of(1, 2), "Attack"); // Cost 10+20=30 > 25

                assertThatThrownBy(() -> validator.validate(submission, spec))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessageContaining("exceeds maximum");
        }

        @Test
        void shouldFailMissingRole() {
                RoundSpec spec = new RoundSpec("Test", 2, 50, Map.of("Heal", 1), null, null, null, "Basic");
                DraftSubmission submission = new DraftSubmission(List.of(1, 2), "Attack"); // Tank, Dps. Missing Heal.

                assertThatThrownBy(() -> validator.validate(submission, spec))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessageContaining("Missing required role");
        }

        @Test
        void shouldFailBannedTag() {
                RoundSpec spec = new RoundSpec("Test", 1, 50, null, null, List.of("Banned"), null, "Basic");
                DraftSubmission submission = new DraftSubmission(List.of(3), "Attack"); // Has "Banned" tag

                assertThatThrownBy(() -> validator.validate(submission, spec))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessageContaining("banned tag");
        }

        @Test
        void shouldFailDuplicates() {
                RoundSpec spec = new RoundSpec("Test", 2, 50, null, null, null, null, "Basic");
                DraftSubmission submission = new DraftSubmission(List.of(1, 1), "Attack");

                assertThatThrownBy(() -> validator.validate(submission, spec))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessageContaining("Duplicate heroes");
        }
}
