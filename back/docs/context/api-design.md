# API Design

## Current Endpoints

| Method | Path | Handler | Description |
|--------|------|---------|-------------|
| GET | `/api/pets` | `PetHandler::listPets` | List all active pets for the authenticated user |
| POST | `/api/pets` | `PetHandler::registerPet` | Register a new pet |
| PUT | `/api/pets/{petId}` | `PetHandler::updatePet` | Update an existing pet's profile |
| DELETE | `/api/pets/{petId}` | `PetHandler::deletePet` | Soft-delete a pet (idempotent) |
| POST | `/api/pets/{petId}/avatar/presign` | `PetHandler::generatePresignedUrl` | Generate presigned URL for avatar upload |
| POST | `/api/pets/{petId}/avatar/confirm` | `PetHandler::confirmAvatarUpload` | Confirm avatar upload and update pet |

## Endpoint Details

### GET /api/pets

**Router:** `PetRouter.kt` — `infrastructure/entrypoint-rest/src/main/kotlin/com/donperry/rest/pet/PetRouter.kt`
**Handler:** `PetHandler.kt` — `infrastructure/entrypoint-rest/src/main/kotlin/com/donperry/rest/pet/handler/PetHandler.kt`
**Authentication:** Required (JWT Bearer token)

**Request Body:** None

**Success Response:** `200 OK`
```json
[
  {
    "id": "string",
    "name": "string",
    "species": "DOG | CAT",
    "breed": "string | null",
    "photoUrl": "string | null"
  }
]
```

**Error Responses:**
- `401 Unauthorized` — Missing or invalid JWT token
- `500 Internal Server Error` — Database error

**Error Response Format:**
```json
{
  "error": "UNAUTHORIZED | INTERNAL_ERROR",
  "message": "Detailed error message",
  "timestamp": "ISO-8601 timestamp"
}
```

**Behavior:**
- Returns only active pets (soft-deleted pets are excluded)
- Results are automatically filtered by owner based on JWT userId
- Returns empty array if user has no pets
- Response uses simplified `PetListResponse` DTO (subset of full pet data)

### POST /api/pets

**Router:** `PetRouter.kt` — `infrastructure/entrypoint-rest/src/main/kotlin/com/donperry/rest/pet/PetRouter.kt`
**Handler:** `PetHandler.kt` — `infrastructure/entrypoint-rest/src/main/kotlin/com/donperry/rest/pet/handler/PetHandler.kt`
**Authentication:** Required (JWT Bearer token)
**Content-Type:** `application/json`

**Request Body:**
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
- `500 Internal Server Error` — Database error

**Error Response Format:**
```json
{
  "error": "VALIDATION_ERROR | PET_LIMIT_EXCEEDED | UNAUTHORIZED | INTERNAL_ERROR",
  "message": "Detailed error message",
  "timestamp": "ISO-8601 timestamp"
}
```

### PUT /api/pets/{petId}

**Router:** `PetRouter.kt` — `infrastructure/entrypoint-rest/src/main/kotlin/com/donperry/rest/pet/PetRouter.kt`
**Handler:** `PetHandler.kt` — `infrastructure/entrypoint-rest/src/main/kotlin/com/donperry/rest/pet/handler/PetHandler.kt`
**Authentication:** Required (JWT Bearer token)
**Content-Type:** `application/json`

**Path Parameters:**
- `petId` (required): UUID of the pet to update

**Request Body:**
```json
{
  "name": "string",
  "species": "DOG | CAT",
  "breed": "string | null",
  "age": "int",
  "birthdate": "YYYY-MM-DD | null",
  "weight": "decimal | null",
  "nickname": "string | null",
  "photoUrl": "string | null"
}
```

**Success Response:** `200 OK`
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
- `401 Unauthorized` — Missing or invalid JWT token, or user is not the pet owner
- `404 Not Found` — Pet not found
- `500 Internal Server Error` — Database error

