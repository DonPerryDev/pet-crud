# Coding Standards

## Naming Conventions

| Element | Convention | Example |
|---------|-----------|---------|
| Classes | PascalCase | `RegisterPetUseCase`, `PetHandler` |
| Functions | camelCase | `registerPet`, `execute` |
| Properties | camelCase | `petPersistenceGateway`, `registrationDate` |
| Constants | UPPER_SNAKE_CASE | `MAX_RETRY_COUNT` |
| Packages | lowercase | `com.donperry.model.pet` |
| Test functions | Backtick descriptive | `` `should save pet when valid` `` |
| DTOs | PascalCase + suffix | `RegisterPetRequest`, `PetResponse` |
| Entities (DB) | PascalCase + `Data` | `PetData` |
| Mappers | PascalCase + `Mapper` | `PetMapper` (object, not class) |

## File Organization by Layer

### Domain Model (`domain/model/`)
```
com.donperry.model.{entity}/
├── {Entity}.kt              # data class, no annotations
├── {Enum}.kt                # enum classes (e.g., Species)
├── gateway/
│   ├── {Entity}PersistenceGateway.kt   # interface, Mono/Flux return types
│   └── {Service}Gateway.kt              # interface for external services
├── exception/
│   └── DomainException.kt   # sealed class + specific exceptions
```

### Domain UseCase (`domain/usecase/`)
```
com.donperry.usecase.{entity}/
└── {Action}{Entity}UseCase.kt   # plain class, constructor-injected gateways
```

### REST Entrypoint (`infrastructure/entrypoint-rest/`)
```
com.donperry.rest.{entity}/
├── {Entity}Router.kt            # @Configuration, @Bean RouterFunction
├── handler/
│   └── {Entity}Handler.kt      # @Component, ServerRequest → Mono<ServerResponse>
├── dto/
│   ├── {Action}{Entity}Request.kt   # data class
│   └── {Entity}Response.kt          # data class
├── common/
│   └── dto/
│       └── ErrorResponse.kt        # shared error response DTO
```

### Persistence (`infrastructure/postgres-db/`)
```
com.donperry.persistence.{entity}/
├── {Entity}PersistenceAdapter.kt    # @Service, implements gateway
├── {Entity}Repository.kt           # ReactiveCrudRepository interface
├── entities/
│   └── {Entity}Data.kt             # R2DBC entity with @Table
├── mapper/
│   └── {Entity}Mapper.kt           # object with toEntity/toModel functions
```

### REST Client (`infrastructure/client-rest/`)
```
com.donperry.client.rest.{entity}/
├── {Entity}Adapter.kt              # @Component, implements gateway, uses WebClient
├── dto/
│   └── {Entity}Data.kt             # external API response model
├── mapper/
│   └── {Entity}Mapper.kt           # object with mapping functions
```

### S3 Storage (`infrastructure/s3-storage/`)
```
com.donperry.storage.{type}/
├── S3{Type}StorageAdapter.kt       # @Service, implements gateway
├── S3ClientConfig.kt               # @Configuration, S3Client bean
└── S3Properties.kt                 # @ConfigurationProperties
```

## Reactive Patterns

### Return Types
- Single value: `Mono<T>`
- Multiple values: `Flux<T>`
- Void operations: `Mono<Void>`

### Reactive Chain Rules
```kotlin
// CORRECT: Pure reactive chain
fun execute(name: String): Mono<Pet> =
    petGateway.save(pet)
        .map { savedPet -> transform(savedPet) }
        .doOnNext { logger.info("Saved") }
        .onErrorMap { BusinessException("Failed") }

// WRONG: Blocking call
fun execute(name: String): Pet =
    petGateway.save(pet).block()  // NEVER

// WRONG: try-catch in reactive
fun execute(name: String): Mono<Pet> =
    try {                          // NEVER
        petGateway.save(pet)
    } catch (e: Exception) {
        Mono.error(e)
    }

// WRONG: Imperative in map
fun execute(name: String): Mono<Pet> =
    petGateway.save(pet)
        .map { pet ->
            println("saved")      // NEVER: side effect in map
            pet
        }
```

### Error Handling
```kotlin
// Map to business exception
.onErrorMap(DataAccessException::class.java) { e ->
    PersistenceException("Failed to save pet", e)
}

// Fallback
.onErrorResume(NotFoundException::class.java) { e ->
    Mono.empty()
}

// Log errors (use doOnError, not try-catch)
.doOnError { error ->
    logger.warning("[$petId] Operation failed: ${error.message}")
}
```

## Logging

### Logger Declaration
```kotlin
companion object {
    private val logger: Logger = Logger.getLogger(ClassName::class.java.name)
}
```

### Log Levels
| Level | Use For | Example |
|-------|---------|---------|
| `INFO` | Operation boundaries | `"[$id] Starting registration"`, `"[$id] Completed"` |
| `WARNING` | Recoverable failures | `"[$id] Failed to save: ${error.message}"` |
| `FINE` | Debug details | `"[$id] Parameters: name=$name, age=$age"` |

### Rules
- Always include identifier in brackets: `[$petId]`
- Use `java.util.logging.Logger` (not SLF4J, not println)
- No PII in logs (no emails, passwords, tokens)

## Domain Layer Rules

- NO Spring annotations (`@Component`, `@Service`, `@Autowired`, etc.)
- NO framework imports (only pure Kotlin + Reactor)
- Data classes for models — no inheritance, no mutable state
- Gateway interfaces define contracts — implementations live in infrastructure
- Use cases are plain classes with constructor-injected gateways

## Infrastructure Layer Rules

- Adapters implement gateway interfaces from domain
- Spring annotations allowed (`@Service`, `@Component`, `@Configuration`)
- Mapper objects convert between domain models and infrastructure entities
- DTOs are separate from domain models

## Test Structure

```kotlin
@ExtendWith(MockitoExtension::class)
class {ClassName}Test {

    @Mock
    private lateinit var dependency: DependencyType

    @InjectMocks
    private lateinit var subject: SubjectType

    @Test
    fun `should {behavior} when {condition}`() {
        // Arrange
        whenever(dependency.method(any())).thenReturn(Mono.just(result))

        // Act & Assert
        StepVerifier.create(subject.method(input))
            .expectNext(expected)
            .verifyComplete()
    }
}
```
