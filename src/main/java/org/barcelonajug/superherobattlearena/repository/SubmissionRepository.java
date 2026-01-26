package org.barcelonajug.superherobattlearena.repository;

import java.util.List;
import java.util.UUID;
import org.barcelonajug.superherobattlearena.domain.Submission;
import org.barcelonajug.superherobattlearena.domain.SubmissionId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubmissionRepository extends JpaRepository<Submission, SubmissionId> {
    List<Submission> findByRoundNoAndAcceptedTrue(Integer roundNo);
}
