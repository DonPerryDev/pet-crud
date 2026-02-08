# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Kotlin Spring WebFlux application built using hexagonal architecture principles. The project uses a multi-module Gradle structure with clean separation between domain, infrastructure, and application layers.

## Architecture

The project follows hexagonal architecture with these modules:

### Domain Layer
- **model** (`/domain/model`) - Core business entities and gateway interfaces
- **usecase** (`/domain/usecase`) - Business logic and use cases

### Infrastructure Layer  
- **rest** (`/infrastructure/entrypoint-rest`) - REST API controllers and DTOs
- **persistence** (`/infrastructure/postgres-db`) - Database persistence with R2DBC
- **client-rest** (`/infrastructure/client-rest`) - External REST client adapters

### Application Layer
- **pet** (`/applications/pet`) - Main Spring Boot application that wires everything together

## Development Commands

### Build and Run
```bash
# Clean build all modules
./gradlew clean build

# Run the application
./gradlew :pet:bootRun

# Run with profile
gradle :pet:bootRun
```

### Testing
```bash
# Run all tests
./gradlew test

# Run tests with merged coverage report
./gradlew testWithMergedCoverage

# Run tests for specific module
./gradlew :model:test
./gradlew :persistence:test
```

### Docker
```bash
# Run with Docker Compose (includes PostgreSQL)
cd applications/pet
docker-compose up --build
```

## Technical Stack

- **Language**: Kotlin 2.1.10
- **Framework**: Spring Boot 3.4.3 with WebFlux (reactive)
- **Database**: PostgreSQL with R2DBC (reactive database connectivity)
- **Testing**: JUnit 5 with Kotlin test extensions
- **Build**: Gradle with Java 21 toolchain
- **Code Coverage**: JaCoCo with merged reporting

## Key Configuration

### Database Connection
- **Development**: R2DBC connection via Docker network (`postgres:5432`)
- **Docker**: PostgreSQL container with database `local`, schema `petapp`
- **Credentials**: `petapp/petapp123` (development only)

### Application Structure
- Main class: `com.donperry.app.StartAppKt` in `:pet` module
- REST endpoints defined in `PetRouter.kt` using functional routing
- Database entities in `persistence` module with R2DBC repositories
- Business logic in `usecase` module, pure Kotlin without Spring dependencies

## Module Dependencies
- `:pet` depends on all other modules and contains Spring Boot configuration
- `:usecase` and `:model` are pure Kotlin modules without Spring dependencies
- Infrastructure modules (`:rest`, `:persistence`, `:client-rest`) depend on domain modules
- Only the `:pet` application module produces a bootable JAR

## Testing

### Test Commands
```bash
# Run all tests
./gradlew test

# Run tests with merged coverage report
./gradlew testWithMergedCoverage

# Run tests for specific module
./gradlew :model:test
./gradlew :usecase:test
./gradlew :rest:test
./gradlew :persistence:test

# Generate coverage reports
./gradlew jacocoTestReport           # Individual module reports
./gradlew jacocoMergedReport         # Combined coverage report
```

### Testing Architecture
- **Pure Unit Tests** - No Spring context required
- **JUnit 5** with Kotlin test extensions
- **Mockito** with Kotlin support for mocking
- **StepVerifier** for reactive stream testing
- **WebTestClient** for router function testing
- **Coverage Reports** - JaCoCo with merged reporting

### Test Structure by Layer
```
1. Entrypoint Tests (Router)    - Routing, content type validation
2. Handler Tests               - Request/response, error scenarios  
3. Use Case Tests              - Business logic, pure unit tests
4. Adapter Tests               - Persistence, data transformation
5. Model/DTO Tests             - Domain models, data classes
```

## Development Notes
- The application uses reactive programming with Reactor and Kotlin coroutines
- Database operations are non-blocking using R2DBC
- Clean architecture enforced through module boundaries
- JaCoCo coverage reports generated automatically on build
- All tests are isolated unit tests without Spring Boot context