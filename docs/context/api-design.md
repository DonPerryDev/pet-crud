# API Design

## Current Endpoints

| Method | Path | Handler | Description |
|--------|------|---------|-------------|
| POST | `/api/pets` | `PetHandler::registerPet` | Register a new pet |

## Endpoint Details

### POST /api/pets

**Router:** `PetRouter.kt` — `infrastructure/entrypoint-rest/src/main/kotlin/com/donperry/rest/pet/PetRouter.kt`
**Handler:** `PetHandler.kt` — `infrastructure/entrypoint-rest/src/main/kotlin/com/donperry/rest/pet/handler/PetHandler.kt`
**Authentication:** Required (JWT Bearer token)
**Content-Type:** `multipart/form-data`

**Request Parts:**
- `pet` (required): JSON with pet data
  ```json
  {
    "name": "string",
    "species": "DOG | CAT",
    "breed": "string | null",
    "age": "int",
    "birthdate": "YYYY-MM-DD | null",
    "weight": "decimal | null",
    "nickname": "string | null"
  }
  ```
- `photo` (optional): Image file (max 5 MB)

**Success Response:** `201 Created`
```json
{
  "id": "string",
  "name": "string",
  "species": "DOG | CAT",
  "breed": "string | null",
  "age": "int",
  "birthdate": "YYYY-MM-DD | null",
  "weight": "decimal | null",
  "nickname": "string | null",
  "owner": "string",
  "registrationDate": "YYYY-MM-DD",
  "photoUrl": "string | null"
}
```

**Error Responses:**
- `400 Bad Request` — Validation errors (invalid species, blank name, negative age, future birthdate, invalid weight)
- `401 Unauthorized` — Missing or invalid JWT token
- `409 Conflict` — Pet limit exceeded (max 10 pets per user)
- `413 Payload Too Large` — Photo exceeds 5 MB
- `500 Internal Server Error` — S3 upload failure or database error

**Error Response Format:**
```json
{
  "error": "VALIDATION_ERROR | PET_LIMIT_EXCEEDED | PHOTO_SIZE_EXCEEDED | UNAUTHORIZED | PHOTO_UPLOAD_FAILED | INTERNAL_ERROR",
  "message": "Detailed error message",
  "timestamp": "ISO-8601 timestamp"
}
```

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
