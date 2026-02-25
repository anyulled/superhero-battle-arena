package org.barcelonajug.superherobattlearena.application.port.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.barcelonajug.superherobattlearena.domain.Submission;

public interface SubmissionRepositoryPort {
  Submission save(Submission submission);

  Optional<Submission> findByTeamIdAndRoundNo(UUID teamId, Integer roundNo);

  List<Submission> findByRoundNo(Integer roundNo);

  void deleteAll();
}
