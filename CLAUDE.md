# CLAUDE.md

Pet registration and management API built with Kotlin, Spring WebFlux, and Clean Architecture.

## Architecture

| Layer | Module | Path | Description |
|-------|--------|------|-------------|
| Domain Model | `:model` | `domain/model/` | Data classes, enums, gateway interfaces (pure Kotlin) |
| Domain Use Cases | `:usecase` | `domain/usecase/` | Business logic, orchestrates gateways |
| REST Entrypoint | `:rest` | `infrastructure/entrypoint-rest/` | WebFlux routers, handlers, DTOs |
| Persistence | `:persistence` | `infrastructure/postgres-db/` | R2DBC adapters, entities, mappers |
| REST Client | `:client-rest` | `infrastructure/client-rest/` | WebClient adapters for external APIs |
| Application | `:pet` | `applications/pet/` | Spring Boot bootstrap, wiring configs |

## Commands

```bash
# Build
./gradlew clean build

# Run
./gradlew :pet:bootRun

# Test
./gradlew test

# Test specific module
./gradlew :model:test
./gradlew :usecase:test
./gradlew :rest:test
./gradlew :persistence:test
./gradlew :client-rest:test

# Coverage report (merged)
./gradlew testWithMergedCoverage

# Docker
docker compose -f applications/pet/docker-compose.yml up -d
```

## Tech Stack

Kotlin 2.1.10, Java 21, Spring Boot 3.4.3, WebFlux (reactive), R2DBC, PostgreSQL 15, JUnit 5, Mockito-Kotlin, JaCoCo
