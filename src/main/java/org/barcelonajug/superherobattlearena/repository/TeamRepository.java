package org.barcelonajug.superherobattlearena.repository;

import java.util.UUID;
import org.barcelonajug.superherobattlearena.domain.Team;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamRepository extends JpaRepository<Team, UUID> {
    boolean existsByName(String name);
}
