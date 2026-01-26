package org.barcelonajug.superherobattlearena.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.barcelonajug.superherobattlearena.domain.Hero;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

@Service
public class RosterService {

    private final ObjectMapper objectMapper;
    private Map<Integer, Hero> heroMap;

    public RosterService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void loadRoster() throws IOException {
        ClassPathResource resource = new ClassPathResource("data/all-heroes.json");
        try (InputStream inputStream = resource.getInputStream()) {
            List<Hero> heroes = objectMapper.readValue(inputStream, new TypeReference<List<Hero>>() {
            });
            heroMap = heroes.stream()
                    .collect(Collectors.toMap(Hero::id, Function.identity()));
        }
    }

    public Optional<Hero> getHero(int id) {
        return Optional.ofNullable(heroMap.get(id));
    }

    public List<Hero> getAllHeroes() {
        return List.copyOf(heroMap.values());
    }
}
