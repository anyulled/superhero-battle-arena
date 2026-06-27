///usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS com.fasterxml.jackson.core:jackson-databind:2.18.2
//DEPS com.github.javafaker:javafaker:1.0.2
//DEPS org.slf4j:slf4j-simple:2.0.9
//DEPS info.picocli:picocli:4.7.6
//DEPS io.github.cdimascio:dotenv-java:3.0.0

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import io.github.cdimascio.dotenv.Dotenv;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(
    name = "InitFixture",
    mixinStandardHelpOptions = true,
    version = "2.0",
    description = "Initialize or advance fixture data for the Superhero Battle Arena")
class InitFixture implements Callable<Integer> {
  private static final String DEFAULT_BASE_URL = "http://localhost:8080";
  private static final String DEFAULT_HEROES_FILE = "src/main/resources/all-superheroes.json";
  private static final int DEFAULT_TEAM_COUNT = 20;
  private static final int DEFAULT_TEAM_SIZE = 5;
  private static final int DEFAULT_BUDGET_CAP = 1000;
  private static final String DEFAULT_MAP_TYPE = "ARENA_1";
  private static final Logger logger = LoggerFactory.getLogger(InitFixture.class);
  private static final List<String> DEFAULT_TEAM_NAMES =
      List.of(
          "Cosmic Defenders",
          "Shadow Warriors",
          "Quantum Squad",
          "Mystic Alliance",
          "Tech Titans",
          "Emerald Knights",
          "Thunder Legion",
          "Phantom Force",
          "Velocity Vanguard",
          "Crimson Crusaders",
          "Arctic Avengers",
          "Inferno Brigade",
          "Stealth Syndicate",
          "Psionic Protectors",
          "Aquatic Armada",
          "Savage Squad",
          "Celestial Champions",
          "Dark Dimension",
          "Speedster Alliance",
          "Ultimate Warriors");
  private static final List<String> STRATEGIES =
      List.of("AGGRESSIVE", "DEFENSIVE", "BALANCED");

  @Option(
      names = {"-s", "--session-id"},
      description = "Session ID to create or verify")
  private UUID sessionId;

  @Option(
      names = {"-r", "--round-no"},
      description = "Existing round number to target when reusing data")
  private Integer requestedRoundNo;

  @Option(
      names = {"-t", "--teams"},
      description = "Number of teams to register or target",
      defaultValue = "" + DEFAULT_TEAM_COUNT)
  private int teamCount;

  @Option(
      names = {"-u", "--url"},
      description = "Base URL",
      defaultValue = DEFAULT_BASE_URL)
  private String baseUrl;

  @Option(
      names = {"-f", "--file"},
      description = "Heroes JSON fallback file",
      defaultValue = DEFAULT_HEROES_FILE)
  private String heroesFile;

  @Option(
      names = {"--reset"},
      description = "Reset tournament data before running the flow")
  private boolean reset;

  @Option(
      names = {"--skip-session"},
      description = "Skip session creation and verify the active session")
  private boolean skipSession;

  @Option(
      names = {"--skip-round"},
      description = "Skip round creation and verify an existing round")
  private boolean skipRound;

  @Option(
      names = {"--skip-teams"},
      description = "Skip team registration and reuse existing teams from the session")
  private boolean skipTeams;

  @Option(
      names = {"--skip-squads"},
      description = "Skip squad submission and reuse existing submissions")
  private boolean skipSquads;

  @Option(
      names = {"--skip-matchmaking"},
      description = "Skip admin auto-match")
  private boolean skipMatchmaking;

  @Option(
      names = {"--skip-battles"},
      description = "Skip admin run-all battles")
  private boolean skipBattles;

  @Option(
      names = {"--allow-hero-reuse"},
      description = "Allow the same hero to appear in multiple team submissions")
  private boolean allowHeroReuse;

  @Option(
      names = {"--round-description"},
      description = "Round description",
      defaultValue = "Fixture Round")
  private String roundDescription;

  @Option(
      names = {"--team-size"},
      description = "Round team size",
      defaultValue = "" + DEFAULT_TEAM_SIZE)
  private int teamSize;

  @Option(
      names = {"--budget-cap"},
      description = "Round budget cap",
      defaultValue = "" + DEFAULT_BUDGET_CAP)
  private int budgetCap;

