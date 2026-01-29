# Jbang Scripts

This directory contains JBang scripts for managing the Superhero Battle Arena.

## Prerequisites

- [JBang](https://www.jbang.dev/) installed
- Application running on `http://localhost:8080` (or specify custom URL)

## Scripts

### InitFixture.java

Initializes fixture data for the Superhero Battle Arena with:

- 20 teams with randomly generated member names using Java Faker
- 5 heroes per team from the superhero database
- Rotating battle strategies (AGGRESSIVE, DEFENSIVE, BALANCED)

**Usage:**

```bash
# Use defaults (random session, localhost:8080, src/main/resources/all-superheroes.json)
jbang scripts/InitFixture.java

# Specific session ID
jbang scripts/InitFixture.java <session-id>

# Custom base URL (automatically generates a random session ID)
jbang scripts/InitFixture.java http://localhost:9090

# Specific session ID and custom base URL
jbang scripts/InitFixture.java <session-id> http://localhost:9090

# Custom base URL and heroes file (automatically generates a random session ID)
jbang scripts/InitFixture.java http://localhost:9090 path/to/heroes.json
```

**Features:**

- Immutable data structures using Java records
- Functional programming patterns
- Uses Java Faker for realistic name generation
- HTTP client with proper timeout configuration

+### GenerateSuperheroSql.java
+
+Generates SQL seed data from the `all-superheroes.json` file. This script populates the `superheroes`, `superhero_powerstats`, `superhero_appearance`, `superhero_biography`, and `superhero_images` tables.
+
+**Usage:**
+
+```bash
+jbang scripts/GenerateSuperheroSql.java
+```
+
+**Features:**
+
+- Parses complex JSON hero data using Jackson
+- Calculates hero "cost" based on power stats
+- Handled SQL escaping and null values for database compatibility
+- Implements modern Java 21 `Math.clamp` for stat normalization
+

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

Both scripts follow these principles:

- **Immutability**: Use `final` fields, immutable collections, and records
- **Functional Style**: Stream API, method references, and pure functions
- **Type Safety**: Strong typing with enums and records
- **Error Handling**: Proper exception handling and user feedback
- **Dependency Management**: JBang handles all dependencies automatically

## Dependencies

Dependencies are automatically managed by JBang:

- `com.fasterxml.jackson.core:jackson-databind:2.18.2` - JSON processing
- `com.github.javafaker:javafaker:1.0.2` - Fake data generation (InitFixture only)
- `org.slf4j:slf4j-nop:2.0.9` - Logging (InitFixture only)
