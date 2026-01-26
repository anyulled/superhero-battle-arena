package org.barcelonajug.superherobattlearena.adapter.out.persistence.mapper;

import org.barcelonajug.superherobattlearena.adapter.out.persistence.entity.SubmissionEntity;
import org.barcelonajug.superherobattlearena.domain.Submission;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SubmissionMapper {
    Submission toDomain(SubmissionEntity entity);

    SubmissionEntity toEntity(Submission domain);
}
