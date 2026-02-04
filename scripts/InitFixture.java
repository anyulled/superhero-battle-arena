///usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS com.fasterxml.jackson.core:jackson-databind:2.18.2
//DEPS com.github.javafaker:javafaker:1.0.2
//DEPS org.slf4j:slf4j-simple:2.0.9

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;

import java.io.File;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JBang script to initialize fixture data for the Superhero Battle Arena.
 * Uses Java Faker to generate team member names that are different from hero
 * names.
 * 
 * Usage: jbang InitFixture.java [session-id|base-url]
 * [base-url|heroes-json-file] [heroes-json-file]
 * Defaults: session-id=random UUID, base-url=http://localhost:8080,
 * heroes-json-file=src/main/resources/all-superheroes.json
 */
class InitFixture {
    private static final String DEFAULT_BASE_URL = "http://localhost:8080";
    private static final String DEFAULT_HEROES_FILE = "src/main/resources/all-superheroes.json";
    private static final String AUTH_HEADER = "Basic YWRtaW46YWRtaW4=";
    private static final int TEAM_COUNT = 20;
    private static final int HEROES_PER_TEAM = 5;
    private static final Logger logger = LoggerFactory.getLogger(InitFixture.class);

    private final String sessionId;
    private final String baseUrl;
    private final String heroesFile;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final Faker faker;
    private final Set<String> usedHeroNames;

    public InitFixture(final String sessionId, final String baseUrl, final String heroesFile) {
        this.sessionId = sessionId;
        this.baseUrl = baseUrl;
        this.heroesFile = heroesFile;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.objectMapper = new ObjectMapper();
        this.faker = new Faker();
        this.usedHeroNames = new HashSet<>();
    }

    public static void main(String[] args) {
        final String sessionId;
        final String baseUrl;
        final String heroesFile;

        if (args.length == 0) {
            sessionId = UUID.randomUUID().toString();
            baseUrl = DEFAULT_BASE_URL;
            heroesFile = DEFAULT_HEROES_FILE;
        } else if (args[0].startsWith("http")) {
            sessionId = UUID.randomUUID().toString();
            baseUrl = args[0];
            heroesFile = args.length > 1 ? args[1] : DEFAULT_HEROES_FILE;
        } else {
            sessionId = args[0];
            baseUrl = args.length > 1 ? args[1] : DEFAULT_BASE_URL;
            heroesFile = args.length > 2 ? args[2] : DEFAULT_HEROES_FILE;
        }

        final InitFixture fixture = new InitFixture(sessionId, baseUrl, heroesFile);
        fixture.run();
    }

    public void run() {
        printHeader("Initializing Enhanced Fixture Data");

        try {
            initializeSession();
            final List<String> teamIds = registerTeams();
            createRound();
            final List<Integer> heroIds = extractHeroIds();
            submitSquadFormations(teamIds, heroIds);
            printSuccess();
        } catch (Exception e) {
            logger.error("Error initializing fixture: {}", e.getMessage(), e);
            System.exit(1);
        }
    }

    private void initializeSession() throws Exception {
        logger.info("Initializing Session: {}", sessionId);

        final String url = baseUrl + "/api/admin/sessions/start?sessionId=" + sessionId;
        final HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", AUTH_HEADER)
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        httpClient.send(request, HttpResponse.BodyHandlers.discarding());
        logger.info("Session started.");
    }

    private List<String> registerTeams() throws Exception {
        printHeader("Registering " + TEAM_COUNT + " Teams...");

        final List<TeamConfig> teamConfigs = generateTeamConfigs();
        final List<String> teamIds = new ArrayList<>();

        for (int i = 0; i < teamConfigs.size(); i++) {
            final TeamConfig config = teamConfigs.get(i);
            logger.info("[{}/{}] Registering: {}", i + 1, TEAM_COUNT, config.name());

            final String teamId = registerTeam(config);
            teamIds.add(teamId);

            logger.info("   Team ID: {}", teamId);
        }

        logger.info("All {} teams registered successfully!", TEAM_COUNT);
        return Collections.unmodifiableList(teamIds);
    }

    private String registerTeam(final TeamConfig config) throws Exception {
        final String encodedName = URLEncoder.encode(config.name(), StandardCharsets.UTF_8);
        final String encodedMembers = URLEncoder.encode(config.members(), StandardCharsets.UTF_8);

        final String url = String.format(
                "%s/api/teams/register?name=%s&members=%s&sessionId=%s",
                baseUrl, encodedName, encodedMembers, sessionId);

        final HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        final HttpResponse<String> response = httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString());

