package org.barcelonajug.superherobattlearena.adapter.out.persistence.repository;

import java.util.Optional;
import java.util.UUID;
import java.util.List;
import org.barcelonajug.superherobattlearena.adapter.out.persistence.entity.SubmissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpringDataSubmissionRepository extends JpaRepository<SubmissionEntity, SubmissionEntity.SubmissionId> {
    Optional<SubmissionEntity> findByTeamIdAndRoundNo(UUID teamId, Integer roundNo);

    List<SubmissionEntity> findByRoundNo(Integer roundNo);
}
