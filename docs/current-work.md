# Current Work

## User Story: Add a New Pet

**As a** user, **I want to** add a new pet with basic info and upload an avatar, **so that** I can start managing its profile.

### Acceptance Criteria

- **Given** I am logged in, **when** I tap "Add pet" and fill in name and species (required), **then** the pet is created.
- **Given** I have 10 pets, **when** I try to add another, **then** I see a message that the limit has been reached.
- **Given** I fill in optional fields (breed, birthdate, weight, nickname), **when** I save, **then** all data is stored.
- **Given** I submit without a name or species, **when** I try to save, **then** I see validation errors.
- **Given** I want to upload an avatar, **when** I request a presigned URL, **then** I receive an upload URL that expires in 15 minutes.
- **Given** I have uploaded an avatar to S3, **when** I confirm the upload with the photo key, **then** the pet's photoUrl is updated.

### Entity: Pet

### Notes

- Max 10 pets per user in MVP.
- Species: dog, cat (expandable post-MVP).
- Photo upload uses presigned URLs (client uploads directly to S3).
- Avatar workflow: request presigned URL → upload to S3 → confirm upload.
- Photo stored in S3 at `pets/{userId}/{petId}/{uuid}.{ext}`; `photoUrl` is the full S3 URL.
- `userId` is set automatically from the JWT — not user input.

---

## Current Task

### Implementation Summary

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
- `PhotoStorageGateway` methods: `generatePresignedUrl`, `verifyPhotoExists`, `buildPhotoUrl`

**Error Handling:**
- Proper HTTP status codes (400, 401, 404, 409, 500)
- Domain exceptions mapped to error codes
- Structured ErrorResponse DTO
