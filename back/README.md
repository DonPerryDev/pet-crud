# Pet App

Pet registration and management API built with Kotlin, Spring WebFlux, and Clean Architecture.

## Prerequisites

- JDK 21 or higher
- Gradle
- Docker & Docker Compose

## Getting Started

### 1. Start infrastructure (Postgres + migrations)

```shell
docker compose -f applications/pet/docker-compose.yml up -d
```

This starts three services in order:

1. **postgres** — PostgreSQL 15 database (port `5435`)
2. **flyway** — Runs all SQL migrations from `db/migrations/`, then exits
3. **pet-app** — Spring Boot application (port `8080`) — starts only after Flyway completes

### 2. Run the application locally (without Docker app container)

If you prefer running the app from your IDE or terminal, start only Postgres and Flyway:

```shell
docker compose -f applications/pet/docker-compose.yml up postgres flyway -d
```

Then run the app:

```shell
./gradlew :pet:bootRun
```

### 3. Build

```shell
./gradlew clean build
```

### 4. Run tests

```shell
./gradlew test
```

## Database Migrations

Migrations are managed with **Flyway** and run as an isolated Docker container — Flyway is **not** embedded in the application.

### How it works

- Migration files live in `db/migrations/` at the project root
- Flyway runs as a separate Docker service before the app starts
- The `pet-app` service depends on Flyway completing successfully (`service_completed_successfully`)
- Flyway tracks applied migrations in the `petapp.flyway_schema_history` table

### Adding a new migration

1. Create a new SQL file in `db/migrations/` following the naming convention:

   ```
   V{number}__{description}.sql
   ```

   Example: `V3__add_vaccination_table.sql`

2. Write your SQL using explicit schema references:

   ```sql
   CREATE TABLE petapp.vaccinations (
       id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
       pet_id UUID NOT NULL REFERENCES petapp.pets(id),
       vaccine_name VARCHAR(255) NOT NULL,
       applied_date DATE NOT NULL
   );
   ```

3. Run the migration:

   ```shell
   docker compose -f applications/pet/docker-compose.yml up flyway
   ```

### Re-running migrations

Flyway only applies new (unapplied) migrations. To re-run:

```shell
# Re-run migrations against the existing database
docker compose -f applications/pet/docker-compose.yml up flyway

# Full reset: destroy volumes and start fresh
docker compose -f applications/pet/docker-compose.yml down -v
docker compose -f applications/pet/docker-compose.yml up -d
```

### Current migrations

| Version | File | Description |
|---------|------|-------------|
| V1 | `V1__create_pets_table.sql` | Creates `petapp` schema and `pets` table with all columns |

### Connection details (local)

| Property | Value |
|----------|-------|
| Host | `localhost` |
| Port | `5435` |
| Database | `local` |
| Schema | `petapp` |
| Username | `petapp` |
| Password | `petapp123` |
