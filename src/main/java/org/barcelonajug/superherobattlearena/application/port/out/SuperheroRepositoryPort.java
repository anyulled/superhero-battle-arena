package org.barcelonajug.superherobattlearena.application.port.out;

import java.util.List;
import java.util.Optional;
import org.barcelonajug.superherobattlearena.domain.Hero;
import org.barcelonajug.superherobattlearena.domain.HeroSearchCriteria;
import org.barcelonajug.superherobattlearena.domain.HeroSearchResult;

public interface SuperheroRepositoryPort {
  List<Hero> findAll();

  Optional<Hero> findById(Integer id);

  List<Hero> searchByName(String term);

  List<Hero> findByAlignmentAndPublisher(String alignment, String publisher);

  List<Hero> findAll(int page, int size);

  List<Hero> findByIds(List<Integer> ids);

  long count();

  HeroSearchResult search(HeroSearchCriteria criteria);
}
