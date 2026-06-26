package org.barcelonajug.superherobattlearena.adapter.out.persistence;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.barcelonajug.superherobattlearena.adapter.out.persistence.entity.SuperheroEntity;
import org.barcelonajug.superherobattlearena.adapter.out.persistence.mapper.SuperheroMapper;
import org.barcelonajug.superherobattlearena.adapter.out.persistence.repository.SpringDataSuperheroRepository;
import org.barcelonajug.superherobattlearena.application.port.out.SuperheroRepositoryPort;
import org.barcelonajug.superherobattlearena.domain.Hero;
import org.barcelonajug.superherobattlearena.domain.HeroSearchCriteria;
import org.barcelonajug.superherobattlearena.domain.HeroSearchResult;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

@Component
public class SuperheroPersistenceAdapter implements SuperheroRepositoryPort {

  private final SpringDataSuperheroRepository repository;
  private final SuperheroMapper mapper;
  private final SuperheroSearchQuery searchQuery;

  public SuperheroPersistenceAdapter(
      SpringDataSuperheroRepository repository,
      SuperheroMapper mapper,
      SuperheroSearchQuery searchQuery) {
    this.repository = repository;
    this.mapper = mapper;
    this.searchQuery = searchQuery;
  }

  @Override
  public List<Hero> findAll() {
    return toHeroes(repository.findAll());
  }

  @Override
  public List<Hero> findAll(int page, int size) {
    return toHeroes(repository.findAll(PageRequest.of(page, size)).getContent());
  }

  @Override
  public Optional<Hero> findById(Integer id) {
    return repository.findById(id).map(mapper::toDomain);
  }

  @Override
  public List<Hero> findByIds(List<Integer> ids) {
    return toHeroes(repository.findAllById(ids));
  }

  @Override
  public List<Hero> searchByName(String term) {
    return toHeroes(repository.searchByName(term));
  }

  @Override
  public List<Hero> findByAlignmentAndPublisher(String alignment, String publisher) {
    if (alignment != null && publisher != null) {
      return toHeroes(repository.findByAlignmentAndPublisher(alignment, publisher));
    } else if (alignment != null) {
      return toHeroes(repository.findByAlignment(alignment));
    } else if (publisher != null) {
      return toHeroes(repository.findByPublisher(publisher));
    }
    return findAll();
  }

  @Override
  public long count() {
    return repository.count();
  }

  @Override
  @SuppressWarnings("NullAway")
  public HeroSearchResult search(HeroSearchCriteria criteria) {
    SuperheroSearchQuery.SearchResultPage result = searchQuery.search(criteria);
    List<Hero> heroes = toHeroes(result.entities());

    return new HeroSearchResult(
        heroes,
        result.totalElements(),
        result.totalPages(),
        criteria.page(),
        criteria.size(),
        criteria.page() < result.totalPages() - 1,
        criteria.page() > 0);
  }

  private List<Hero> toHeroes(List<SuperheroEntity> entities) {
    return entities.stream()
        .map(mapper::toDomain)
        .filter(Objects::nonNull)
        .map(Objects::requireNonNull)
        .toList();
  }
}
