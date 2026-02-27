package org.barcelonajug.superherobattlearena.adapter.out.persistence.repository;

import java.util.List;

import org.barcelonajug.superherobattlearena.adapter.out.persistence.entity.SuperheroEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface SpringDataSuperheroRepository extends JpaRepository<SuperheroEntity, Integer> {

  @Query("SELECT s FROM SuperheroEntity s WHERE LOWER(s.name) LIKE LOWER(CONCAT('%', :term, '%'))")
  List<SuperheroEntity> searchByName(String term);

  List<SuperheroEntity> findByAlignmentAndPublisher(String alignment, String publisher);

  List<SuperheroEntity> findByAlignment(String alignment);

  List<SuperheroEntity> findByPublisher(String publisher);

  @Override
  Page<SuperheroEntity> findAll(Pageable pageable);
}
