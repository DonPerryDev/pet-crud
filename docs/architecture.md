# Architecture

## Overview

```
┌─────────────────────────────────────────────────────────┐
│                    applications/pet                      │
│              (Bootstrap, Config, Wiring)                 │
├─────────────────────────────────────────────────────────┤
│                                                         │
│  ┌─────────────────┐  ┌──────────────┐  ┌────────────┐ │
│  │  entrypoint-rest │  │  postgres-db │  │ client-rest│ │
│  │  (Router,Handler)│  │  (R2DBC)     │  │ (WebClient)│ │
│  └────────┬─────────┘  └──────┬───────┘  └─────┬──────┘ │
│           │                   │                 │        │
│           ▼                   ▼                 ▼        │
│  ┌─────────────────────────────────────────────────────┐ │
│  │                  domain/usecase                      │ │
│  │              (Business Logic)                        │ │
│  └──────────────────────┬──────────────────────────────┘ │
│                         │                                │
│                         ▼                                │
│  ┌─────────────────────────────────────────────────────┐ │
│  │                  domain/model                        │ │
│  │        (Entities, Enums, Gateway Interfaces)         │ │
│  └─────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────┘
```

## Module Map

| Module | Gradle Name | Path | Package | Purpose |
|--------|-------------|------|---------|---------|
| Domain Model | `:model` | `domain/model/` | `com.donperry.model` | Data classes, enums, gateway interfaces |
| Domain UseCase | `:usecase` | `domain/usecase/` | `com.donperry.usecase` | Business logic orchestration |
| REST Entrypoint | `:rest` | `infrastructure/entrypoint-rest/` | `com.donperry.rest` | WebFlux routers, handlers, DTOs, security config |
| Persistence | `:persistence` | `infrastructure/postgres-db/` | `com.donperry.persistence` | R2DBC adapters, entities, mappers |
| REST Client | `:client-rest` | `infrastructure/client-rest/` | `com.donperry.client.rest` | WebClient adapters for external APIs |
| Application | `:pet` | `applications/pet/` | `com.donperry.app` | Spring Boot app, wiring config |

## Dependency Flow

```
:pet ──→ :rest ──→ :usecase ──→ :model
  │                                ▲
  ├──→ :persistence ───────────────┘
  │                                │
  └──→ :client-rest ───────────────┘
```

- `:model` depends on nothing (pure Kotlin + Reactor)
- `:usecase` depends on `:model`
- `:rest` depends on `:model` + `:usecase`
- `:persistence` depends on `:model`
- `:client-rest` depends on `:model`
- `:pet` depends on all modules (wiring only)

## Key Entry Points

| File | Path | Purpose |
|------|------|---------|
| `StartApp.kt` | `applications/pet/src/main/kotlin/com/donperry/app/` | Spring Boot main class |
| `UseCasesConfig.kt` | `applications/pet/src/main/kotlin/com/donperry/app/configuration/` | ComponentScan for use cases |
| `JacksonConfig.kt` | `applications/pet/src/main/kotlin/com/donperry/app/configuration/` | Jackson serialization config |
| `application.yaml` | `applications/pet/src/main/resources/` | App config (R2DBC, CORS, actuator) |
| `docker-compose.yml` | `applications/pet/` | PostgreSQL + app containers |
| `PetRouter.kt` | `infrastructure/entrypoint-rest/src/main/kotlin/com/donperry/rest/pet/` | Pet API route definitions |
| `CorsGlobalConfig.kt` | `infrastructure/entrypoint-rest/src/main/kotlin/com/donperry/rest/security/` | CORS configuration |

## Tech Stack

| Component | Version | Notes |
|-----------|---------|-------|
| Kotlin | 2.1.10 | JVM target 21, `-Xjsr305=strict` |
| Java | 21 | Toolchain |
| Spring Boot | 3.4.3 | WebFlux (no Tomcat) |
| Spring WebFlux | (managed) | Functional router pattern |
| R2DBC | (managed) | Reactive database access |
| PostgreSQL | 15 | Via docker-compose |
| Reactor Kotlin Extensions | (managed) | Kotlin extensions for Reactor |
| Jackson Kotlin Module | (managed) | Kotlin-aware JSON serialization |
| SendGrid | 4.10.3 | Email integration (client-rest) |
| JUnit 5 | (managed) | Test framework |
| Mockito + Mockito-Kotlin | 5.8.0 / 5.2.1 | Mocking |
| JaCoCo | 0.8.12 | Code coverage |
