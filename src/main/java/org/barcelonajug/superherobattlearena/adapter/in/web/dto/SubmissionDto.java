package org.barcelonajug.superherobattlearena.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.barcelonajug.superherobattlearena.domain.Submission;
import org.barcelonajug.superherobattlearena.domain.json.DraftSubmission;
import org.jspecify.annotations.Nullable;

@Schema(name = "Submission")
public record SubmissionDto(
    UUID teamId,
    Integer roundNo,
    @Nullable DraftSubmissionDto submissionJson,
    Boolean accepted,
    @Nullable String rejectedReason,
    @Nullable OffsetDateTime submittedAt) {
  public static SubmissionDto from(Submission value) {
    return new SubmissionDto(
        value.getTeamId(),
        value.getRoundNo(),
        nestedFrom(value.getSubmissionJson()),
        value.getAccepted(),
        value.getRejectedReason(),
        value.getSubmittedAt());
  }

  private static @Nullable DraftSubmissionDto nestedFrom(@Nullable DraftSubmission value) {
    if (value == null) {
      return null;
    }
    return DraftSubmissionDto.from(value);
  }
}
