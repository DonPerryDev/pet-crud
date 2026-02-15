# API Design

## Current Endpoints

| Method | Path | Handler | Description |
|--------|------|---------|-------------|
| POST | `/api/pets` | `PetHandler::registerPet` | Register a new pet |

## Endpoint Details

### POST /api/pets

**Router:** `PetRouter.kt` — `infrastructure/entrypoint-rest/src/main/kotlin/com/donperry/rest/pet/PetRouter.kt`
**Handler:** `PetHandler.kt` — `infrastructure/entrypoint-rest/src/main/kotlin/com/donperry/rest/pet/handler/PetHandler.kt`

**Request Body:**
```json
{
  "name": "string",
  "species": "string",
  "breed": "string | null",
  "age": "int",
  "owner": "string"
}
```

**Success Response:** `200 OK` (empty body)
**Error Response:** `400 Bad Request` (empty body)

## Management Endpoints

| Method | Path | Port | Description |
|--------|------|------|-------------|
| GET | `/management/health` | 9090 | Actuator health check |

## External API Calls

| Adapter | Method | Target | Description |
|---------|--------|--------|-------------|
| `UserAdapter` | GET | `/api/v1/users/email/{email}` | Fetch user by email via WebClient |

## Design Patterns

### Adding a New Endpoint

1. Create request/response DTOs in `rest/{entity}/dto/`
2. Create handler in `rest/{entity}/handler/{Entity}Handler.kt`
3. Create router in `rest/{entity}/{Entity}Router.kt`
4. Register route under `/api/{entity}s` namespace
5. Use `accept(MediaType.APPLICATION_JSON)` for content negotiation
6. Handler methods accept `ServerRequest` and return `Mono<ServerResponse>`

### Route Naming Convention
- Collection: `/api/{entity}s`
- Single resource: `/api/{entity}s/{id}`
- Actions: `/api/{entity}s/{id}/{action}`