**Error Response Format:**
```json
{
  "error": "VALIDATION_ERROR | UNAUTHORIZED | PET_NOT_FOUND | INTERNAL_ERROR",
  "message": "Detailed error message",
  "timestamp": "ISO-8601 timestamp"
}
```

### DELETE /api/pets/{petId}

**Router:** `PetRouter.kt` — `infrastructure/entrypoint-rest/src/main/kotlin/com/donperry/rest/pet/PetRouter.kt`
**Handler:** `PetHandler.kt` — `infrastructure/entrypoint-rest/src/main/kotlin/com/donperry/rest/pet/handler/PetHandler.kt`
**Authentication:** Required (JWT Bearer token)

**Path Parameters:**
- `petId` (required): UUID of the pet to delete

**Request Body:** None

**Success Response:** `204 No Content`

No body returned on successful deletion.

**Error Responses:**
- `401 Unauthorized` — Missing or invalid JWT token, or user is not the pet owner
- `404 Not Found` — Pet not found
- `500 Internal Server Error` — Database error

**Error Response Format:**
```json
{
  "error": "UNAUTHORIZED | PET_NOT_FOUND | INTERNAL_ERROR",
  "message": "Detailed error message",
  "timestamp": "ISO-8601 timestamp"
}
```

**Behavior:**
- Soft-delete operation: Sets `deleted_at` to current timestamp
- Idempotent: Deleting an already-deleted pet returns 204 No Content without error
- Ownership verification: Only the pet owner can delete their pet
- Deleted pets are excluded from all queries (findById, findAllByOwner, countByOwner)

### POST /api/pets/{petId}/avatar/presign

**Router:** `PetRouter.kt` — `infrastructure/entrypoint-rest/src/main/kotlin/com/donperry/rest/pet/PetRouter.kt`
**Handler:** `PetHandler.kt` — `infrastructure/entrypoint-rest/src/main/kotlin/com/donperry/rest/pet/handler/PetHandler.kt`
**Authentication:** Required (JWT Bearer token)
**Content-Type:** `application/json`

**Path Parameters:**
- `petId` (required): UUID of the pet

**Request Body:**
```json
{
  "contentType": "image/jpeg | image/png"
}
```

**Success Response:** `200 OK`
```json
{
  "uploadUrl": "string (S3 presigned URL)",
  "key": "string (S3 object key)",
  "expiresAt": "ISO-8601 timestamp"
}
```

**Error Responses:**
- `400 Bad Request` — Invalid content type
- `401 Unauthorized` — Missing or invalid JWT token, or user is not the pet owner
- `404 Not Found` — Pet not found
- `500 Internal Server Error` — S3 presigner failure

**Error Response Format:**
```json
{
  "error": "VALIDATION_ERROR | UNAUTHORIZED | PET_NOT_FOUND | INTERNAL_ERROR",
  "message": "Detailed error message",
  "timestamp": "ISO-8601 timestamp"
}
```

### POST /api/pets/{petId}/avatar/confirm

**Router:** `PetRouter.kt` — `infrastructure/entrypoint-rest/src/main/kotlin/com/donperry/rest/pet/PetRouter.kt`
**Handler:** `PetHandler.kt` — `infrastructure/entrypoint-rest/src/main/kotlin/com/donperry/rest/pet/handler/PetHandler.kt`
**Authentication:** Required (JWT Bearer token)
**Content-Type:** `application/json`

**Path Parameters:**
- `petId` (required): UUID of the pet

**Request Body:**
```json
{
  "contentType": "image/jpeg | image/png"
}
```

**Success Response:** `200 OK`
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
  "photoUrl": "string"
}
```

**Error Responses:**
- `400 Bad Request` — Invalid content type
- `401 Unauthorized` — Missing or invalid JWT token, or user is not the pet owner
- `404 Not Found` — Pet not found or photo not found in S3
- `500 Internal Server Error` — Database error

**Error Response Format:**
```json
{
  "error": "VALIDATION_ERROR | UNAUTHORIZED | PET_NOT_FOUND | PHOTO_NOT_FOUND | INTERNAL_ERROR",
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
