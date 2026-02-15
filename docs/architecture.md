# Architecture

## Overview

```
┌─────────────────────────────────────────────────────────────┐
│                    applications/pet                          │
│              (Bootstrap, Config, Wiring)                     │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ┌──────────────┐  ┌───────────┐  ┌──────────┐  ┌────────┐ │
│  │entrypoint-rest│  │postgres-db│  │client-rest│  │s3-storage│
│  │(Router,Handler│  │  (R2DBC)  │  │(WebClient)│  │(AWS S3)│ │
│  │  Security)    │  │(Migrations)│  │           │  │        │ │
│  └──────┬───────┘  └─────┬─────┘  └─────┬─────┘  └───┬────┘ │
│         │                │               │             │      │
│         ▼                ▼               ▼             ▼      │
│  ┌──────────────────────────────────────────────────────────┐ │
│  │                  domain/usecase                           │ │
│  │              (Business Logic)                             │ │
│  └──────────────────────┬───────────────────────────────────┘ │
│                         │                                     │
│                         ▼                                     │
│  ┌──────────────────────────────────────────────────────────┐ │
│  │                  domain/model                             │ │
│  │   (Entities, Enums, Exceptions, Gateway Interfaces)       │ │
│  └──────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
```

## Module Map

| Module | Gradle Name | Path | Package | Purpose |
|--------|-------------|------|---------|---------|
| Domain Model | `:model` | `domain/model/` | `com.donperry.model` | Data classes, enums, exceptions, gateway interfaces |
| Domain UseCase | `:usecase` | `domain/usecase/` | `com.donperry.usecase` | Business logic orchestration |
| REST Entrypoint | `:rest` | `infrastructure/entrypoint-rest/` | `com.donperry.rest` | WebFlux routers, handlers, DTOs, error responses |
| Persistence | `:persistence` | `infrastructure/postgres-db/` | `com.donperry.persistence` | R2DBC adapters, entities, mappers, migrations |
| REST Client | `:client-rest` | `infrastructure/client-rest/` | `com.donperry.client.rest` | WebClient adapters for external APIs |
| S3 Storage | `:s3-storage` | `infrastructure/s3-storage/` | `com.donperry.storage` | AWS S3 client config and photo storage adapter |
| Application | `:pet` | `applications/pet/` | `com.donperry.app` | Spring Boot app, wiring config, security config |

## Dependency Flow

```
:pet ──→ :rest ──────→ :usecase ──→ :model
  │                                    ▲
  ├──→ :persistence ───────────────────┘
  │                                    │
  ├──→ :client-rest ───────────────────┘
  │                                    │
  └──→ :s3-storage ────────────────────┘
```

- `:model` depends on nothing (pure Kotlin + Reactor)
- `:usecase` depends on `:model`
- `:rest` depends on `:model` + `:usecase`
- `:persistence` depends on `:model`
- `:client-rest` depends on `:model`
- `:s3-storage` depends on `:model`
- `:pet` depends on all modules (wiring only)

## Key Entry Points

| File | Path | Purpose |
|------|------|---------|
| `StartApp.kt` | `applications/pet/src/main/kotlin/com/donperry/app/` | Spring Boot main class |
| `SecurityConfig.kt` | `applications/pet/src/main/kotlin/com/donperry/app/configuration/` | JWT OAuth2 security config |
| `UseCasesConfig.kt` | `applications/pet/src/main/kotlin/com/donperry/app/configuration/` | ComponentScan for use cases |
| `JacksonConfig.kt` | `applications/pet/src/main/kotlin/com/donperry/app/configuration/` | Jackson serialization config |
| `application.yaml` | `applications/pet/src/main/resources/` | App config (R2DBC, JWT, AWS S3, CORS, actuator) |
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
| Spring Security | (managed) | OAuth2 Resource Server with JWT |
| R2DBC | (managed) | Reactive database access |
| PostgreSQL | 15 | Via docker-compose |
| AWS SDK for Java v2 | (managed) | S3 client for photo storage |
| Reactor Kotlin Extensions | (managed) | Kotlin extensions for Reactor |
| Jackson Kotlin Module | (managed) | Kotlin-aware JSON serialization |
| SendGrid | 4.10.3 | Email integration (client-rest) |
| JUnit 5 | (managed) | Test framework |
| Mockito + Mockito-Kotlin | 5.8.0 / 5.2.1 | Mocking |
| JaCoCo | 0.8.12 | Code coverage |
