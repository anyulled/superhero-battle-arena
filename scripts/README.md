# JBang Scripts

This directory contains JBang scripts for managing the Superhero Battle Arena.

## Prerequisites

- [JBang](https://www.jbang.dev/) installed
- Application running on `http://localhost:8080` (or specify custom URL)

## Scripts

### InitFixture.java

Initializes or advances fixture data for the Superhero Battle Arena using [picocli](https://picocli.info/) for a richer command-line flow.

**Features:**

- Creates or reuses a session and round
- Registers or reuses teams for the selected session
- Builds squad submissions from the live `/api/teams/heroes` endpoint and falls back to the JSON file when needed
- Filters hero picks against the current round constraints before submitting squads
- Can continue the admin flow through auto-match and `run-all`
- Supports partial reruns by skipping session, round, teams, squads, matchmaking, or battles

**Usage:**

```bash
# Show help
jbang scripts/InitFixture.java --help

# Use the full default flow (create session, create round, register teams,
# submit squads, auto-match, run battles)
jbang scripts/InitFixture.java

# Reuse the active session and round, only refresh teams and squads
jbang scripts/InitFixture.java --skip-session --skip-round --skip-matchmaking --skip-battles

# Drive a constrained round
jbang scripts/InitFixture.java \
  --allowed-role Fighter \
  --allowed-gender Male \
  --allowed-race Human

# Reuse an existing round and only run the admin phase
jbang scripts/InitFixture.java \
  --skip-session \
  --skip-round \
  --skip-teams \
  --skip-squads

# Use a specific heroes file
jbang scripts/InitFixture.java -f path/to/heroes.json
```

**Options:**

- `-s, --session-id`: Specific session ID (UUID)
- `-r, --round-no`: Existing round number to target when `--skip-round` is used
- `-u, --url`: Base URL of the application (default: <http://localhost:8080>)
- `-f, --file`: Path to the heroes JSON file (default: src/main/resources/all-superheroes.json)
- `--reset`: Reset tournament data before starting
- `--skip-session`: Skip session initialization (verifies existing session)
- `--skip-round`: Skip round creation and reuse an existing round
- `--skip-teams`: Skip team registration and reuse existing teams
- `--skip-squads`: Skip squad formations
- `--skip-matchmaking`: Skip admin auto-match
- `--skip-battles`: Skip admin `run-all`
- `--allow-hero-reuse`: Allow heroes to be reused across teams
- `--team-size`, `--budget-cap`, `--map-type`, `--round-description`: Configure the created round
- `--allowed-role`, `--allowed-gender`, `--allowed-race`, `--allowed-publisher`, `--allowed-alignment`, `--banned-tag`: Configure round filters for the created round

### GenerateSuperheroSql.java

Generates SQL seed data from the `all-superheroes.json` file. This script populates the `superheroes`, `superhero_powerstats`, `superhero_appearance`, `superhero_biography`, and `superhero_images` tables. It generates separate files for H2 and PostgreSQL dialects.

**Usage:**

```bash
jbang scripts/GenerateSuperheroSql.java
```

**Features:**

- Parses complex JSON hero data using Jackson
- Calculates hero "cost" based on power stats
- Handles SQL escaping and null values for database compatibility
- Implements modern Java 21 `Math.clamp` for stat normalization
- Supports `ON CONFLICT` for PostgreSQL and standard inserts for H2

### ExtractHeroes.java

Extracts hero IDs from the all-superheroes.json file.

**Usage:**

```bash
jbang scripts/ExtractHeroes.java <json-file> <count>

# Example
jbang scripts/ExtractHeroes.java src/main/resources/all-superheroes.json 100
```

**Features:**

- Immutable, functional programming style
- Outputs space-separated hero IDs for shell script compatibility
- Error handling and validation

## Design Principles

The scripts follow these principles:

- **Immutability**: Use `final` fields, immutable collections, and records
- **Functional Style**: Stream API, method references, and pure functions
- **Type Safety**: Strong typing with enums and records
- **Error Handling**: Proper exception handling and user feedback
- **Dependency Management**: JBang handles all dependencies automatically

## Dependencies

Dependencies are automatically managed by JBang:

- `com.fasterxml.jackson.core:jackson-databind` - JSON processing
- `com.github.javafaker:javafaker` - Fake data generation
- `info.picocli:picocli` - Command-line parsing
- `org.slf4j:slf4j-simple` - Logging
