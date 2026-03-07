# Skill: Backend Developer

Implements production Kotlin/Spring WebFlux code following Clean Architecture patterns.

## Workflow

1. Read the approved requirement (from requirement-architect)
2. Search codebase for existing related code (avoid duplication)
3. Read `docs/coding-standards.md` and `docs/scaffolding.md`
4. Implement layer by layer: model → gateway → usecase → handler → router → adapter
5. Verify build: `./gradlew clean build`

## Checklist

- [ ] Domain model is a `data class` with no annotations
- [ ] Gateway interface is in `domain/model/{entity}/gateway/`
- [ ] Gateway returns `Mono<T>` or `Flux<T>`
- [ ] Use case is a plain Kotlin class (no Spring annotations)
- [ ] Use case class name ends with `UseCase` (auto-scanned)
- [ ] Handler is `@Component` with `ServerRequest → Mono<ServerResponse>`
- [ ] Router is `@Configuration` with `@Bean` returning `RouterFunction`
- [ ] DTOs are separate `data class` files in `dto/` package
- [ ] Persistence adapter is `@Service` implementing gateway interface
- [ ] Mapper is an `object` with `toEntity`/`toModel` functions
- [ ] Logger is `java.util.logging.Logger` in `companion object`
- [ ] No `.block()` anywhere
- [ ] No `try-catch` in reactive chains
- [ ] No Spring annotations in `domain/model` or `domain/usecase`
- [ ] Functions have at most 5 arguments (use DTOs for more)
- [ ] Build passes: `./gradlew clean build`

## Validation Patterns

Use **idiomatic Kotlin** instead of chained `if` throws.

### Handler-Level Validation (Validated sealed class)

Handlers must **never** contain imperative `if`/`return@flatMap Mono.error` validation blocks. Instead, use a `Validated<T>` sealed class with `validate()` extension functions on request DTOs. This keeps handlers declarative and validation logic testable in isolation.

**Validated sealed class** (in `rest/common/validation/`):
```kotlin
sealed class Validated<out T> {
    data class Valid<T>(val value: T) : Validated<T>()
    data class Invalid(val error: String) : Validated<Nothing>()
}
```

**DTO validate() extension** — converts a request DTO into a domain command or returns an error:
```kotlin
fun RegisterPetRequest.validate(userId: String): Validated<RegisterPetCommand> {
    val error = when {
        name.isBlank() -> "Pet name cannot be blank"
        species.isBlank() -> "Pet species cannot be blank"
        breed?.isBlank() == true -> "Pet breed cannot be blank"
        nickname?.isBlank() == true -> "Pet nickname cannot be blank"
        else -> null
    }
    if (error != null) return Validated.Invalid(error)

    val parsedSpecies = Species.entries.find { it.name.equals(species, ignoreCase = true) }
        ?: return Validated.Invalid("Invalid species: $species. Must be one of: ${Species.entries.joinToString()}")

    return Validated.Valid(RegisterPetCommand(userId, name, parsedSpecies, breed, age, birthdate, weight, nickname))
}
```

**Handler usage** — `when` expression dispatches on the validation result:
```kotlin
request.bodyToMono(RegisterPetRequest::class.java)
    .flatMap { petRequest ->
        when (val result = petRequest.validate(userId)) {
            is Validated.Invalid -> Mono.error(ValidationException(result.error))
            is Validated.Valid -> registerPetUseCase.execute(result.value)
                .flatMap { pet -> buildCreatedResponse(pet) }
        }
    }
```

### Use Case-Level Validation (listOfNotNull + takeIf)

**Aggregate errors** — when the user benefits from seeing all validation failures at once:
```kotlin
private fun validateInputs(userId: String, name: String, age: Int) {
    val errors = listOfNotNull(
        "User ID cannot be blank".takeIf { userId.isBlank() },
        "Name cannot be blank".takeIf { name.isBlank() },
        "Age must be zero or greater".takeIf { age < 0 },
    )
    if (errors.isNotEmpty()) {
        throw ValidationException(errors.joinToString("; "))
    }
}
```

### Single-Condition Guards (when expression)

**Fail-fast** — when a single condition is enough to reject:
```kotlin
private fun validatePhoto(photoSize: Long?) {
    when {
        photoSize != null && photoSize > MAX_SIZE -> throw PhotoSizeExceededException(photoSize, MAX_SIZE)
    }
}
```

> **Never** use chained `if (...) throw` blocks or `if`/`return@flatMap Mono.error` blocks. Use `Validated<T>` for handler validation, `listOfNotNull` + `takeIf` for use case validation, `when` for single-condition guards.

## Function Argument Limits

If a function has **more than 5 arguments**, create a **data class DTO** to group them.

**Bad** — too many arguments:
```kotlin
fun execute(
    userId: String, name: String, species: Species, breed: String?,
    age: Int, birthdate: LocalDate?, weight: BigDecimal?, nickname: String?,
    photoFileName: String?, photoContentType: String?, photoBytes: ByteArray?, photoSize: Long?
): Mono<Pet>
```

**Good** — grouped into DTOs:
```kotlin
data class RegisterPetCommand(
    val userId: String,
    val name: String,
    val species: Species,
    val breed: String?,
    val age: Int,
    val birthdate: LocalDate?,
    val weight: BigDecimal?,
    val nickname: String?,
    val photo: PhotoUploadData?
)

fun execute(command: RegisterPetCommand): Mono<Pet>
```

> **Placement:** Domain-level DTOs go in `domain/model/src/main/kotlin/.../model/{entity}/`. Infrastructure DTOs (REST requests/responses) stay in their respective `dto/` packages.

## Common Issues

| Issue | Fix |
|-------|-----|
| Use case not detected by ComponentScan | Ensure class name ends with `UseCase` |
| Gateway not wired | Check that adapter has `@Service` and implements the gateway interface |
| Router not registered | Ensure class has `@Configuration` and method has `@Bean` |
| R2DBC mapping fails | Verify `@Table` name matches database table, `@Id` on primary key |
| WebClient fails | Check `WebClient.Builder` bean exists, verify base URL |
| Function has >5 arguments | Create a `data class` DTO to group parameters |
