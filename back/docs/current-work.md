# Current Work

## Completed User Stories

### User Story: List User's Pets (US-010)

**As a** user, **I want to** view a list of all my registered pets, **so that** I can see my pet collection at a glance.

**Status:** COMPLETED

**Acceptance Criteria:**
- Given I am authenticated, when I request my pet list, then I see all my active pets
- Given I have deleted pets, when I query my pet list, then soft-deleted pets are excluded
- Given I have no pets, when I request my pet list, then I receive an empty array
- Given the response includes pet data, when I view the list, then I see id, name, species, breed, and photoUrl (simplified view)

**Implementation Summary:**
- `GET /api/pets` endpoint with JWT authentication
- No request body required (userId extracted from JWT)
- Returns array of simplified pet objects (PetListResponse)
- New files created:
  - `ListPetsUseCase.kt` — Business logic to fetch all active pets for a user
  - `PetListResponse.kt` — Simplified DTO with id, name, species, breed, photoUrl (subset of full PetResponse)
- Modified files:
  - `PetHandler.kt` — Added `listPets()` handler method with 200 OK response
  - `PetRouter.kt` — Added `GET("", petHandler::listPets)` route
- Uses existing `PetPersistenceGateway.findAllByOwner()` method
- Automatic filtering by soft-delete status (deleted_at IS NULL)
- Full error handling (401, 500)
- Tests included for all layers
- Branch: `feat/us-010-list-all-pets`

---

### User Story: Delete Pet - Soft-Delete (US-009)

**As a** user, **I want to** delete a pet from my profile, **so that** I can remove pets I no longer manage.

**Status:** COMPLETED

**Acceptance Criteria:**
- Given I am the owner of a pet, when I delete the pet, then it is soft-deleted (not permanently removed)
- Given I am not the owner, when I try to delete a pet, then I receive a not found error (ownership verification)
- Given I delete an already-deleted pet, when I submit the request, then it succeeds idempotently (204 No Content)
- Given a pet is soft-deleted, when I query for my pets, then the deleted pet is not included in results

**Implementation Summary:**
- `DELETE /api/pets/{petId}` endpoint with JWT authentication and ownership verification
- Soft-delete pattern: Sets `deleted_at` timestamp instead of removing records
- Idempotent operation: Deleting an already-deleted pet returns success without error
- New files created:
  - `DeletePetCommand.kt` — Domain command with petId and userId
  - `DeletePetUseCase.kt` — Business logic with ownership validation and idempotent soft-delete
- Modified files:
  - `Pet.kt` — Added `deletedAt: LocalDateTime?` field
  - `PetData.kt` — Added `deletedAt` column mapping
  - `PetPersistenceGateway.kt` — Added `softDelete(petId: String): Mono<Void>` and `findAllByOwner(userId: String): Flux<Pet>`
  - `PetPersistenceAdapter.kt` — Implemented `softDelete()` and `findAllByOwner()` methods
  - `PetRepository.kt` — Added custom queries with `deleted_at IS NULL` filters:
    - `softDeleteById(id: UUID): Mono<Void>` — Updates deleted_at to CURRENT_TIMESTAMP
    - `findAllByOwnerAndDeletedAtIsNull(owner: String): Flux<PetData>`
    - Overridden `findById()` to filter deleted pets
    - Updated `countByOwner()` to exclude deleted pets
  - `PetHandler.kt` — Added `deletePet()` handler method with 204 No Content response
  - `PetRouter.kt` — Added `DELETE("/{petId}", petHandler::deletePet)` route
- Database migration:
  - `V2__add_deleted_at_column.sql` — Added deleted_at TIMESTAMP column
  - Created partial indexes: `idx_pets_owner_active`, `idx_pets_id_active` for query performance
- All queries (findById, findAllByOwner, countByOwner) exclude soft-deleted pets
- Full error handling (401, 404, 500)
- Tests included for all layers
- Branch: `feat/us-009-delete-pet`

---

### User Story: Edit Pet Profile (US-008)

**As a** user, **I want to** edit my pet's profile information, **so that** I can keep their details up to date.

**Status:** COMPLETED

**Acceptance Criteria:**
- Given I am the owner of a pet, when I update the pet's information, then the changes are saved
- Given I am not the owner, when I try to update a pet, then I receive an unauthorized error
- Given I submit invalid data, when I try to update, then I see validation errors
- Given I update optional fields (breed, birthdate, weight, nickname, photoUrl), when I save, then all data is stored

