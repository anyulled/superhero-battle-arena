package org.barcelonajug.superherobattlearena.repository;

import java.util.UUID;
import org.barcelonajug.superherobattlearena.domain.Match;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MatchRepository extends JpaRepository<Match, UUID> {
}
