# Pet App

Pet registration and management application. React frontend + Kotlin/Spring Boot backend.

---

## Requirements

| Tool       | Version  | Install                          |
|------------|----------|----------------------------------|
| Java       | 21       | [sdkman.io](https://sdkman.io) or [Homebrew](https://brew.sh): `brew install openjdk@21` |
| Node.js    | 20+      | [nodejs.org](https://nodejs.org) or `brew install node` |
| Docker     | 24+      | [Docker Desktop](https://www.docker.com/products/docker-desktop/) (Mac) |
| Git        | any      | `brew install git` |

> **macOS**: install Homebrew first if you don't have it: `/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"`

---

## Running locally

### Option 1 — Full stack with Docker (recommended)

Builds both apps and starts all services in one command:

```bash
./start.sh
```

| Service  | URL                                     |
|----------|-----------------------------------------|
| Frontend | http://localhost:3000                   |
| Backend  | http://localhost:8080                   |
| Health   | http://localhost:9090/management/health |

```bash
# Stop all services
docker compose down
```

### Option 2 — Backend + Frontend separately

**Backend** (requires local Postgres):

```bash
cd back

# Start Postgres
docker compose -f applications/pet/docker-compose.yml up -d

# Run the API
./gradlew :pet:bootRun
```

**Frontend** (in a separate terminal):

```bash
cd front

# Create local env file with dev token (first time only)
echo "VITE_DEV_TOKEN=<your-jwt>" > .env.local

npm install
npm run dev       # http://localhost:5173
```

---

## Environment variables

### Backend

| Variable              | Default                                                         | Description          |
|-----------------------|-----------------------------------------------------------------|----------------------|
| `R2DBC_URL`           | `r2dbc:postgresql://localhost:5435/local?currentSchema=petapp` | Database URL         |
| `R2DBC_USERNAME`      | `petapp`                                                        | DB username          |
| `R2DBC_PASSWORD`      | `petapp123`                                                     | DB password          |
| `ALLOWED_ORIGINS`     | `http://localhost:3000,...`                                     | CORS allowed origins |
| `MANAGEMENT_PORT`     | `9090`                                                          | Actuator port        |
| `AWS_S3_BUCKET_NAME`  | `pet-app-photos`                                                | S3 bucket for photos |

### Frontend

| Variable          | Default                 | Description                             |
|-------------------|-------------------------|-----------------------------------------|
| `VITE_API_URL`    | `http://localhost:8080` | Backend API URL                         |
| `VITE_DEV_TOKEN`  | —                       | Hardcoded JWT for dev (skipped if unset) |

---

## Project structure

```
pet-app-kt/
├── back/              # Kotlin + Spring WebFlux API (Clean Architecture)
├── front/             # React 19 + TypeScript + Vite SPA
├── build.sh           # Build both apps locally
├── start.sh           # Build + start full stack via Docker
└── docker-compose.yml
```

---

## Backend

**Stack:** Kotlin 2.1.10, Java 21, Spring Boot 3.4.3, WebFlux (reactive), R2DBC, PostgreSQL 15, AWS S3, Spring Security (JWT OAuth2)

```bash
cd back

./gradlew clean build              # Build
./gradlew test                     # Run all tests
./gradlew testWithMergedCoverage   # Coverage report
```

### Architecture

| Layer        | Module         | Path                              |
|--------------|----------------|-----------------------------------|
| Domain Model | `:model`       | `domain/model/`                   |
| Use Cases    | `:usecase`     | `domain/usecase/`                 |
| REST API     | `:rest`        | `infrastructure/entrypoint-rest/` |
| Persistence  | `:persistence` | `infrastructure/postgres-db/`     |
| REST Client  | `:client-rest` | `infrastructure/client-rest/`     |
| S3 Storage   | `:s3-storage`  | `infrastructure/s3-storage/`      |
| Bootstrap    | `:pet`         | `applications/pet/`               |

---

## Frontend

**Stack:** React 19, TypeScript 5.9, Vite 7, Tailwind CSS 4, Zustand 5

```bash
cd front

npm run dev       # Dev server with HMR at http://localhost:5173
npm run build     # Production build to dist/
npm run typecheck # Type check only
npm run lint      # ESLint
npm run check     # Typecheck + lint
```
