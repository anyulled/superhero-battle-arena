package org.barcelonajug.superherobattlearena.application.port.out;

import java.util.Optional;
import java.util.UUID;
import org.barcelonajug.superherobattlearena.domain.Submission;

public interface SubmissionRepositoryPort {
    Submission save(Submission submission);

    Optional<Submission> findByTeamIdAndRoundNo(UUID teamId, Integer roundNo);
}
