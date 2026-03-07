# Project Summary

Pet App is a web application for registering and managing pets. Users can create profiles for their pets, keep track of their details, and upload avatar photos.

> **Status:** In active development. Not yet publicly deployed.
> The public URL will be added here once the project is officially launched.

---

## Current functionalities

### Frontend

Built with React 19 + TypeScript + Vite, following Clean Architecture (feature-first).

#### Pet cards (list view)

- Displays all pets belonging to the authenticated user as a card grid
- Each card shows the pet's photo, name, species, and breed
- Filter by species (All / Dogs / Cats)
- Search by name or breed
- Button to register a new pet

#### Pet detail

- Full profile view for a single pet
- Shows: name, species, breed, age, birthdate, weight, nickname, registration date, and avatar photo
- Accessible from the pet card

#### Pet form (create & update)

- Single form used for both registering a new pet and editing an existing one
- Fields: name (required), species (required: DOG / CAT), breed, age (required), birthdate, weight, nickname
- Validation on required fields and data types

---

### Backend

Built with Kotlin + Spring WebFlux + Clean Architecture. All endpoints require a JWT bearer token.

#### Pets — `/api/pets`

| Method   | Endpoint                        | Description                                                   |
|----------|---------------------------------|---------------------------------------------------------------|
| `GET`    | `/api/pets`                     | List all active pets for the authenticated user               |
| `POST`   | `/api/pets`                     | Register a new pet (max 10 per user)                          |
| `PUT`    | `/api/pets/{petId}`             | Update all mutable fields of a pet (owner only)               |
| `DELETE` | `/api/pets/{petId}`             | Soft-delete a pet (owner only, idempotent)                    |
| `GET`    | `/api/pets/{petId}/detail`      | Get full pet profile (owner only, excludes soft-deleted pets) |

#### Avatar — `/api/pets/{petId}/avatar`

Photo upload uses a two-step S3 presigned URL flow:

| Method | Endpoint                           | Description                                                              |
|--------|------------------------------------|--------------------------------------------------------------------------|
| `POST` | `/api/pets/{petId}/avatar/presign` | Generate a presigned S3 URL (expires in 15 min, JPEG/PNG only)           |
| `POST` | `/api/pets/{petId}/avatar/confirm` | Confirm the upload and link the photo URL to the pet profile             |

#### Business rules

- Maximum **10 pets per user**
- Species supported: `DOG`, `CAT`
- Age must be ≥ 0; weight must be > 0 if provided; birthdate cannot be in the future
- Soft delete: deleted pets are hidden but not removed from the database
- Avatar accepted formats: `image/jpeg`, `image/png`

#### Authentication

All endpoints require a `Authorization: Bearer <JWT>` header. The token must contain a `user_id` claim. Signature validation is delegated to an upstream identity provider.
