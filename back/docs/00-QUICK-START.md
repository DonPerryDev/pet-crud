# Quick Start

## Documentation Index

| Document | Load When | Description |
|----------|-----------|-------------|
| `architecture.md` | Starting new work or onboarding | Module structure, dependency flow, entry points |
| `coding-standards.md` | Writing or reviewing code | Naming, patterns, reactive rules, logging |
| `scaffolding.md` | Creating a new entity/feature | Code templates for each architecture layer |
| `current-work.md` | Resuming work or checking status | Active branch, recent commits, module status |
| `context/api-design.md` | Adding or modifying endpoints | Endpoint inventory and design patterns |
| `context/database-schema.md` | Working with persistence layer | Schema definitions and migration patterns |
| `context/security.md` | Handling auth, validation, or secrets | Security patterns and checklist |
| `context/observability.md` | Adding logging or metrics | Logger setup, log format, level guidelines |
| `skills/backend-developer.md` | Implementing features | Developer workflow checklist |
| `skills/code-reviewer.md` | Reviewing code | Review categories and common issues |
| `skills/test-developer.md` | Writing tests | Test patterns and coverage targets |
| `skills/documentation-specialist.md` | Updating docs | Documentation sync protocol |
| `skills/logging-specialist.md` | Auditing logs | Logging violations and fixes |
| `guides/creating-new-agents.md` | Creating new AI agents | Agent template and model selection |
| `guides/creating-new-skills.md` | Creating new skill checklists | Skill template and linking to agents |

## Commands

| Command | Description |
|---------|-------------|
| `./gradlew clean build` | Full build with tests and coverage |
| `./gradlew :pet:bootRun` | Run the application |
| `./gradlew test` | Run all tests |
| `./gradlew :model:test` | Test domain model module |
| `./gradlew :usecase:test` | Test use case module |
| `./gradlew :rest:test` | Test REST entrypoint module |
| `./gradlew :persistence:test` | Test persistence module |
| `./gradlew :client-rest:test` | Test REST client module |
| `./gradlew testWithMergedCoverage` | Run tests with merged JaCoCo report |
| `docker compose -f applications/pet/docker-compose.yml up -d` | Start PostgreSQL |

## Workflow Shortcuts

| Shortcut | What It Does |
|----------|-------------|
| `/new-feature <desc>` | Architect → Implement → Test → Review |
| `/update-feature <desc>` | Find existing → Architect delta → Implement → Test → Review |
| `/review [scope]` | Structured code review with severity ranking |
| `/sync-docs [scope]` | Sync documentation with codebase state |
| `/clean-comments` | Remove redundant WHAT comments, keep WHY comments |
