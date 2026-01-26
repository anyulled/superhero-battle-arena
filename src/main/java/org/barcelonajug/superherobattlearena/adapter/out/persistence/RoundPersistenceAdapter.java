package org.barcelonajug.superherobattlearena.adapter.out.persistence;

import java.util.Optional;
import org.barcelonajug.superherobattlearena.adapter.out.persistence.mapper.RoundMapper;
import org.barcelonajug.superherobattlearena.adapter.out.persistence.repository.SpringDataRoundRepository;
import org.barcelonajug.superherobattlearena.application.port.out.RoundRepositoryPort;
import org.barcelonajug.superherobattlearena.domain.Round;
import org.springframework.stereotype.Component;

@Component
public class RoundPersistenceAdapter implements RoundRepositoryPort {

    private final SpringDataRoundRepository repository;
    private final RoundMapper mapper;

    public RoundPersistenceAdapter(SpringDataRoundRepository repository, RoundMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Optional<Round> findById(Integer roundNo) {
        return repository.findById(roundNo).map(mapper::toDomain);
    }

    @Override
    public Round save(Round round) {
        return mapper.toDomain(repository.save(mapper.toEntity(round)));
    }
}
