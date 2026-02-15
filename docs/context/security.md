# Security

## Current Configuration

### CORS
- **Config class:** `CorsGlobalConfig.kt` in `infrastructure/entrypoint-rest/`
- **Allowed origins:** Configurable via `app.security.allowed-origins` property
- **Default origins:** `http://localhost:3000`, `http://localhost:3005`

### Environment Variables
| Variable | Purpose | Default |
|----------|---------|---------|
| `R2DBC_URL` | Database connection | `r2dbc:postgresql://localhost:5435/local?currentSchema=petapp` |
| `R2DBC_USERNAME` | Database user | `petapp` |
| `R2DBC_PASSWORD` | Database password | `petapp123` |
| `JWT_ISSUER_URI` | JWT token issuer | `https://dev-example.auth0.com/` |
| `JWT_JWK_SET_URI` | JWK Set endpoint | `https://dev-example.auth0.com/.well-known/jwks.json` |
| `AWS_S3_BUCKET_NAME` | S3 bucket for photos | `pet-app-photos` |
| `ALLOWED_ORIGINS` | CORS allowed origins | `http://localhost:3000,http://localhost:3005` |
| `MANAGEMENT_PORT` | Actuator port | `9090` |

## Input Validation

### Current Implementation
- Business validation performed in use case layer (see `RegisterPetUseCase`, `GenerateAvatarPresignedUrlUseCase`, `ConfirmAvatarUploadUseCase`)
- Validations:
  - User ID: cannot be blank
  - Pet name: cannot be blank
  - Age: must be >= 0
  - Weight: must be > 0 if provided
  - Birthdate: cannot be in the future
  - Pet limit: max 10 pets per user
  - Pet ownership: user must own the pet to upload/confirm avatar
  - Content type: only image/jpeg and image/png allowed for avatar
  - Photo key: must match expected S3 key format (pets/{userId}/{petId}/...)
- Domain exceptions thrown for validation failures (see `model/exception/DomainException.kt`):
  - `ValidationException`
  - `PetLimitExceededException`
  - `PetNotFoundException`
  - `PhotoNotFoundException`
  - `UnauthorizedException`

## Authentication & Authorization

### Current Implementation
- **Framework:** Spring Security OAuth2 Resource Server with JWT
- **Config class:** `SecurityConfig.kt` in `applications/pet/src/main/kotlin/com/donperry/app/configuration/`
- **Protected endpoints:**
  - `POST /api/pets` — Requires valid JWT token (`.authenticated()`)
  - `POST /api/pets/{petId}/avatar/presign` — Requires valid JWT token and pet ownership
  - `POST /api/pets/{petId}/avatar/confirm` — Requires valid JWT token and pet ownership
- **Public endpoints:**
  - `/management/**` — Health checks and actuator endpoints
- **User extraction:** `ReactiveSecurityContextHolder.getContext().authentication.name` provides user ID
- **Token validation:** Automatic via Spring Security using JWK Set from `JWT_JWK_SET_URI`

### Security Rules
- All `/api/pets` endpoints require authentication
- Management endpoints are publicly accessible
- CSRF is disabled (stateless API)
- User ID is extracted from JWT, not from request body

## AWS Configuration

### S3 Client
- **Authentication:** Uses default AWS SDK credential provider chain (environment variables, instance profile, ~/.aws/credentials)
- **Region:** Resolved via `DefaultAwsRegionProviderChain` (environment, config file, instance metadata)
- **Configuration:** `S3ClientConfig` uses `S3AsyncClient.create()` and `S3Presigner.create()` with default settings
- **Required config:** Only `aws.s3.bucket-name` in application.yaml

## Secrets Handling

- Use environment variables for all secrets
- Never commit secrets to version control
- Docker Compose uses environment variables for database credentials
- Application defaults are for local development only
- AWS credentials are managed via AWS SDK default credential provider chain (no explicit config in application.yaml)

## Security Checklist for New Endpoints

- [ ] Input validated (non-null, type-safe, length/range constraints)
- [ ] No PII in log messages
- [ ] SQL injection prevented (use R2DBC parameterized queries)
- [ ] CORS policy reviewed
- [ ] Authentication required (if applicable)
- [ ] Authorization checked (if applicable)
- [ ] Error responses don't leak internal details
