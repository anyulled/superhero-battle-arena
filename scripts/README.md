# JBang Scripts

This directory contains JBang scripts for managing the Superhero Battle Arena.

## Prerequisites

- [JBang](https://www.jbang.dev/) installed
- Application running on `http://localhost:8080` (or specify custom URL)

## Scripts

### InitFixture.java

Initializes fixture data for the Superhero Battle Arena using [picocli](https://picocli.info/) for a rich command-line experience.

**Features:**

- Creates a session and round 1 automatically (if not skipped)
- Registers 20 teams with randomly generated member names using Java Faker
- Submits squad formations for all teams with rotating battle strategies (AGGRESSIVE, DEFENSIVE, BALANCED)
- Supports skipping specific lifecycle steps (session, round, teams, squads)
- Verifies existing state when steps are skipped

**Usage:**

```bash
# Show help
jbang scripts/InitFixture.java --help

# Use all defaults (random session, localhost:8080, default heroes file)
jbang scripts/InitFixture.java

# Specific session ID and custom base URL
jbang scripts/InitFixture.java -s my-session -u http://localhost:9090

# Skip session and round creation (assumes they already exist)
jbang scripts/InitFixture.java --skip-session --skip-round

# Use a specific heroes file
jbang scripts/InitFixture.java -f path/to/heroes.json
```

**Options:**

- `-s, --session-id`: Specific session ID (default: random UUID or latest existing if skipped)
- `-u, --url`: Base URL of the application (default: <http://localhost:8080>)
- `-f, --file`: Path to the heroes JSON file (default: src/main/resources/all-superheroes.json)
- `--skip-session`: Skip session initialization (verifies existing session)
- `--skip-teams`: Skip team registration (also skips squad formations)
- `--skip-round`: Skip round creation (verifies existing round 1)
- `--skip-squads`: Skip squad formations

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
