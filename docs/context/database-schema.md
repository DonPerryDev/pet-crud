# Database Schema

## Connection

- **Database:** PostgreSQL 15
- **Access:** R2DBC (reactive, non-blocking)
- **URL:** `r2dbc:postgresql://localhost:5435/local?currentSchema=petapp`
- **Schema:** `petapp`

## Current Tables

### pets

| Column | Type | Nullable | Description |
|--------|------|----------|-------------|
| id | UUID | No (auto) | Primary key |
| name | VARCHAR | No | Pet name |
| species | VARCHAR | No | Animal species (DOG, CAT) |
| breed | VARCHAR | Yes | Breed (null for mixed) |
| age | INT | No | Pet age in years |
| birthdate | DATE | Yes | Pet birthdate |
| weight | DECIMAL(10,2) | Yes | Pet weight |
| nickname | VARCHAR(255) | Yes | Pet nickname |
| owner | VARCHAR | No | Owner user ID (from JWT) |
| registration_date | DATE | No | Date of registration |
| photo_url | VARCHAR(500) | Yes | S3 URL for pet photo |

**Entity class:** `infrastructure/postgres-db/src/main/kotlin/com/donperry/persistence/pet/entities/PetData.kt`
**Migrations:** `db/migrations/V1__create_pets_table.sql`

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

Located in `db/migrations/`:
- `V1__create_pets_table.sql` — Initial pets table with all fields (id, name, species, breed, age, birthdate, weight, nickname, owner, registration_date, photo_url)

Migration strategy: SQL scripts in `db/migrations/` directory (convention-based versioning managed by Flyway).
