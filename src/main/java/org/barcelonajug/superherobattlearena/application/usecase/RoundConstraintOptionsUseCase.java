package org.barcelonajug.superherobattlearena.application.usecase;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.function.Function;
import java.util.stream.StreamSupport;
import org.barcelonajug.superherobattlearena.domain.RoundConstraintOptions;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

@Service
public class RoundConstraintOptionsUseCase {

  private final ObjectMapper objectMapper = new ObjectMapper();
  private static final List<String> ROLE_OPTIONS =
      List.of("Assassin", "Fighter", "Support", "Tank");

  public RoundConstraintOptions getOptions() {
    JsonNode heroes = readHeroes();
    return new RoundConstraintOptions(
        ROLE_OPTIONS,
        collectDistinct(heroes, hero -> hero.path("appearance").path("gender").asText(null)),
        collectDistinct(heroes, hero -> hero.path("appearance").path("race").asText(null)),
        collectDistinct(heroes, hero -> hero.path("biography").path("publisher").asText(null)),
        collectDistinct(heroes, hero -> hero.path("biography").path("alignment").asText(null)));
  }

  private JsonNode readHeroes() {
    try (InputStream inputStream = new ClassPathResource("all-superheroes.json").getInputStream()) {
      return objectMapper.readTree(inputStream);
    } catch (IOException e) {
      throw new IllegalStateException("Failed to load superhero constraint options", e);
    }
  }

  private List<String> collectDistinct(JsonNode heroes, Function<JsonNode, String> extractor) {
    return StreamSupport.stream(heroes.spliterator(), false)
        .map(extractor)
        .filter(value -> value != null && !value.isBlank())
        .distinct()
        .sorted()
        .toList();
  }
}
