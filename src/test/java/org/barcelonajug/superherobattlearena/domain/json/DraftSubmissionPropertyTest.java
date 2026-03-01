package org.barcelonajug.superherobattlearena.domain.json;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.Size;

class DraftSubmissionPropertyTest {

  @Property
  void draftSubmissionHandlesAnyValidOrInvalidInputs(
      @ForAll @Size(max = 100) List<Integer> heroIds, @ForAll String strategy) {

    DraftSubmission submission = new DraftSubmission(heroIds, strategy);

    assertThat(submission.heroIds()).isEqualTo(heroIds);
    assertThat(submission.strategy()).isEqualTo(strategy);
  }
}
