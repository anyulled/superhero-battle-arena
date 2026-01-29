package org.barcelonajug.superherobattlearena.adapter.out.persistence;

import java.util.List;
import java.util.Optional;
import org.barcelonajug.superherobattlearena.adapter.out.persistence.mapper.SuperheroMapper;
import org.barcelonajug.superherobattlearena.adapter.out.persistence.repository.SpringDataSuperheroRepository;
import org.barcelonajug.superherobattlearena.application.port.out.SuperheroRepositoryPort;
import org.barcelonajug.superherobattlearena.domain.Hero;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

@Component
public class SuperheroPersistenceAdapter implements SuperheroRepositoryPort {

  private final SpringDataSuperheroRepository repository;
  private final SuperheroMapper mapper;

  public SuperheroPersistenceAdapter(
      SpringDataSuperheroRepository repository, SuperheroMapper mapper) {
    this.repository = repository;
    this.mapper = mapper;
  }

  @Override
  public List<Hero> findAll() {
    return repository.findAll().stream().map(mapper::toDomain).toList();
  }

  @Override
  public List<Hero> findAll(int page, int size) {
    return repository.findAll(PageRequest.of(page, size)).stream().map(mapper::toDomain).toList();
  }

  @Override
  public Optional<Hero> findById(Integer id) {
    return repository.findById(id).map(mapper::toDomain);
  }

  @Override
  public List<Hero> findByIds(List<Integer> ids) {
    return repository.findAllById(ids).stream().map(mapper::toDomain).toList();
  }

  @Override
  public List<Hero> searchByName(String term) {
    return repository.searchByName(term).stream().map(mapper::toDomain).toList();
  }

  @Override
  public List<Hero> findByAlignmentAndPublisher(String alignment, String publisher) {
    if (alignment != null && publisher != null) {
      return repository.findByAlignmentAndPublisher(alignment, publisher).stream()
          .map(mapper::toDomain)
          .toList();
    } else if (alignment != null) {
      return repository.findByAlignment(alignment).stream().map(mapper::toDomain).toList();
    } else if (publisher != null) {
      return repository.findByPublisher(publisher).stream().map(mapper::toDomain).toList();
    } else {
      return findAll();
    }
  }

  @Override
  public long count() {
    return repository.count();
  }
}
