# Skill: Documentation Specialist

Keeps project documentation synchronized with the codebase state.

## Documentation Inventory

| Document | Update When |
|----------|------------|
| `CLAUDE.md` | New modules, architecture changes, new build commands |
| `docs/architecture.md` | New modules, dependency changes, new entry points |
| `docs/coding-standards.md` | New conventions adopted, pattern changes |
| `docs/scaffolding.md` | Template improvements from real implementations |
| `docs/current-work.md` | Any implementation work (update module status) |
| `docs/context/api-design.md` | New endpoints, changed routes, new DTOs |
| `docs/context/database-schema.md` | New tables, schema changes, migrations |
| `docs/context/security.md` | Auth changes, new environment variables |
| `docs/context/observability.md` | Logging pattern changes, new metrics |

## Sync Protocol

### For API Changes
1. Grep for `router {` and route definitions in `infrastructure/entrypoint-rest/`
2. Compare against `docs/context/api-design.md`
3. Update endpoint table, request/response schemas

### For Module Changes
1. Scan for new `*Gateway.kt`, `*UseCase.kt`, `*Adapter.kt`, `*Handler.kt`
2. Compare against `docs/architecture.md` module map
3. Update module table and dependency flow

### For Schema Changes
1. Scan for new `*Data.kt` entities in `infrastructure/postgres-db/`
2. Compare against `docs/context/database-schema.md`
3. Update table definitions

## Checklist

- [ ] All current endpoints documented in `api-design.md`
- [ ] Module map in `architecture.md` matches actual directories
- [ ] Dependency flow diagram is accurate
- [ ] Module status table in `current-work.md` is up to date
- [ ] Tech stack versions match `build.gradle.kts`
- [ ] Build commands in `CLAUDE.md` work correctly
