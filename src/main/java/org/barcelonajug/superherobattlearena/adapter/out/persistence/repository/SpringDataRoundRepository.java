package org.barcelonajug.superherobattlearena.adapter.out.persistence.repository;

import org.barcelonajug.superherobattlearena.adapter.out.persistence.entity.RoundEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpringDataRoundRepository extends JpaRepository<RoundEntity, Integer> {
}
