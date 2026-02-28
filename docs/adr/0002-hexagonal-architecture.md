# 2. Hexagonal Architecture

Date: 2026-03-01

## Status

Accepted

## Context

The Superhero Battle Arena project needs a structured architecture that isolates the core domain logic (hero battles, matchmaking, fatigue mechanics) from external delivery mechanisms (HTTP/web, Server-Sent Events) and infrastructure (databases, specifically PostgreSQL/H2).

## Decision

We have decided to implement a Hexagonal Architecture (Ports and Adapters).

- **Domain:** Pure Java representing facts and logic (e.g., `Hero`, `Match`). No framework annotations.
- **Application:** Orchestrates use cases. Defines outbound ports (interfaces).
- **Adapters:** Connects the application to external concerns. Separated into `in.web` (Controllers) and `out.persistence` (Spring Data repositories, DB Entities).

An explicit constraint is enforced: adapters can depend on the domain, but the domain can never depend on adapters. Aditionally, adapters cannot depend on other adapters (e.g., `in.web` cannot directly access `out.persistence`).

## Consequences

- **Positive:** High testability. The core domain and application layers can be tested thoroughly without spinning up database containers or web contexts.
- **Positive:** Tech stack flexibility. We can swap databases or web frameworks at the adapter level without touching the core logic.
- **Negative:** Increased boilerplate. We must implement mapping logic at the boundaries to translate between internal Domain models and external JPA Entities/Web DTOs.
- **Negative:** Steeper learning curve for developers unfamiliar with the pattern.
