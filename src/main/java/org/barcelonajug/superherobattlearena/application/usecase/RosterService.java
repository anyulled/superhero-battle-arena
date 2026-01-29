package org.barcelonajug.superherobattlearena.application.usecase;

import java.util.List;
import java.util.Optional;

import org.barcelonajug.superherobattlearena.application.port.out.SuperheroRepositoryPort;
import org.barcelonajug.superherobattlearena.domain.Hero;
import org.springframework.stereotype.Service;

@Service
public class RosterService {

    private final SuperheroRepositoryPort superheroRepository;

    public RosterService(SuperheroRepositoryPort superheroRepository) {
        this.superheroRepository = superheroRepository;
    }

    public List<Hero> getAllHeroes() {
        return superheroRepository.findAll();
    }

    public List<Hero> getAllHeroes(int page, int size) {
        return superheroRepository.findAll(page, size);
    }

    public Optional<Hero> getHero(int id) {
        return superheroRepository.findById(id);
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
