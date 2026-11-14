///usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS com.fasterxml.jackson.core:jackson-databind:2.18.2

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

class ExtractHeroes {
  private static final int REQUIRED_ARGS_COUNT = 2;

  private final String jsonFile;
  private final int count;
  private final ObjectMapper objectMapper;

  ExtractHeroes(final String jsonFile, final int count) {
    this.jsonFile = jsonFile;
    this.count = count;
    this.objectMapper = new ObjectMapper();
  }

  public static void main(String[] args) {
    if (args.length < REQUIRED_ARGS_COUNT) {
      System.err.println("Usage: jbang ExtractHeroes.java <json-file> <count>");
      System.exit(1);
    }

    final String jsonFile = args[0];
    final int count = Integer.parseInt(args[1]);

    final ExtractHeroes extractor = new ExtractHeroes(jsonFile, count);
    extractor.run();
  }

  void run() {
    try {
      final JsonNode heroes = objectMapper.readTree(new File(jsonFile));
      System.err.println("Total heroes found in file: " + heroes.size());

      final List<Integer> heroIds = extractHeroIds(heroes);
      System.err.println("Extracted " + heroIds.size() + " hero IDs.");

      final String output =
          heroIds.stream().map(String::valueOf).collect(Collectors.joining(" "));

      System.out.println(output);
    } catch (Exception e) {
      System.err.println("Error extracting heroes: " + e.getMessage());
      System.exit(1);
    }
  }

  private List<Integer> extractHeroIds(final JsonNode heroes) {
    final int maxCount = Math.min(count, heroes.size());

    return IntStream.range(0, maxCount)
        .mapToObj(heroes::get)
        .map(hero -> hero.get("id").asInt())
        .collect(Collectors.toUnmodifiableList());
  }
}