  @Option(
      names = {"--map-type"},
      description = "Round map type",
      defaultValue = DEFAULT_MAP_TYPE)
  private String mapType;

  @Option(
      names = {"--allowed-role"},
      split = ",",
      description = "Allowed roles for the round")
  private List<String> allowedRoles = new ArrayList<>();

  @Option(
      names = {"--allowed-gender"},
      split = ",",
      description = "Allowed genders for the round")
  private List<String> allowedGenders = new ArrayList<>();

  @Option(
      names = {"--allowed-race"},
      split = ",",
      description = "Allowed races for the round")
  private List<String> allowedRaces = new ArrayList<>();

  @Option(
      names = {"--allowed-publisher"},
      split = ",",
      description = "Allowed publishers for the round")
  private List<String> allowedPublishers = new ArrayList<>();

  @Option(
      names = {"--allowed-alignment"},
      split = ",",
      description = "Allowed alignments for the round")
  private List<String> allowedAlignments = new ArrayList<>();

  @Option(
      names = {"--banned-tag"},
      split = ",",
      description = "Banned tags for the round")
  private List<String> bannedTags = new ArrayList<>();

  private HttpClient httpClient;
  private ObjectMapper objectMapper;
  private Faker faker;
  private String authHeader;
  private int activeRoundNo;
  private RoundSpecState activeRoundSpec;
  private Map<Integer, String> roundStatusesByNo = Map.of();

  public static void main(String[] args) {
    int exitCode = new CommandLine(new InitFixture()).execute(args);
    System.exit(exitCode);
  }

  @Override
  public Integer call() throws Exception {
    httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    objectMapper = new ObjectMapper();
    faker = new Faker();

    Dotenv dotenv =
        Dotenv.configure().ignoreIfMissing().filename(".env.local").load();

    String adminUser = dotenv.get("ADMIN_USERNAME", "admin");
    String adminPass = dotenv.get("ADMIN_PASSWORD", "1234");
    authHeader =
        "Basic "
            + Base64.getEncoder()
                .encodeToString((adminUser + ":" + adminPass).getBytes(StandardCharsets.UTF_8));

    runFixture();
    return 0;
  }

  private void runFixture() throws Exception {
    printHeader("Initializing Fixture Data");

    if (reset) {
      resetDatabase();
    }

    initializeOrVerifySession();
    initializeOrVerifyRound();

    List<TeamState> teams = resolveTeams();

    if (!skipSquads) {
      submitSquads(teams);
    } else {
      logger.info("Skipping squad submission (--skip-squads)");
    }

    if (!skipMatchmaking) {
      autoMatch(teams);
    } else {
      logger.info("Skipping auto-match (--skip-matchmaking)");
    }

    if (!skipBattles) {
      runAllBattles();
    } else {
      logger.info("Skipping battle simulation (--skip-battles)");
    }

    printSuccess(teams);
  }

  private void resetDatabase() throws Exception {
    printHeader("Resetting Database");
    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(URI.create(baseUrl + "/api/admin/reset"))
            .header("Authorization", authHeader)
            .POST(HttpRequest.BodyPublishers.noBody())
            .build();
    sendExpecting(request, 200, "Failed to reset database");
  }

  private void initializeOrVerifySession() throws Exception {
    if (!skipSession) {
      initializeSession();
      return;
    }

    logger.info("Skipping session creation (--skip-session)");
    verifySession();
  }

  private void initializeSession() throws Exception {
    if (sessionId == null) {
      logger.info("Creating session with server-generated ID");
    } else {
      logger.info("Creating session {}", sessionId);
    }

    String url = baseUrl + "/api/admin/sessions/start";
    if (sessionId != null) {
      url += "?sessionId=" + sessionId;
    }

    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Authorization", authHeader)
            .POST(HttpRequest.BodyPublishers.noBody())
            .build();

    HttpResponse<String> response = sendExpecting(request, 200, "Failed to create session");
    sessionId = objectMapper.readValue(response.body(), UUID.class);
    logger.info("Using session {}", sessionId);
  }

  private void verifySession() throws Exception {
    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(URI.create(baseUrl + "/api/sessions/active"))
            .GET()
            .build();

    HttpResponse<String> response =
        sendExpecting(request, 200, "Failed to retrieve the active session");
    JsonNode body = objectMapper.readTree(response.body());
    UUID activeSessionId = UUID.fromString(body.path("sessionId").asText());

    if (sessionId != null && !sessionId.equals(activeSessionId)) {
      throw new IllegalStateException(
          "Requested session "
              + sessionId
              + " is not the active session "
              + activeSessionId);
    }

    sessionId = activeSessionId;
    logger.info("Using active session {}", sessionId);
  }

