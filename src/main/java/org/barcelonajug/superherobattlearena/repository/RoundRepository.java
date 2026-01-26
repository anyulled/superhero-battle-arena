package org.barcelonajug.superherobattlearena.repository;

import org.barcelonajug.superherobattlearena.domain.Round;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoundRepository extends JpaRepository<Round, Integer> {
}
