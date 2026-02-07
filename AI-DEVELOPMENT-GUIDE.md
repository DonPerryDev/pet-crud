# AI Development Guide

## 🤖 For AI Agents, LLMs, and Code Assistants

This guide is designed for AI-powered development tools including Claude Code, Cursor, GitHub Copilot, CodeT5, Codex, and other code generation systems working with this Kotlin Spring Boot project.

---

## 📋 Project Overview

**Architecture**: Hexagonal Architecture (Ports & Adapters)  
**Language**: Kotlin 2.1.10  
**Framework**: Spring Boot 3.4.3 with WebFlux (Reactive)  
**Database**: PostgreSQL with R2DBC  
**Build System**: Gradle Multi-Module  
**Testing**: JUnit 5 + Kotlin Test  

---

## 🏗️ Architecture Patterns

### Module Structure
```
pet-app-kt/
├── domain/
│   ├── model/           # Business entities & gateway interfaces
│   └── usecase/         # Pure business logic (no Spring dependencies)
├── infrastructure/
│   ├── entrypoint-rest/ # REST controllers, handlers, routers
│   ├── postgres-db/     # Database adapters, repositories, entities
│   └── client-rest/     # External service clients
└── applications/
    └── pet/             # Main Spring Boot app (wires everything)
```

### Dependency Rules
- **Domain** depends on nothing
- **Infrastructure** depends on domain
- **Application** depends on all layers
- **Use Cases** are framework-agnostic
- **Only `:pet` module** produces bootable JAR

---

## 🚀 Scaffolding Templates

### 1. Creating a New Use Case

#### Step 1: Domain Model
```kotlin
// domain/model/src/main/kotlin/com/donperry/model/{entity}/{Entity}.kt
package com.donperry.model.{entity}

import java.time.LocalDate

data class {Entity}(
    val id: String? = null,
    val name: String,
    val description: String?,
    val createdAt: LocalDate = LocalDate.now()
)
```

#### Step 2: Gateway Interface
```kotlin
// domain/model/src/main/kotlin/com/donperry/model/{entity}/gateway/{Entity}PersistenceGateway.kt
package com.donperry.model.{entity}.gateway

import com.donperry.model.{entity}.{Entity}
import reactor.core.publisher.Mono
import reactor.core.publisher.Flux

interface {Entity}PersistenceGateway {
    fun save(entity: {Entity}): Mono<{Entity}>
    fun findById(id: String): Mono<{Entity}>
    fun findAll(): Flux<{Entity}>
    fun deleteById(id: String): Mono<Void>
}
```

#### Step 3: Use Case
```kotlin
// domain/usecase/src/main/kotlin/com/donperry/usecase/{entity}/{Action}{Entity}UseCase.kt
package com.donperry.usecase.{entity}

import com.donperry.model.{entity}.{Entity}
import com.donperry.model.{entity}.gateway.{Entity}PersistenceGateway
import reactor.core.publisher.Mono
import java.util.logging.Logger

class {Action}{Entity}UseCase(
    private val {entity}PersistenceGateway: {Entity}PersistenceGateway
) {
    companion object {
        private val logger: Logger = Logger.getLogger({Action}{Entity}UseCase::class.java.name)
    }

    fun execute(
        name: String,
        description: String?
    ): Mono<{Entity}> {
        logger.info("Starting {entity} {action} process")
        
        val {entity} = {Entity}(
            name = name,
            description = description
        )
        
        return {entity}PersistenceGateway.save({entity})
            .doOnNext { saved{Entity} ->
                logger.info("{Entity} successfully saved with ID: ${saved{Entity}.id}")
            }
            .doOnError { error ->
                logger.warning("Failed to {action} {entity}: ${error.message}")
            }
    }
}
```

### 2. REST Infrastructure

#### Step 1: Request/Response DTOs
```kotlin
// infrastructure/entrypoint-rest/src/main/kotlin/com/donperry/rest/{entity}/dto/{Action}{Entity}Request.kt
package com.donperry.rest.{entity}.dto

data class {Action}{Entity}Request(
    val name: String,
    val description: String?
)

// infrastructure/entrypoint-rest/src/main/kotlin/com/donperry/rest/{entity}/dto/{Entity}Response.kt
package com.donperry.rest.{entity}.dto

import java.time.LocalDate

data class {Entity}Response(
    val id: String,
    val name: String,
    val description: String?,
    val createdAt: LocalDate
)
```

