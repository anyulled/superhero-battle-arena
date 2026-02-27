package org.barcelonajug.superherobattlearena.domain.mother;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.barcelonajug.superherobattlearena.domain.Submission;
import org.barcelonajug.superherobattlearena.domain.json.DraftSubmission;

public final class SubmissionMother {

  private SubmissionMother() {
    // Prevent instantiation
  }

  public static Submission aSubmission(UUID teamId, Integer roundNo, List<Integer> heroIds) {
    return Submission.builder()
        .teamId(teamId)
        .roundNo(roundNo)
        .submissionJson(new DraftSubmission(heroIds, "Created by Mother"))
        .submittedAt(OffsetDateTime.now(ZoneOffset.UTC))
        .build();
  }
}
