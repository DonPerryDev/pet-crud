# Database Schema

## Connection

- **Database:** PostgreSQL 15
- **Access:** R2DBC (reactive, non-blocking)
- **URL:** `r2dbc:postgresql://localhost:5435/local?currentSchema=petapp`
- **Schema:** `petapp`

## Current Tables

### pets (inferred from PetData entity)

| Column | Type | Nullable | Description |
|--------|------|----------|-------------|
| id | String | No (auto) | Primary key |
| name | String | No | Pet name |
| species | String | No | Animal species |
| breed | String | Yes | Breed (null for mixed) |
| age | Int | No | Pet age |
| owner | String | No | Owner identifier |
| registration_date | LocalDate | No | Date of registration |

**Entity class:** `infrastructure/postgres-db/src/main/kotlin/com/donperry/persistence/pet/entities/PetData.kt`

> **Note:** Verify this schema against the actual database. The above is inferred from the domain model and entity classes.

## Schema Template

When adding a new table:

```sql
CREATE TABLE IF NOT EXISTS {entity}s (
    id VARCHAR(36) PRIMARY KEY DEFAULT gen_random_uuid()::varchar,
    -- columns
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);
```

## R2DBC Patterns

### Repository
```kotlin
interface {Entity}Repository : ReactiveCrudRepository<{Entity}Data, String>
```

### Custom Queries
```kotlin
interface {Entity}Repository : ReactiveCrudRepository<{Entity}Data, String> {
    @Query("SELECT * FROM {entity}s WHERE {column} = :value")
    fun findBy{Column}(value: String): Flux<{Entity}Data>
}
```

### Migrations

> **TODO:** Document the migration strategy for this project (Flyway, Liquibase, or manual SQL scripts).
