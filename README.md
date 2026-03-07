# Pet App

Pet registration and management application. React frontend + Kotlin/Spring Boot backend.

## Structure

```
pet-app-kt/
├── back/       # Kotlin + Spring WebFlux API (Clean Architecture)
├── front/      # React 19 + TypeScript + Vite SPA
├── build.sh    # Build both apps locally
├── start.sh    # Build + start full stack via Docker
└── docker-compose.yml
```

## Quick Start

### Full stack with Docker

```bash
# Build both apps and start all services
./start.sh
```

| Service  | URL                                      |
|----------|------------------------------------------|
| Frontend | http://localhost:3000                    |
| Backend  | http://localhost:8080                    |
| Health   | http://localhost:9090/management/health  |

```bash
# Stop all services
docker compose down
```

### Build only (no Docker)

```bash
./build.sh
```

---

## Backend

Kotlin 2.1.10, Java 21, Spring Boot 3.4.3, WebFlux (reactive), R2DBC, PostgreSQL 15, AWS S3, Spring Security (JWT OAuth2).

```bash
cd back

# Run (requires local Postgres — see back/applications/pet/docker-compose.yml)
docker compose -f applications/pet/docker-compose.yml up -d
./gradlew :pet:bootRun

# Build
./gradlew clean build

# Test
./gradlew test

# Coverage
./gradlew testWithMergedCoverage
```

### Architecture

| Layer        | Module       | Path                          |
|--------------|-------------|-------------------------------|
| Domain Model | `:model`     | `domain/model/`               |
| Use Cases    | `:usecase`   | `domain/usecase/`             |
| REST API     | `:rest`      | `infrastructure/entrypoint-rest/` |
| Persistence  | `:persistence` | `infrastructure/postgres-db/` |
| REST Client  | `:client-rest` | `infrastructure/client-rest/` |
| S3 Storage   | `:s3-storage`  | `infrastructure/s3-storage/`  |
| Bootstrap    | `:pet`       | `applications/pet/`           |

---

## Frontend

React 19, TypeScript 5.9, Vite 7, Tailwind CSS 4, Zustand 5.

```bash
cd front

npm install
npm run dev       # Dev server with HMR at http://localhost:5173
npm run build     # Production build to dist/
npm run typecheck # Type check only
npm run lint      # ESLint
```

The API base URL is configured via the `VITE_API_URL` environment variable (defaults to `http://localhost:8080`).

---

## Environment Variables

### Backend

| Variable         | Default                                          | Description              |
|------------------|--------------------------------------------------|--------------------------|
| `R2DBC_URL`      | `r2dbc:postgresql://localhost:5435/local?currentSchema=petapp` | Database URL |
| `R2DBC_USERNAME` | `petapp`                                         | DB username              |
| `R2DBC_PASSWORD` | `petapp123`                                      | DB password              |
| `ALLOWED_ORIGINS`| `http://localhost:3000,...`                      | CORS allowed origins     |
| `MANAGEMENT_PORT`| `9090`                                           | Actuator port            |
| `AWS_S3_BUCKET_NAME` | `pet-app-photos`                             | S3 bucket for photos     |

### Frontend

| Variable       | Default                  | Description       |
|----------------|--------------------------|-------------------|
| `VITE_API_URL` | `http://localhost:8080`  | Backend API URL   |
