package org.barcelonajug.superherobattlearena.application.usecase;

import java.util.List;
import java.util.Optional;
import org.barcelonajug.superherobattlearena.application.port.out.SuperheroRepositoryPort;
import org.barcelonajug.superherobattlearena.domain.Hero;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class RosterUseCase {

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
    return superheroRepository.findById(id);
  }

  @Cacheable(value = "heroLists", key = "#ids.hashCode()")
  public List<Hero> getHeroes(List<Integer> ids) {
    return superheroRepository.findByIds(ids);
  }

  public List<Hero> searchHeroes(String term) {
    return superheroRepository.searchByName(term);
  }

  public List<Hero> filterHeroes(String alignment, String publisher) {
    return superheroRepository.findByAlignmentAndPublisher(alignment, publisher);
  }

  public long countHeroes() {
    return superheroRepository.count();
  }
}
