///usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS com.fasterxml.jackson.core:jackson-databind:2.18.2

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * JBang script to extract hero IDs from the all-superheroes.json file.
 * Usage: jbang ExtractHeroes.java <json-file> <count>
 */
class ExtractHeroes {
    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Usage: jbang ExtractHeroes.java <json-file> <count>");
            System.exit(1);
        }

        String jsonFile = args[0];
        int count = Integer.parseInt(args[1]);

        ObjectMapper mapper = new ObjectMapper();
        JsonNode heroes = mapper.readTree(new File(jsonFile));

        System.err.println("Total heroes found in file: " + heroes.size());

        List<Integer> heroIds = new ArrayList<>();
        for (int i = 0; i < Math.min(count, heroes.size()); i++) {
            JsonNode hero = heroes.get(i);
            heroIds.add(hero.get("id").asInt());
        }

        System.err.println("Extracted " + heroIds.size() + " hero IDs.");

        // Output space-separated IDs for bash
        System.out.println(String.join(" ", heroIds.stream()
                .map(String::valueOf)
                .toArray(String[]::new)));
    }
}
