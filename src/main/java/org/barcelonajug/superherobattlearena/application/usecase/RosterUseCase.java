package org.barcelonajug.superherobattlearena.application.usecase;

import java.util.List;
import java.util.Optional;
import org.barcelonajug.superherobattlearena.application.port.out.SuperheroRepositoryPort;
import org.barcelonajug.superherobattlearena.domain.Hero;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class RosterUseCase {

  private static final Logger log = LoggerFactory.getLogger(RosterUseCase.class);

  private final SuperheroRepositoryPort superheroRepository;

  public RosterUseCase(SuperheroRepositoryPort superheroRepository) {
    this.superheroRepository = superheroRepository;
  }

  public List<Hero> getAllHeroes() {
    return superheroRepository.findAll();
  }

  public List<Hero> getAllHeroes(int page, int size) {
    return superheroRepository.findAll(page, size);
  }

  @Cacheable(value = "heroes", key = "#id")
  public Optional<Hero> getHero(int id) {
    log.debug("Fetching hero by id={}", id);
    return superheroRepository.findById(id);
  }

  @Cacheable(value = "heroLists", key = "#ids.hashCode()")
  public List<Hero> getHeroes(List<Integer> ids) {
    log.debug("Fetching {} heroes by ids", ids.size());
    return superheroRepository.findByIds(ids);
  }

  public List<Hero> searchHeroes(String term) {
    log.debug("Searching heroes by term='{}'", term);
    List<Hero> results = superheroRepository.searchByName(term);
    log.debug("Found {} heroes matching term='{}'", results.size(), term);
    return results;
  }

  public List<Hero> filterHeroes(String alignment, String publisher) {
    log.debug("Filtering heroes by alignment='{}', publisher='{}'", alignment, publisher);
    List<Hero> results = superheroRepository.findByAlignmentAndPublisher(alignment, publisher);
    log.debug(
        "Found {} heroes matching alignment='{}', publisher='{}'",
        results.size(),
        alignment,
        publisher);
    return results;
  }

  public long countHeroes() {
    return superheroRepository.count();
  }
}
