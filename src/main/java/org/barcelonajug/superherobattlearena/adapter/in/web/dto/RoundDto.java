package org.barcelonajug.superherobattlearena.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.barcelonajug.superherobattlearena.domain.Round;
import org.barcelonajug.superherobattlearena.domain.RoundStatus;
import org.barcelonajug.superherobattlearena.domain.json.RoundSpec;
import org.jspecify.annotations.Nullable;

@Schema(name = "Round")
public record RoundDto(
    UUID roundId,
    Integer roundNo,
    UUID sessionId,
    @Nullable Long seed,
    @Nullable RoundSpecDto specJson,
    RoundStatus status,
    @Nullable OffsetDateTime submissionDeadline) {
  public static RoundDto from(Round value) {
    return new RoundDto(
        value.getRoundId(),
        value.getRoundNo(),
        value.getSessionId(),
        value.getSeed(),
        nestedFrom(value.getSpecJson()),
        value.getStatus(),
        value.getSubmissionDeadline());
  }

  private static @Nullable RoundSpecDto nestedFrom(@Nullable RoundSpec value) {
    if (value == null) {
      return null;
    }
    return RoundSpecDto.from(value);
  }
}