#### Step 2: Handler
```kotlin
// infrastructure/entrypoint-rest/src/main/kotlin/com/donperry/rest/{entity}/handler/{Entity}Handler.kt
package com.donperry.rest.{entity}.handler

import com.donperry.rest.{entity}.dto.{Action}{Entity}Request
import com.donperry.rest.{entity}.dto.{Entity}Response
import com.donperry.usecase.{entity}.{Action}{Entity}UseCase
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono
import java.util.logging.Logger

@Component
class {Entity}Handler(
    private val {action}{Entity}UseCase: {Action}{Entity}UseCase
) {
    companion object {
        private val logger: Logger = Logger.getLogger({Entity}Handler::class.java.name)
    }

    fun {action}{Entity}(request: ServerRequest): Mono<ServerResponse> {
        logger.info("Received {entity} {action} request")
        return request
            .bodyToMono({Action}{Entity}Request::class.java)
            .flatMap { req ->
                {action}{Entity}UseCase.execute(req.name, req.description)
            }
            .map { entity ->
                {Entity}Response(
                    id = entity.id!!,
                    name = entity.name,
                    description = entity.description,
                    createdAt = entity.createdAt
                )
            }
            .flatMap { response ->
                ServerResponse.ok().bodyValue(response)
            }
            .onErrorResume { throwable ->
                logger.warning("Error during {entity} {action}: ${throwable.message}")
                ServerResponse.badRequest().build()
            }
    }
}
```

#### Step 3: Router
```kotlin
// infrastructure/entrypoint-rest/src/main/kotlin/com/donperry/rest/{entity}/{Entity}Router.kt
package com.donperry.rest.{entity}

import com.donperry.rest.{entity}.handler.{Entity}Handler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.router

@Configuration
class {Entity}Router {

    @Bean
    fun {entity}Routes({entity}Handler: {Entity}Handler): RouterFunction<ServerResponse> {
        return router {
            "/api/{entities}".nest {
                accept(MediaType.APPLICATION_JSON).nest {
                    POST("", {entity}Handler::{action}{Entity})
                    GET("/{id}", {entity}Handler::get{Entity}ById)
                    GET("", {entity}Handler::getAll{Entities})
                    DELETE("/{id}", {entity}Handler::delete{Entity})
                }
            }
        }
    }
}
```

### 3. Persistence Infrastructure

#### Step 1: Database Entity
```kotlin
// infrastructure/postgres-db/src/main/kotlin/com/donperry/persistence/{entity}/entities/{Entity}Data.kt
package com.donperry.persistence.{entity}.entities

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDate

@Table("{entity_table_name}")
data class {Entity}Data(
    @Id val id: String? = null,
    val name: String,
    val description: String?,
    val createdAt: LocalDate
)
```

#### Step 2: Repository
```kotlin
// infrastructure/postgres-db/src/main/kotlin/com/donperry/persistence/{entity}/{Entity}Repository.kt
package com.donperry.persistence.{entity}

import com.donperry.persistence.{entity}.entities.{Entity}Data
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface {Entity}Repository : ReactiveCrudRepository<{Entity}Data, String>
```

#### Step 3: Mapper
```kotlin
// infrastructure/postgres-db/src/main/kotlin/com/donperry/persistence/{entity}/mapper/{Entity}Mapper.kt
package com.donperry.persistence.{entity}.mapper

import com.donperry.model.{entity}.{Entity}
import com.donperry.persistence.{entity}.entities.{Entity}Data

object {Entity}Mapper {
    fun toEntity({entity}: {Entity}): {Entity}Data {
        return {Entity}Data(
            id = {entity}.id,
            name = {entity}.name,
            description = {entity}.description,
            createdAt = {entity}.createdAt
        )
    }

    fun toModel({entity}Data: {Entity}Data): {Entity} {
        return {Entity}(
            id = {entity}Data.id,
            name = {entity}Data.name,
            description = {entity}Data.description,
            createdAt = {entity}Data.createdAt
        )
    }
}
```

#### Step 4: Persistence Adapter
```kotlin
// infrastructure/postgres-db/src/main/kotlin/com/donperry/persistence/{entity}/{Entity}PersistenceAdapter.kt
package com.donperry.persistence.{entity}

import com.donperry.model.{entity}.{Entity}
import com.donperry.model.{entity}.gateway.{Entity}PersistenceGateway
import com.donperry.persistence.{entity}.mapper.{Entity}Mapper
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Service
class {Entity}PersistenceAdapter(
    private val {entity}Repository: {Entity}Repository
) : {Entity}PersistenceGateway {

    override fun save({entity}: {Entity}): Mono<{Entity}> {
        return {entity}Repository.save({Entity}Mapper.toEntity({entity}))
            .map { {Entity}Mapper.toModel(it) }
    }

    override fun findById(id: String): Mono<{Entity}> {
        return {entity}Repository.findById(id)
            .map { {Entity}Mapper.toModel(it) }
    }

    override fun findAll(): Flux<{Entity}> {
        return {entity}Repository.findAll()
            .map { {Entity}Mapper.toModel(it) }
    }

    override fun deleteById(id: String): Mono<Void> {
        return {entity}Repository.deleteById(id)
    }
}
```

