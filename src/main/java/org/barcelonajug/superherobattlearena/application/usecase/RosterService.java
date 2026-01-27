package org.barcelonajug.superherobattlearena.application.usecase;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import jakarta.annotation.PostConstruct;

import org.barcelonajug.superherobattlearena.domain.Hero;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

@Service
public class RosterService {

    private final ObjectMapper objectMapper;
    private Map<Integer, Hero> heroes;

    public RosterService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void loadRoster() throws IOException {
        ClassPathResource resource = new ClassPathResource("all-superheroes.json");
        try (InputStream inputStream = resource.getInputStream()) {
            List<Hero> heroesList = objectMapper.readValue(inputStream, new TypeReference<List<Hero>>() {
            });
            this.heroes = heroesList.stream()
                    .collect(Collectors.toMap(Hero::id, Function.identity()));
        }
    }

    public List<Hero> getAllHeroes() {
        return List.copyOf(heroes.values());
    }

    public Optional<Hero> getHero(int id) {
        return Optional.ofNullable(heroes.get(id));
    }
}
