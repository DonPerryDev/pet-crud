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
- [ ] Build passes: `./gradlew clean build`

## Common Issues

| Issue | Fix |
|-------|-----|
| Use case not detected by ComponentScan | Ensure class name ends with `UseCase` |
| Gateway not wired | Check that adapter has `@Service` and implements the gateway interface |
| Router not registered | Ensure class has `@Configuration` and method has `@Bean` |
| R2DBC mapping fails | Verify `@Table` name matches database table, `@Id` on primary key |
| WebClient fails | Check `WebClient.Builder` bean exists, verify base URL |