**Implementation Summary:**
- `PUT /api/pets/{petId}` endpoint with JWT authentication and ownership verification
- New files created:
  - `UpdatePetCommand.kt` — Domain command with petId, userId, and all pet fields
  - `UpdatePetUseCase.kt` — Business logic with ownership validation using filter + switchIfEmpty pattern
  - `UpdatePetRequest.kt` — REST request DTO with validation
- Modified files:
  - `PetPersistenceGateway.kt` — Added `update(pet: Pet): Mono<Pet>` method
  - `PetHandler.kt` — Added `updatePet()` handler method
  - `PetRouter.kt` — Added `PUT("/{petId}", petHandler::updatePet)` route
  - `PetPersistenceAdapter.kt` — Implemented `update()` method
- Validation follows Validated<T> pattern at handler level
- Use case uses filter + switchIfEmpty for ownership check (new coding standard)
- Full error handling (400, 401, 404, 500)
- Tests included for all layers

---

### User Story: Add a New Pet (Initial MVP)

**As a** user, **I want to** add a new pet with basic info and upload an avatar, **so that** I can start managing its profile.

**Status:** COMPLETED

**Acceptance Criteria:**

- **Given** I am logged in, **when** I tap "Add pet" and fill in name and species (required), **then** the pet is created.
- **Given** I have 10 pets, **when** I try to add another, **then** I see a message that the limit has been reached.
- **Given** I fill in optional fields (breed, birthdate, weight, nickname), **when** I save, **then** all data is stored.
- **Given** I submit without a name or species, **when** I try to save, **then** I see validation errors.
- **Given** I want to upload an avatar, **when** I request a presigned URL, **then** I receive an upload URL that expires in 15 minutes.
- **Given** I have uploaded an avatar to S3, **when** I confirm the upload with the photo key, **then** the pet's photoUrl is updated.

**Notes:**

- Max 10 pets per user in MVP.
- Species: dog, cat (expandable post-MVP).
- Photo upload uses presigned URLs (client uploads directly to S3).
- Avatar workflow: request presigned URL → upload to S3 → confirm upload.
- Photo stored in S3 at `pets/{userId}/{petId}/{uuid}.{ext}`; `photoUrl` is the full S3 URL.
- `userId` is set automatically from the JWT — not user input.

**Implementation Summary:**

All acceptance criteria have been implemented:

**Pet Registration:**
- `POST /api/pets` endpoint with JWT authentication
- JSON request body (no multipart)
- Species enum (DOG, CAT) with validation
- Full validation suite in `RegisterPetUseCase`:
  - Name, species (required)
  - Age >= 0
  - Weight > 0 (if provided)
  - Birthdate not in future (if provided)
  - Pet limit: 10 per user
- Database schema includes all fields (birthdate, weight, nickname, photo_url)

**Avatar Upload Flow:**
- `POST /api/pets/{petId}/avatar/presign` — Generate presigned URL for upload
  - Validates user owns the pet
  - Supports image/jpeg and image/png
  - Returns presigned URL with 15-minute expiration
- `POST /api/pets/{petId}/avatar/confirm` — Confirm upload and update pet
  - Validates user owns the pet
  - Verifies photo exists in S3
  - Updates pet's photoUrl field

**S3 Integration:**
- S3 client uses AWS SDK default credential provider chain
- Region resolved via `DefaultAwsRegionProviderChain`
- Only `bucket-name` configured in application.yaml
- `PhotoStorageGateway` methods: `generatePresignedUrl`, `verifyPhotoExists`, `buildPhotoKey`, `buildPhotoUrl`

**Error Handling:**
- Proper HTTP status codes (400, 401, 404, 409, 500)
- Domain exceptions mapped to error codes
- Structured ErrorResponse DTO

---

## Current Task

**Status:** Awaiting next user story

All planned features for the initial MVP have been implemented:
- Pet registration with validation
- Avatar upload via presigned URLs
- Pet profile editing with ownership verification
- Pet soft-delete with idempotent behavior
- List user's pets (active pets only)

**Recent Updates:**
- Implemented US-010 List User's Pets with GET /api/pets endpoint
- Created ListPetsUseCase and PetListResponse DTO
- Implemented US-009 Delete Pet with soft-delete pattern and partial indexes
- Added `deleted_at` field to Pet domain model and database schema
- Updated all queries to exclude soft-deleted pets
- Added GitHub Actions workflow for PR tests (`.github/workflows/pr-tests.yml`)
- Updated coding standards with "Conditional Guards with filter + switchIfEmpty" pattern
- Updated agent skills: backend-developer.md and code-reviewer.md with new patterns
- Branch: `feat/us-010-list-all-pets` (current)