        return response.body().replace("\"", "");
    }

    private void createRound() throws Exception {
        printHeader("Creating Round 1...");

        final String url = baseUrl + "/api/admin/rounds/create";

        // Create a default RoundSpec
        Map<String, Object> spec = new HashMap<>();
        spec.put("description", "Round 1 - The Beginning");
        spec.put("teamSize", HEROES_PER_TEAM);
        spec.put("budgetCap", 100);
        spec.put("requiredRoles", Map.of());
        spec.put("maxSameRole", Map.of());
        spec.put("bannedTags", List.of());
        spec.put("tagModifiers", Map.of());
        spec.put("mapType", "ARENA");

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("sessionId", sessionId);
        requestBody.put("roundNo", 1);
        requestBody.put("spec", spec);

        String jsonBody = objectMapper.writeValueAsString(requestBody);

        final HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .header("Authorization", AUTH_HEADER)
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        httpClient.send(request, HttpResponse.BodyHandlers.discarding());
        logger.info("Round 1 created via Admin API!");
    }

    private List<Integer> extractHeroIds() throws Exception {
        final JsonNode heroes = objectMapper.readTree(new File(heroesFile));
        logger.info("Total heroes found in file: {}", heroes.size());

        final int requiredHeroes = TEAM_COUNT * HEROES_PER_TEAM;
        final List<Integer> heroIds = IntStream.range(0, Math.min(requiredHeroes, heroes.size()))
                .mapToObj(heroes::get)
                .peek(hero -> usedHeroNames.add(hero.get("name").asText().toLowerCase().replaceAll("\\s+", "")))
                .map(hero -> hero.get("id").asInt())
                .collect(Collectors.toList());

        logger.info("Extracted {} hero IDs.", heroIds.size());
        return Collections.unmodifiableList(heroIds);
    }

    private void submitSquadFormations(final List<String> teamIds, final List<Integer> heroIds) throws Exception {
        printHeader("Submitting Squad Formations...");

        final List<Strategy> strategies = List.of(Strategy.AGGRESSIVE, Strategy.DEFENSIVE, Strategy.BALANCED);

        for (int i = 0; i < teamIds.size(); i++) {
            final String teamId = teamIds.get(i);
            final int startIdx = i * HEROES_PER_TEAM;
            final List<Integer> teamHeroIds = heroIds.subList(startIdx, startIdx + HEROES_PER_TEAM);
            final Strategy strategy = strategies.get(i % strategies.size());

            logger.info("[{}/{}] Submitting roster for Team {}", i + 1, teamIds.size(), teamId);
            logger.info("   Heroes: {}", teamHeroIds);
            logger.info("   Strategy: {}", strategy);

            submitRoster(teamId, teamHeroIds, strategy);
        }
    }

    private void submitRoster(final String teamId, final List<Integer> heroIds, final Strategy strategy)
            throws Exception {
        final String url = baseUrl + "/api/rounds/1/submit?teamId=" + teamId;
        final String heroIdsJson = heroIds.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));

        final String jsonBody = String.format(
                "{\"heroIds\":[%s],\"strategy\":\"%s\"}",
                heroIdsJson, strategy);

        final HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        httpClient.send(request, HttpResponse.BodyHandlers.discarding());
    }

    private List<TeamConfig> generateTeamConfigs() {
        final List<String> teamNames = List.of(
                "Cosmic Defenders", "Shadow Warriors", "Quantum Squad", "Mystic Alliance",
                "Tech Titans", "Emerald Knights", "Thunder Legion", "Phantom Force",
                "Velocity Vanguard", "Crimson Crusaders", "Arctic Avengers", "Inferno Brigade",
                "Stealth Syndicate", "Psionic Protectors", "Aquatic Armada", "Savage Squad",
                "Celestial Champions", "Dark Dimension", "Speedster Alliance", "Ultimate Warriors");

        return teamNames.stream()
                .map(this::createTeamConfig)
                .toList();
    }

    private TeamConfig createTeamConfig(final String teamName) {
        final int memberCount = faker.number().numberBetween(2, 6);
        final List<String> members = generateUniqueMembers(memberCount);
        final String membersString = String.join(",", members);

        return new TeamConfig(teamName, membersString);
    }

    private List<String> generateUniqueMembers(final int count) {
        final Set<String> members = new HashSet<>();

        while (members.size() < count) {
            final String member = generateUniqueMemberName();
            members.add(member);
        }

        return new ArrayList<>(members);
    }

    private String generateUniqueMemberName() {
        return faker.name().firstName() + " " + faker.name().lastName();
    }

    private void printHeader(final String message) {
        logger.info("=========================================");
        logger.info(message);
        logger.info("=========================================");
    }

    private void printSuccess() {
        printHeader("Fixture Data Initialized Successfully!");
        logger.info("Session ID: {}", sessionId);
        logger.info("Total Teams: {}", TEAM_COUNT);
        logger.info("Round 1: Created with {} squad submissions", TEAM_COUNT);
        logger.info("You can now create matches between any of the {} teams!", TEAM_COUNT);
        logger.info("=========================================");
    }

    // Immutable record for team configuration
    private record TeamConfig(String name, String members) {
    }

    // Enum for battle strategies
    private enum Strategy {
        AGGRESSIVE, DEFENSIVE, BALANCED
    }
}
