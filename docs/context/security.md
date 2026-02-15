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
| `ALLOWED_ORIGINS` | CORS allowed origins | `http://localhost:3000,http://localhost:3005` |
| `MANAGEMENT_PORT` | Actuator port | `9090` |

## Input Validation

### Current State
- Request DTOs use Kotlin data classes with non-nullable fields for required values
- Nullable fields (`String?`) for optional values
- No Jakarta validation annotations detected yet

### Recommended Pattern
```kotlin
data class {Action}{Entity}Request(
    @field:NotBlank(message = "Name is required")
    val name: String,

    @field:Min(value = 0, message = "Age must be non-negative")
    val age: Int,

    val optional: String? = null
)
```

## Authentication & Authorization

> **TODO:** No authentication mechanism detected. When implementing:
> - Consider JWT validation in a WebFilter
> - Extract user identity from token
> - Apply role-based access control per route

## Secrets Handling

- Use environment variables for all secrets
- Never commit secrets to version control
- Docker Compose uses environment variables for database credentials
- Application defaults are for local development only

## Security Checklist for New Endpoints

- [ ] Input validated (non-null, type-safe, length/range constraints)
- [ ] No PII in log messages
- [ ] SQL injection prevented (use R2DBC parameterized queries)
- [ ] CORS policy reviewed
- [ ] Authentication required (if applicable)
- [ ] Authorization checked (if applicable)
- [ ] Error responses don't leak internal details
