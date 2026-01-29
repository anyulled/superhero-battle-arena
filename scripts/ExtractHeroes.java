///usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS com.fasterxml.jackson.core:jackson-databind:2.18.2

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

/**
 * JBang script to extract hero IDs from the all-superheroes.json file.
 * Uses immutable, functional programming style.
 * 
 * Usage: jbang ExtractHeroes.java <json-file> <count>
 */
class ExtractHeroes {

    private final String jsonFile;
    private final int count;
    private final ObjectMapper objectMapper;

    public ExtractHeroes(final String jsonFile, final int count) {
        this.jsonFile = jsonFile;
        this.count = count;
        this.objectMapper = new ObjectMapper();
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: jbang ExtractHeroes.java <json-file> <count>");
            System.exit(1);
        }

        final String jsonFile = args[0];
        final int count = Integer.parseInt(args[1]);

        final ExtractHeroes extractor = new ExtractHeroes(jsonFile, count);
        extractor.run();
    }

    public void run() {
        try {
            final JsonNode heroes = objectMapper.readTree(new File(jsonFile));
            System.err.println("Total heroes found in file: " + heroes.size());

            final List<Integer> heroIds = extractHeroIds(heroes);
            System.err.println("Extracted " + heroIds.size() + " hero IDs.");

            // Output space-separated IDs for bash compatibility
            final String output = heroIds.stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(" "));

            System.out.println(output);

        } catch (Exception e) {
            System.err.println("Error extracting heroes: " + e.getMessage());
            e.printStackTrace();
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
