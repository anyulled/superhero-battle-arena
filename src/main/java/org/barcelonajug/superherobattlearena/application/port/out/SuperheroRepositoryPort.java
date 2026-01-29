package org.barcelonajug.superherobattlearena.application.port.out;

import org.barcelonajug.superherobattlearena.domain.Hero;
import java.util.List;
import java.util.Optional;

public interface SuperheroRepositoryPort {
    List<Hero> findAll();

    Optional<Hero> findById(Integer id);

    List<Hero> searchByName(String term);

    List<Hero> findByAlignmentAndPublisher(String alignment, String publisher);

    // Add pagination support later or now? The plan mentions pagination.
    // Standard approach might need a domain Page object or use Spring Data's Page.
    // To keep it simple and dependency-free in domain, maybe List with
    // offset/limit?
    // Or just return List for now as per RosterService needs.
    // Wait, HeroController needs pagination.
    // Let's use List for now and add pagination logic in Controller or Service if
    // needed, or simple offset/limit params here.
    List<Hero> findAll(int page, int size);

    long count();
}
