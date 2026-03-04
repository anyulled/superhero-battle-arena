package org.barcelonajug.superherobattlearena.application.usecase;

import org.barcelonajug.superherobattlearena.application.port.out.SuperheroRepositoryPort;
import org.barcelonajug.superherobattlearena.domain.HeroSearchCriteria;
import org.barcelonajug.superherobattlearena.domain.HeroSearchResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class HeroSearchUseCase {

  private static final Logger log = LoggerFactory.getLogger(HeroSearchUseCase.class);

  private final SuperheroRepositoryPort superheroRepository;

  public HeroSearchUseCase(SuperheroRepositoryPort superheroRepository) {
    this.superheroRepository = superheroRepository;
  }

  public HeroSearchResult search(HeroSearchCriteria criteria) {
    log.debug("Searching heroes with criteria: {}", criteria);
    HeroSearchResult result = superheroRepository.search(criteria);
    log.debug(
        "Found {} heroes (page {}/{})",
        result.totalElements(),
        result.currentPage() + 1,
        result.totalPages());
    return result;
  }
}
