# Skill: Code Reviewer

Reviews code for architecture compliance, quality, security, and test coverage.

## Review Categories

### 1. Architecture Compliance
- [ ] Domain layer has no Spring annotations
- [ ] Gateway interfaces in `domain/model/{entity}/gateway/`
- [ ] Use cases depend only on `:model`
- [ ] Infrastructure adapters implement gateway interfaces
- [ ] No cross-layer dependency violations
- [ ] DTOs separate from domain models

### 2. Code Quality
- [ ] Kotlin idiomatic (data classes, null safety, extension functions)
- [ ] Pure reactive chains (no `.block()`, no `try-catch`)
- [ ] Error handling with `onErrorMap`/`onErrorResume`
- [ ] No imperative side effects in `map`/`flatMap`
- [ ] Logger in companion object using JUL

### 3. Security
- [ ] No hardcoded secrets
- [ ] Input validation on request DTOs
- [ ] Parameterized queries (no string concatenation in SQL)
- [ ] No PII in logs
- [ ] CORS properly configured
- [ ] Error responses don't leak internals

### 4. Testing Coverage
- [ ] Unit tests for use cases, handlers, adapters
- [ ] StepVerifier for Mono/Flux assertions
- [ ] Both success and error paths tested
- [ ] No `@SpringBootTest` in unit tests
- [ ] Mocks for interfaces/gateways, not concrete classes

## Validation Patterns

Flag chained `if (...) throw` blocks as a code smell. Require idiomatic Kotlin alternatives:

| Scenario | Pattern | Example |
|----------|---------|---------|
| Multiple field validations | `listOfNotNull` + `takeIf` | Aggregates all errors into one `ValidationException` with `joinToString("; ")` |
| Single guard condition | `when` expression | Fail-fast with one `throw` |

**Bad** (flag as MEDIUM severity):
```kotlin
if (name.isBlank()) throw ValidationException("Name required")
if (age < 0) throw ValidationException("Invalid age")
```

**Good** — aggregate:
```kotlin
val errors = listOfNotNull(
    "Name required".takeIf { name.isBlank() },
    "Invalid age".takeIf { age < 0 },
)
if (errors.isNotEmpty()) throw ValidationException(errors.joinToString("; "))
```

**Good** — fail-fast single check:
```kotlin
when {
    photoSize > MAX_SIZE -> throw PhotoSizeExceededException(photoSize, MAX_SIZE)
}
```

## Common Issues

| Issue | Severity | Fix |
|-------|----------|-----|
| `.block()` in reactive chain | HIGH | Use `flatMap`/`map` instead |
| `try-catch` around reactive code | HIGH | Use `onErrorMap`/`onErrorResume` |
| Spring annotation in domain | HIGH | Move to infrastructure layer |
| `println` in production | MEDIUM | Replace with `logger.info` |
| Missing error handling in chain | MEDIUM | Add `onErrorResume`/`onErrorMap` |
| `@SpringBootTest` in unit test | MEDIUM | Use `@ExtendWith(MockitoExtension::class)` |
| Chained `if` throw validations | MEDIUM | Use `listOfNotNull`+`takeIf` or `when` |
| Missing bracket identifier in log | LOW | Add `[$id]` prefix |
| DTO reused as domain model | LOW | Create separate domain data class |
