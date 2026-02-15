# Current Work

## User Story: Add a New Pet

**As a** user, **I want to** add a new pet with basic info, **so that** I can start managing its profile.

### Acceptance Criteria

- **Given** I am logged in, **when** I tap "Add pet" and fill in name and species (required), **then** the pet is created.
- **Given** I have 10 pets, **when** I try to add another, **then** I see a message that the limit has been reached.
- **Given** I fill in optional fields (breed, birthdate, weight, nickname, photo), **when** I save, **then** all data is stored.
- **Given** I submit without a name or species, **when** I try to save, **then** I see validation errors.

### Entity: Pet

### Notes

- Max 10 pets per user in MVP.
- Species: dog, cat (expandable post-MVP).
- Photo stored in S3; `photoUrl` saved in Postgres via Pet Service.
- `userId` is set automatically from the JWT — not user input.

---

## Current Task

### Scope

Implement `POST /pets` in Pet Service (Kotlin/Spring WebFlux).

### Tasks

- [ ] Create endpoint `POST /pets`
- [ ] Set `userId` from JWT (not from request body)
- [ ] Validate required fields: `name`, `species`
- [ ] Validate 10-pet limit per user
- [ ] Photo upload: accept multipart file, upload to S3 (`pets/` folder), store `photoUrl`
- [ ] Reject photo files > 5 MB
- [ ] Return created Pet entity
