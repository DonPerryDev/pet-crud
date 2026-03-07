# Skill: Test Developer

Writes fast, isolated unit tests with StepVerifier for Kotlin/Spring WebFlux.

## Testing Rules

- Pure unit tests by default (no `@SpringBootTest`)
- Mock interfaces/gateways, not concrete classes
- StepVerifier for ALL Mono/Flux assertions
- Mockito + MockitoExtension with mockito-kotlin
- JUnit 5 with backtick test names
- Test both success and error paths

## Testing by Layer

| Layer | Module | Mock Strategy | Assertion |
|-------|--------|--------------|-----------|
| Model | `:model` | No mocks | Direct assertions on data classes |
| UseCase | `:usecase` | Mock gateway interfaces | StepVerifier |
| Handler | `:rest` | Mock use cases | StepVerifier on `Mono<ServerResponse>` |
| Persistence Adapter | `:persistence` | Mock repository | StepVerifier |
| REST Client Adapter | `:client-rest` | MockWebServer | StepVerifier |

## Test Pattern

```kotlin
@ExtendWith(MockitoExtension::class)
class {ClassName}Test {

    @Mock
    private lateinit var gateway: GatewayType

    @InjectMocks
    private lateinit var useCase: UseCaseType

    @Test
    fun `should {behavior} when {condition}`() {
        // Arrange
        whenever(gateway.method(any())).thenReturn(Mono.just(result))

        // Act & Assert
        StepVerifier.create(useCase.execute(input))
            .expectNext(expected)
            .verifyComplete()
    }

    @Test
    fun `should propagate error when {failure}`() {
        // Arrange
        whenever(gateway.method(any())).thenReturn(Mono.error(RuntimeException("fail")))

        // Act & Assert
        StepVerifier.create(useCase.execute(input))
            .expectError(RuntimeException::class.java)
            .verify()
    }
}
```

## Coverage Targets

| Layer | Target | Rationale |
|-------|--------|-----------|
| UseCase | 90%+ | Core business logic |
| Handler | 80%+ | Request/response mapping |
| Adapter | 80%+ | Infrastructure integration |
| Model | 70%+ | Data class validation |

## Test Commands

```bash
# All tests
./gradlew test

# Single module
./gradlew :usecase:test
./gradlew :rest:test
./gradlew :persistence:test
./gradlew :client-rest:test
./gradlew :model:test

# With coverage
./gradlew testWithMergedCoverage

# Single test class
./gradlew :usecase:test --tests "com.donperry.usecase.pet.RegisterPetUseCaseTest"
```