  private void initializeOrVerifyRound() throws Exception {
    if (!skipRound) {
      createRound();
      return;
    }

    logger.info("Skipping round creation (--skip-round)");
    verifyRound();
  }

  private void createRound() throws Exception {
    printHeader("Creating Round");

    Map<String, Object> requestBody = new LinkedHashMap<>();
    requestBody.put("sessionId", sessionId);
    requestBody.put("spec", buildRoundSpecPayload());

    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(URI.create(baseUrl + "/api/admin/rounds/create"))
            .header("Authorization", authHeader)
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(requestBody)))
            .build();

    HttpResponse<String> response = sendExpecting(request, 200, "Failed to create round");
    activeRoundNo = Integer.parseInt(response.body().trim());
    refreshRoundState();
    logger.info("Created round {} for session {}", activeRoundNo, sessionId);
  }

  private Map<String, Object> buildRoundSpecPayload() {
    Map<String, Object> spec = new LinkedHashMap<>();
    spec.put("description", roundDescription);
    spec.put("teamSize", teamSize);
    spec.put("budgetCap", budgetCap);
    spec.put("requiredRoles", Map.of());
    spec.put("maxSameRole", Map.of());
    spec.put("bannedTags", normalizedValues(bannedTags));
    spec.put("tagModifiers", Map.of());
    spec.put("mapType", mapType);
    spec.put("allowedRoles", normalizedValues(allowedRoles));
    spec.put("allowedGenders", normalizedValues(allowedGenders));
    spec.put("allowedRaces", normalizedValues(allowedRaces));
    spec.put("allowedPublishers", normalizedValues(allowedPublishers));
    spec.put("allowedAlignments", normalizedValues(allowedAlignments));
    return spec;
  }

  private void verifyRound() throws Exception {
    List<RoundState> rounds = listRounds();
    if (rounds.isEmpty()) {
      throw new IllegalStateException(
          "No rounds found for session " + sessionId + ". Cannot continue with --skip-round.");
    }

    RoundState selectedRound = selectRound(rounds);
    roundStatusesByNo = toRoundStatusMap(rounds);
    activeRoundNo = selectedRound.roundNo();
    activeRoundSpec = fetchRoundSpec(activeRoundNo);
    logger.info(
        "Using round {} with status {}",
        activeRoundNo,
        selectedRound.status());
  }

  private RoundState selectRound(List<RoundState> rounds) {
    if (requestedRoundNo != null) {
      return rounds.stream()
          .filter(round -> round.roundNo() == requestedRoundNo)
          .findFirst()
          .orElseThrow(
              () ->
                  new IllegalStateException(
                      "Round "
                          + requestedRoundNo
                          + " does not exist in session "
                          + sessionId));
    }

    return rounds.stream()
        .filter(round -> "OPEN".equalsIgnoreCase(round.status()))
        .max(Comparator.comparingInt(RoundState::roundNo))
        .orElseGet(
            () ->
                rounds.stream()
                    .max(Comparator.comparingInt(RoundState::roundNo))
                    .orElseThrow());
  }

  private List<TeamState> resolveTeams() throws Exception {
    List<TeamState> existingTeams = fetchTeams();
    if (skipTeams) {
      if (existingTeams.isEmpty()) {
        throw new IllegalStateException(
            "No teams found for session " + sessionId + ". Cannot continue with --skip-teams.");
      }
      logger.info("Using {} existing teams", existingTeams.size());
      return existingTeams;
    }

    printHeader("Registering Teams");

    Map<String, TeamState> existingByName =
        existingTeams.stream()
            .collect(Collectors.toMap(TeamState::name, team -> team, (left, right) -> left));

    List<TeamState> managedTeams = new ArrayList<>();

    for (int index = 0; index < teamCount; index++) {
      TeamConfig config = createTeamConfig(index);
      TeamState existing = existingByName.get(config.name());
      if (existing != null) {
        logger.info(
            "[{}/{}] Reusing team '{}' ({})",
            index + 1,
            teamCount,
            existing.name(),
            existing.teamId());
        managedTeams.add(existing);
        continue;
      }

      UUID teamId = registerTeam(config);
      TeamState created = new TeamState(teamId, config.name());
      managedTeams.add(created);
      logger.info("[{}/{}] Registered '{}' ({})", index + 1, teamCount, config.name(), teamId);
    }

    return managedTeams;
  }

  private TeamConfig createTeamConfig(int index) {
    String name =
        index < DEFAULT_TEAM_NAMES.size()
            ? DEFAULT_TEAM_NAMES.get(index)
            : faker.team().name() + " " + (index + 1);
    int memberCount = faker.number().numberBetween(2, 6);
    List<String> members = generateUniqueMembers(memberCount);
    return new TeamConfig(name, members);
  }

  private UUID registerTeam(TeamConfig config) throws Exception {
    String encodedName = URLEncoder.encode(config.name(), StandardCharsets.UTF_8);
    String encodedMembers =
        config.members().stream()
            .map(member -> URLEncoder.encode(member, StandardCharsets.UTF_8))
            .collect(Collectors.joining(","));
    String url =
        String.format(
            "%s/api/teams/register?name=%s&members=%s&sessionId=%s",
            baseUrl,
            encodedName,
            encodedMembers,
            sessionId);

    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(URI.create(url))
            .POST(HttpRequest.BodyPublishers.noBody())
            .build();

    HttpResponse<String> response = sendExpecting(request, 200, "Failed to register team");
    return objectMapper.readValue(response.body(), UUID.class);
  }

  private List<String> generateUniqueMembers(int count) {
    Set<String> members = new LinkedHashSet<>();
    while (members.size() < count) {
      members.add(faker.name().firstName() + " " + faker.name().lastName());
    }
    return List.copyOf(members);
  }

  private void submitSquads(List<TeamState> teams) throws Exception {
    if (!"OPEN".equalsIgnoreCase(activeRoundSpec.status())) {
      throw new IllegalStateException(
          "Round " + activeRoundNo + " is not OPEN. Cannot submit squads.");
    }

    printHeader("Submitting Squads");

    List<HeroProfile> heroes = loadHeroes();
    Set<Integer> globallyUsedHeroIds = new HashSet<>();

    for (int index = 0; index < teams.size(); index++) {
      TeamState team = teams.get(index);
      Optional<SubmissionState> existingSubmission = getSubmission(team.teamId());
      if (existingSubmission.isPresent()) {
        globallyUsedHeroIds.addAll(existingSubmission.get().heroIds());
        logger.info(
            "[{}/{}] Team '{}' already has a submission. Reusing it.",
            index + 1,
            teams.size(),
            team.name());
        continue;
      }

      String strategy = STRATEGIES.get(index % STRATEGIES.size());
      List<Integer> heroIds =
          buildRosterForTeam(team, heroes, globallyUsedHeroIds)
              .stream()
              .map(HeroProfile::id)
              .toList();

      submitRoster(team.teamId(), heroIds, strategy);
      globallyUsedHeroIds.addAll(heroIds);
      logger.info(
          "[{}/{}] Submitted {} heroes for '{}' using {}",
          index + 1,
          teams.size(),
          heroIds.size(),
          team.name(),
          strategy);
    }
  }

  private List<HeroProfile> buildRosterForTeam(
      TeamState team,
      List<HeroProfile> heroes,
      Set<Integer> globallyUsedHeroIds) {
    List<HeroProfile> eligibleHeroes = filterEligibleHeroes(heroes);
    if (eligibleHeroes.size() < activeRoundSpec.teamSize()) {
      throw new IllegalStateException(
          "Only "
              + eligibleHeroes.size()
              + " heroes satisfy the round constraints, but "
              + activeRoundSpec.teamSize()
              + " are required.");
    }

    List<HeroProfile> uniquePool =
        eligibleHeroes.stream()
            .filter(hero -> allowHeroReuse || !globallyUsedHeroIds.contains(hero.id()))
            .toList();

    Optional<List<HeroProfile>> uniqueRoster = findValidRoster(uniquePool, team.name());
    if (uniqueRoster.isPresent()) {
      return uniqueRoster.get();
    }

    if (!allowHeroReuse) {
      logger.warn(
          "No unique roster found for '{}'. Retrying with cross-team hero reuse.",
          team.name());
    }

    return findValidRoster(eligibleHeroes, team.name())
        .orElseThrow(
            () ->
                new IllegalStateException(
                    "Could not find a valid roster for team '" + team.name() + "'."));
  }

  private List<HeroProfile> filterEligibleHeroes(List<HeroProfile> heroes) {
    return heroes.stream()
        .filter(hero -> matchesAllowed(activeRoundSpec.allowedRoles(), hero.role()))
        .filter(hero -> matchesAllowed(activeRoundSpec.allowedGenders(), hero.gender()))
        .filter(hero -> matchesAllowed(activeRoundSpec.allowedRaces(), hero.race()))
        .filter(hero -> matchesAllowed(activeRoundSpec.allowedPublishers(), hero.publisher()))
        .filter(hero -> matchesAllowed(activeRoundSpec.allowedAlignments(), hero.alignment()))
        .filter(
            hero ->
                hero.tags().stream()
                    .noneMatch(tag -> activeRoundSpec.bannedTags().contains(normalize(tag))))
        .sorted(Comparator.comparingInt(HeroProfile::cost).thenComparing(HeroProfile::name))
        .toList();
  }

  private boolean matchesAllowed(Set<String> allowedValues, String actualValue) {
    return allowedValues.isEmpty() || allowedValues.contains(normalize(actualValue));
  }

  private Optional<List<HeroProfile>> findValidRoster(List<HeroProfile> candidates, String teamName) {
    if (candidates.size() < activeRoundSpec.teamSize()) {
      return Optional.empty();
    }

    List<HeroProfile> shuffledCandidates = new ArrayList<>(candidates);
    Collections.shuffle(shuffledCandidates);

    logger.debug(
        "Trying to build roster for '{}' from {} eligible heroes",
        teamName,
        shuffledCandidates.size());

    return findValidRoster(
        shuffledCandidates,
        0,
        new ArrayList<>(),
        new HashMap<>(),
        0);
  }

  private Optional<List<HeroProfile>> findValidRoster(
      List<HeroProfile> candidates,
      int startIndex,
      List<HeroProfile> selectedHeroes,
      Map<String, Integer> roleCounts,
      int totalCost) {
    if (selectedHeroes.size() == activeRoundSpec.teamSize()) {
      return satisfiesRequiredRoles(roleCounts)
          ? Optional.of(List.copyOf(selectedHeroes))
          : Optional.empty();
    }

    for (int index = startIndex; index < candidates.size(); index++) {
      HeroProfile candidate = candidates.get(index);
      int updatedCost = totalCost + candidate.cost();
      if (updatedCost > activeRoundSpec.budgetCap()) {
        continue;
      }

      String normalizedRole = normalize(candidate.role());
      int updatedRoleCount = roleCounts.getOrDefault(normalizedRole, 0) + 1;
      int maxSameRole = activeRoundSpec.maxSameRole().getOrDefault(normalizedRole, Integer.MAX_VALUE);
      if (updatedRoleCount > maxSameRole) {
        continue;
      }

      selectedHeroes.add(candidate);
      roleCounts.put(normalizedRole, updatedRoleCount);

      Optional<List<HeroProfile>> result =
          findValidRoster(candidates, index + 1, selectedHeroes, roleCounts, updatedCost);
      if (result.isPresent()) {
        return result;
      }

      selectedHeroes.remove(selectedHeroes.size() - 1);
      if (updatedRoleCount == 1) {
        roleCounts.remove(normalizedRole);
      } else {
        roleCounts.put(normalizedRole, updatedRoleCount - 1);
      }
    }

    return Optional.empty();
  }

  private boolean satisfiesRequiredRoles(Map<String, Integer> roleCounts) {
    return activeRoundSpec.requiredRoles().entrySet().stream()
        .allMatch(
            entry -> roleCounts.getOrDefault(entry.getKey(), 0) >= entry.getValue());
  }

  private void submitRoster(UUID teamId, List<Integer> heroIds, String strategy) throws Exception {
    String url = baseUrl + "/api/rounds/" + activeRoundNo + "/submit?teamId=" + teamId;
    Map<String, Object> requestBody = Map.of("heroIds", heroIds, "strategy", strategy);

    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(requestBody)))
            .build();

    sendExpecting(request, 200, "Failed to submit roster for team " + teamId);
  }

  private void autoMatch(List<TeamState> teams) throws Exception {
    printHeader("Auto-Matching Teams");

    if (teams.size() < 2) {
      logger.warn("Need at least two teams to auto-match.");
      return;
    }

    String url =
        String.format(
            "%s/api/admin/matches/auto-match?sessionId=%s&roundNo=%d",
            baseUrl,
            sessionId,
            activeRoundNo);

    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Authorization", authHeader)
            .POST(HttpRequest.BodyPublishers.noBody())
            .build();

    HttpResponse<String> response = sendExpecting(request, 200, "Failed to auto-match teams");
    List<UUID> matchIds = objectMapper.readValue(response.body(), new TypeReference<>() {});
    logger.info("Auto-match returned {} match(es)", matchIds.size());
  }

  private void runAllBattles() throws Exception {
    printHeader("Running Battles");

    String url =
        String.format(
            "%s/api/admin/matches/run-all?roundNo=%d&sessionId=%s",
            baseUrl,
            activeRoundNo,
            sessionId);

    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Authorization", authHeader)
            .POST(HttpRequest.BodyPublishers.noBody())
            .build();

    HttpResponse<String> response = sendExpecting(request, 200, "Failed to run all battles");
    JsonNode result = objectMapper.readTree(response.body());
    refreshRoundState();
    logger.info(
        "Simulated {} of {} match(es)",
        result.path("successfulSimulations").asInt(),
        result.path("totalMatches").asInt());
  }

  private List<HeroProfile> loadHeroes() throws Exception {
    Optional<List<HeroProfile>> heroesFromApi = loadHeroesFromApi();
    if (heroesFromApi.isPresent() && !heroesFromApi.get().isEmpty()) {
      logger.info("Loaded {} heroes from /api/teams/heroes", heroesFromApi.get().size());
      return heroesFromApi.get();
    }

    List<HeroProfile> heroesFromFile = loadHeroesFromFile();
    logger.info("Loaded {} heroes from {}", heroesFromFile.size(), heroesFile);
    return heroesFromFile;
  }

  private Optional<List<HeroProfile>> loadHeroesFromApi() throws Exception {
    HttpRequest request =
        HttpRequest.newBuilder().uri(URI.create(baseUrl + "/api/teams/heroes")).GET().build();
    HttpResponse<String> response =
        sendWithLogging(request, Set.of(200, 401, 403, 404, 429, 500, 502, 503, 504));
    if (response.statusCode() != 200) {
      logger.warn(
          "Falling back to {} because /api/teams/heroes returned status {}",
          heroesFile,
          response.statusCode());
      return Optional.empty();
    }

    JsonNode body = objectMapper.readTree(response.body());
    if (!body.isArray()) {
      logger.warn("Falling back to {} because /api/teams/heroes returned a non-array payload", heroesFile);
      return Optional.empty();
    }

    List<HeroProfile> heroes = new ArrayList<>();
    for (JsonNode heroNode : body) {
      heroes.add(readHero(heroNode));
    }
    return Optional.of(List.copyOf(heroes));
  }

  private List<HeroProfile> loadHeroesFromFile() throws IOException {
    JsonNode body = objectMapper.readTree(new File(heroesFile));
    List<HeroProfile> heroes = new ArrayList<>();
    for (JsonNode heroNode : body) {
      heroes.add(readHero(heroNode));
    }
    return List.copyOf(heroes);
  }

  private HeroProfile readHero(JsonNode heroNode) {
    int id = heroNode.path("id").asInt();
    String name = heroNode.path("name").asText("hero-" + id);
    String role = heroNode.path("role").asText("Fighter");
    int cost = heroNode.path("cost").asInt(10);
    String gender = textValue(heroNode.path("appearance").path("gender"));
    String race = textValue(heroNode.path("appearance").path("race"));
    String publisher =
        firstNonBlank(
            textValue(heroNode.path("publisher")),
            textValue(heroNode.path("biography").path("publisher")));
    String alignment =
        firstNonBlank(
            textValue(heroNode.path("alignment")),
            textValue(heroNode.path("biography").path("alignment")));
    List<String> tags = new ArrayList<>();
    if (heroNode.path("tags").isArray()) {
      heroNode.path("tags").forEach(tagNode -> tags.add(tagNode.asText()));
    }

    return new HeroProfile(id, name, role, cost, gender, race, publisher, alignment, List.copyOf(tags));
  }

  private String textValue(JsonNode node) {
    if (node == null || node.isMissingNode() || node.isNull()) {
      return null;
    }
    String value = node.asText();
    return value == null || value.isBlank() ? null : value;
  }

  private String firstNonBlank(String first, String second) {
    return first != null && !first.isBlank() ? first : second;
  }

  private Optional<SubmissionState> getSubmission(UUID teamId) throws Exception {
    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(
                URI.create(
                    baseUrl + "/api/rounds/" + activeRoundNo + "/submission?teamId=" + teamId))
            .GET()
            .build();

    HttpResponse<String> response = sendWithLogging(request, Set.of(200, 404));
    if (response.statusCode() == 404) {
      logger.debug("No existing submission for team {} in round {}", teamId, activeRoundNo);
      return Optional.empty();
    }
    if (response.statusCode() != 200) {
      throw failure(
          "Failed to retrieve submission for team " + teamId,
          response.statusCode(),
          response.body());
    }

    JsonNode body = objectMapper.readTree(response.body());
    List<Integer> heroIds = new ArrayList<>();
    body.path("heroIds").forEach(node -> heroIds.add(node.asInt()));
    return Optional.of(new SubmissionState(heroIds));
  }

  private List<TeamState> fetchTeams() throws Exception {
    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(URI.create(baseUrl + "/api/teams?sessionId=" + sessionId))
            .GET()
            .build();

    HttpResponse<String> response = sendExpecting(request, 200, "Failed to list teams");
    JsonNode body = objectMapper.readTree(response.body());
    List<TeamState> teams = new ArrayList<>();
    for (JsonNode teamNode : body) {
      teams.add(
          new TeamState(
              UUID.fromString(teamNode.path("teamId").asText()),
              teamNode.path("name").asText()));
    }
    return List.copyOf(teams);
  }

  private List<RoundState> listRounds() throws Exception {
    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(URI.create(baseUrl + "/api/rounds?sessionId=" + sessionId))
            .GET()
            .build();

    HttpResponse<String> response = sendExpecting(request, 200, "Failed to list rounds");
    JsonNode body = objectMapper.readTree(response.body());
    List<RoundState> rounds = new ArrayList<>();
    for (JsonNode roundNode : body) {
      rounds.add(
          new RoundState(
              roundNode.path("roundNo").asInt(),
              roundNode.path("status").asText("UNKNOWN")));
    }
    List<RoundState> immutableRounds = List.copyOf(rounds);
    roundStatusesByNo = toRoundStatusMap(immutableRounds);
    return immutableRounds;
  }

  private RoundSpecState fetchRoundSpec(int roundNo) throws Exception {
    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(URI.create(baseUrl + "/api/rounds/" + roundNo + "?sessionId=" + sessionId))
            .GET()
            .build();

    HttpResponse<String> response =
        sendExpecting(request, 200, "Failed to retrieve round spec for round " + roundNo);
    JsonNode body = objectMapper.readTree(response.body());

    return new RoundSpecState(
        roundNo,
        resolveRoundStatus(roundNo),
        body.path("teamSize").asInt(DEFAULT_TEAM_SIZE),
        body.path("budgetCap").asInt(DEFAULT_BUDGET_CAP),
        normalizedSet(body.path("allowedRoles")),
        normalizedSet(body.path("allowedGenders")),
        normalizedSet(body.path("allowedRaces")),
        normalizedSet(body.path("allowedPublishers")),
        normalizedSet(body.path("allowedAlignments")),
        normalizedSet(body.path("bannedTags")),
        normalizedIntMap(body.path("requiredRoles")),
        normalizedIntMap(body.path("maxSameRole")));
  }

  private String resolveRoundStatus(int roundNo) throws Exception {
    if (roundStatusesByNo.containsKey(roundNo)) {
      return roundStatusesByNo.get(roundNo);
    }
    listRounds();
    return roundStatusesByNo.getOrDefault(roundNo, "UNKNOWN");
  }

  private void refreshRoundState() throws Exception {
    listRounds();
    activeRoundSpec = fetchRoundSpec(activeRoundNo);
  }

  private Map<Integer, String> toRoundStatusMap(List<RoundState> rounds) {
    return rounds.stream()
        .collect(Collectors.toUnmodifiableMap(RoundState::roundNo, RoundState::status, (left, right) -> right));
  }

  private Set<String> normalizedSet(JsonNode node) {
    if (node == null || !node.isArray()) {
      return Set.of();
    }
    Set<String> values = new LinkedHashSet<>();
    node.forEach(item -> values.add(normalize(item.asText())));
    return Set.copyOf(values);
  }

  private Map<String, Integer> normalizedIntMap(JsonNode node) {
    if (node == null || !node.isObject()) {
      return Map.of();
    }
    Map<String, Integer> values = new LinkedHashMap<>();
    node.fields().forEachRemaining(entry -> values.put(normalize(entry.getKey()), entry.getValue().asInt()));
    return Map.copyOf(values);
  }

  private List<String> normalizedValues(List<String> values) {
    return values == null ? List.of() : values.stream().filter(value -> value != null && !value.isBlank()).toList();
  }

  private String normalize(String value) {
    return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
  }

  private HttpResponse<String> sendExpecting(HttpRequest request, int expectedStatus, String errorMessage)
      throws Exception {
    HttpResponse<String> response = sendWithLogging(request, Set.of(expectedStatus));
    if (response.statusCode() != expectedStatus) {
      throw failure(errorMessage, response.statusCode(), response.body());
    }
    return response;
  }

  private HttpResponse<String> sendWithLogging(HttpRequest request, Set<Integer> expectedStatuses)
      throws Exception {
    try {
      HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
      int statusCode = response.statusCode();
      if (expectedStatuses.contains(statusCode)) {
        logger.debug("HTTP {} {} -> {}", request.method(), request.uri(), statusCode);
      } else if (statusCode >= 500) {
        logger.error(
            "HTTP {} {} -> {} {}",
            request.method(),
            request.uri(),
            statusCode,
            formatProblem(response.body()));
      } else if (statusCode >= 400) {
        logger.warn(
            "HTTP {} {} -> {} {}",
            request.method(),
            request.uri(),
            statusCode,
            formatProblem(response.body()));
      } else {
        logger.debug("HTTP {} {} -> {}", request.method(), request.uri(), statusCode);
      }
      return response;
    } catch (HttpTimeoutException exception) {
      logger.error(
          "HTTP {} {} timed out after {}",
          request.method(),
          request.uri(),
          request.timeout().orElse(Duration.ofSeconds(10)));
      throw exception;
    } catch (Exception exception) {
      logger.error(
          "HTTP {} {} failed: {}",
          request.method(),
          request.uri(),
          exception.getMessage());
      throw exception;
    }
  }

  private RuntimeException failure(String message, int statusCode, String body) {
    return new IllegalStateException(message + " (status=" + statusCode + ", body=" + formatProblem(body) + ")");
  }

  private String formatProblem(String body) {
    if (body == null || body.isBlank()) {
      return "<empty>";
    }
    try {
      JsonNode json = objectMapper.readTree(body);
      if (json.hasNonNull("detail")) {
        return json.path("title").asText("Problem") + ": " + json.path("detail").asText();
      }
    } catch (Exception ignored) {
      return abbreviate(body);
    }
    return abbreviate(body);
  }

  private String abbreviate(String value) {
    return value.length() <= 240 ? value : value.substring(0, 240) + "...";
  }

  private void printHeader(String message) {
    logger.info("=========================================");
    logger.info(message);
    logger.info("=========================================");
  }

  private void printSuccess(List<TeamState> teams) {
    printHeader("Fixture Data Initialized Successfully");
    logger.info("Session ID: {}", sessionId);
    logger.info("Round: {}", activeRoundNo);
    logger.info("Round Status: {}", activeRoundSpec.status());
    logger.info("Teams Targeted: {}", teams.size());
  }

  private record TeamConfig(String name, List<String> members) {}

  private record TeamState(UUID teamId, String name) {}

  private record RoundState(int roundNo, String status) {}

  private record SubmissionState(List<Integer> heroIds) {}

  private record RoundSpecState(
      int roundNo,
      String status,
      int teamSize,
      int budgetCap,
      Set<String> allowedRoles,
      Set<String> allowedGenders,
      Set<String> allowedRaces,
      Set<String> allowedPublishers,
      Set<String> allowedAlignments,
      Set<String> bannedTags,
      Map<String, Integer> requiredRoles,
      Map<String, Integer> maxSameRole) {}

  private record HeroProfile(
      int id,
      String name,
      String role,
      int cost,
      String gender,
      String race,
      String publisher,
      String alignment,
      List<String> tags) {}
}
