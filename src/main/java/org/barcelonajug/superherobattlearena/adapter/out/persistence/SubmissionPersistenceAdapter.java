package org.barcelonajug.superherobattlearena.adapter.out.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.barcelonajug.superherobattlearena.adapter.out.persistence.mapper.SubmissionMapper;
import org.barcelonajug.superherobattlearena.adapter.out.persistence.repository.SpringDataSubmissionRepository;
import org.barcelonajug.superherobattlearena.application.port.out.SubmissionRepositoryPort;
import org.barcelonajug.superherobattlearena.domain.Submission;
import org.springframework.stereotype.Component;

@Component
public class SubmissionPersistenceAdapter implements SubmissionRepositoryPort {

  private final SpringDataSubmissionRepository repository;
  private final SubmissionMapper mapper;

  public SubmissionPersistenceAdapter(
      SpringDataSubmissionRepository repository, SubmissionMapper mapper) {
    this.repository = repository;
    this.mapper = mapper;
  }

  @Override
  public Submission save(Submission submission) {
    return java.util.Objects.requireNonNull(
        mapper.toDomain(
            repository.save(java.util.Objects.requireNonNull(mapper.toEntity(submission)))));
  }

  @Override
  public Optional<Submission> findByTeamIdAndRoundNo(UUID teamId, Integer roundNo) {
    return repository.findByTeamIdAndRoundNo(teamId, roundNo).map(mapper::toDomain);
  }

  @Override
  public List<Submission> findByRoundNo(Integer roundNo) {
    return repository.findByRoundNo(roundNo).stream()
        .map(mapper::toDomain)
        .filter(java.util.Objects::nonNull)
        .map(java.util.Objects::requireNonNull)
        .toList();
  }

  @Override
  public void deleteAll() {
    repository.deleteAll();
  }
}
