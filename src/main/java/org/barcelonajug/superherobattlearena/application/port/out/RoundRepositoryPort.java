package org.barcelonajug.superherobattlearena.application.port.out;

import java.util.Optional;
import org.barcelonajug.superherobattlearena.domain.Round;

public interface RoundRepositoryPort {
    Optional<Round> findById(Integer roundNo);

    Round save(Round round);
}
