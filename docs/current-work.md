# Current Work

## Active Branch
`main`

## Recent Commits

| Hash | Message |
|------|---------|
| `38b8dd9` | feat: enhance AI Development Guide and CLAUDE.md with comprehensive testing practices and commands |
| `067eb92` | test: init test |
| `76677f2` | feat: add AI Development Guide and CLAUDE.md for project documentation |
| `16ddd8a` | fix: update user email endpoint URI in UserAdapter |
| `0ca79df` | fix: update R2DBC connection details in application.yaml and docker-compose.yml |
| `91ae8d1` | feat: add docker-compose.yml |
| `85c0a63` | feat: add devtools |
| `e100c0d` | feat: add devtools |
| `508bbfa` | fix: sonnar issues |
| `77ebce6` | fix: resolve database connection issue |

## Implemented Features

### Pet Domain
- **Pet Model** — `domain/model/src/main/kotlin/com/donperry/model/pet/Pet.kt`
- **Pet Persistence Gateway** — Interface for pet storage operations
- **Register Pet Use Case** — Business logic for pet registration
- **Pet REST Endpoint** — `POST /api/pets` via functional router
- **Pet Persistence Adapter** — R2DBC implementation with mapper
- **Pet Repository** — ReactiveCrudRepository for PetData

### User Domain
- **User Model** — `domain/model/src/main/kotlin/com/donperry/model/user/User.kt`
- **User Gateway** — Interface for user lookup by email
- **User Adapter** — WebClient-based external API client

### Infrastructure
- **CORS Configuration** — Global CORS config with configurable allowed origins
- **Security Properties** — Externalized security configuration
- **Jackson Config** — Kotlin-aware JSON serialization
- **Docker Compose** — PostgreSQL 15 + application containers

## Module Status

| Module | Models | Gateways | Use Cases | Handlers | Tests |
|--------|--------|----------|-----------|----------|-------|
| Pet | Pet | PetPersistenceGateway | RegisterPetUseCase | PetHandler | Yes |
| User | User | UserGateway | — | — | Yes |
| Enums | Channels | — | — | — | — |