### 4. Configuration & Wiring

#### Use Case Bean Configuration
```kotlin
// applications/pet/src/main/kotlin/com/donperry/app/configuration/UseCasesConfig.kt
@Bean
fun {action}{Entity}UseCase({entity}PersistenceGateway: {Entity}PersistenceGateway): {Action}{Entity}UseCase {
    return {Action}{Entity}UseCase({entity}PersistenceGateway)
}
```

---

## 📝 Best Practices & Conventions

### Naming Conventions
- **Packages**: `com.donperry.{layer}.{domain}.{type}`
- **Classes**: PascalCase (`RegisterPetUseCase`, `PetHandler`)
- **Methods**: camelCase (`execute`, `registerPet`)
- **Variables**: camelCase (`petPersistenceGateway`)
- **Constants**: UPPER_SNAKE_CASE
- **Database Tables**: snake_case

### Code Standards
1. **Reactive Programming**: Always return `Mono<T>` or `Flux<T>`
2. **Logging**: Use `java.util.logging.Logger` with consistent patterns
3. **Error Handling**: Use `.doOnError()` for logging, `.onErrorResume()` for recovery
4. **Null Safety**: Leverage Kotlin null safety, use `?` for nullable types
5. **Immutability**: Prefer `data class` and `val` over `var`

### Testing Patterns
```kotlin
// Example test structure
@ExtendWith(MockKExtension::class)
class {Action}{Entity}UseCaseTest {
    
    @MockK
    private lateinit var {entity}PersistenceGateway: {Entity}PersistenceGateway
    
    private lateinit var {action}{Entity}UseCase: {Action}{Entity}UseCase
    
    @BeforeEach
    fun setUp() {
        {action}{Entity}UseCase = {Action}{Entity}UseCase({entity}PersistenceGateway)
    }
    
    @Test
    fun `should {action} {entity} successfully`() {
        // Given
        val {entity} = {Entity}(name = "Test", description = "Test Description")
        every { {entity}PersistenceGateway.save(any()) } returns Mono.just({entity})
        
        // When
        val result = {action}{Entity}UseCase.execute("Test", "Test Description")
        
        // Then
        StepVerifier.create(result)
            .expectNext({entity})
            .verifyComplete()
    }
}
```

### Module Dependencies in build.gradle.kts
```kotlin
// When adding new modules, update applications/pet/build.gradle.kts
dependencies {
    implementation(project(":model"))
    implementation(project(":usecase"))
    implementation(project(":rest"))
    implementation(project(":persistence"))
    // Add new modules here
}
```

---

## ⚡ Quick Commands

### Development
```bash
# Run specific module tests
./gradlew :{module-name}:test

# Build and run
./gradlew clean build && ./gradlew :pet:bootRun

# Generate coverage report
./gradlew testWithMergedCoverage
```

### Docker Development
```bash
cd applications/pet
docker-compose up --build
```

---

## 🔧 AI Assistant Guidelines

### When Implementing New Features:
1. **Start with Domain**: Create model and gateway interface first
2. **Add Use Case**: Implement business logic without framework dependencies  
3. **Add Infrastructure**: REST handlers, persistence adapters
4. **Wire in Application**: Add beans to UseCasesConfig
5. **Add Tests**: For each layer independently
6. **Update Routes**: Add to appropriate router configuration

### Code Generation Priority:
1. Follow existing patterns exactly
2. Maintain reactive programming with Mono/Flux
3. Add proper logging with consistent messages
4. Include comprehensive error handling
5. Write tests following the established patterns
6. Update module dependencies if needed

### Don't:
- ❌ Mix business logic with framework code
- ❌ Put Spring annotations in domain/usecase modules
- ❌ Use blocking operations (use reactive alternatives)
- ❌ Skip error handling and logging
- ❌ Create circular dependencies between modules

### Do:
- ✅ Follow hexagonal architecture principles
- ✅ Use dependency injection through constructor parameters
- ✅ Implement comprehensive logging for debugging
- ✅ Write reactive code with proper error handling
- ✅ Maintain clean separation of concerns
- ✅ Follow established naming conventions

---

This guide ensures consistent, maintainable, and architecturally sound code generation across all AI development tools.