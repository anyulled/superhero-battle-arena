# Superhero Battle Arena

![Superhero Battle Arena Hero Image](src/main/resources/static/images/hero-image.png)

A Spring Boot application built with Hexagonal Architecture that simulates battles between teams of superheroes.

## Features

- **Team Management**: Register your team of superheroes.
- **Matchmaking**: Create matches between two registered teams.
- **Battle Simulation**: Deterministic battle engine with turn-based combat mechanics.
- **Live Replay**: Watch battles unfold in real-time via Server-Sent Events (SSE) with visual animations.
- **Hexagonal Architecture**: Clean separation of concerns with Domain, Application, and Adapter layers.

## Technology Stack

- **Backend**: Java 25 (Preview Features), Spring Boot 3.4
- **Frontend**: HTML5, Tailwind CSS, jQuery
- **Architecture**: Hexagonal (Ports & Adapters)
- **Build Tool**: Maven

## How to Run

1. **Prerequisites**: Ensure you have Java 21+ installed.
2. **Build and Run**:

   ```bash
   ./mvnw spring-boot:run
   ```

3. **Access the Application**:
   Open your browser to [http://localhost:8080/lobby.html](http://localhost:8080/lobby.html)

## Architecture

### System Overview

```mermaid
graph TD
    User[User Browser]
    
    subgraph "Superhero Battle Arena"
        Web["Web Adapter\n(Controllers)"]
        App["Application Layer\n(Services/Use Cases)"]
        Domain["Domain Layer\n(Entities/Logic)"]
        Persist[Persistence Adapter]
    end
    
    User <-->|HTTP/REST/SSE| Web
    Web --> App
    App --> Domain
    App -->|Ports| Persist
```

### Battle Flow

The system flow is divided into three main phases: Registration, Matchmaking, and Battle.

#### Phase 1: Registration (Session & Teams)

```mermaid
sequenceDiagram
    participant U as User/Admin
    participant C as SessionController
    participant T as TeamController
    participant R as Repository

    U->>C: POST /api/sessions
    C->>R: Save New Session
    C-->>U: Session ID
    
    U->>T: POST /api/teams/register (with Session ID)
    T->>R: Save Team
    T-->>U: Team ID
```

#### Phase 2: Matchmaking & Round Setup

```mermaid
sequenceDiagram
    participant U as Admin
    participant RC as RoundController
    participant MC as MatchController
    participant R as Repository

    U->>RC: POST /api/rounds (Session ID, Round No)
    RC->>R: Save Round Config
    
    U->>MC: POST /api/matches/create (Team A, Team B)
    MC->>R: Create Pending Match
    MC-->>U: Match ID
```

#### Phase 3: Battle Simulation & Visualization

```mermaid
sequenceDiagram
    participant U as User
    participant F as Frontend
    participant MC as MatchController
    participant E as BattleEngine

    U->>F: Start/Watch Match
    F->>MC: POST /api/matches/{id}/run
    MC->>E: simulate(match)
    E-->>MC: Full Battle Result (Winner, Events)
    MC-->>F: Match Completed
    
    F->>MC: GET /api/matches/{id}/events/stream
    loop Replay
        MC-->>F: SSE Event (Attack, Damage)
        F->>F: Animate
    end
```

### Domain Model

```mermaid
classDiagram
    class Match {
        UUID id
        UUID teamA
        UUID teamB
        MatchStatus status
        List~MatchEvent~ events
    }
    
    class Team {
        UUID id
        String name
        List~String~ members
    }
    
    class Hero {
        int id
        String name
        PowerStats stats
        String role
    }
    
    class MatchEvent {
        String type
        int actorId
        int targetId
        int value
    }
    
    Match "1" *-- "*" MatchEvent : contains
    Match --> "2" Team : references
```

## Usage Guide

1. **Lobby**: Go to `/lobby.html`. Use the buttons to register default teams if none exist.
2. **Create Match**: Select two teams from the list and click "Create Match".
3. **Bracket**: You will be redirected to `/bracket.html`. Click "Watch Live/Replay" on your match.
4. **Battle**: Watch the simulation on `/battle.html`.

## Development

The project structure follows Hexagonal Architecture principles:

- `domain`: Pure business logic and entities. No framework dependencies.
- `application`: Use cases and input/output ports.
- `adapter`: Implementation of ports (Web, Persistence).
