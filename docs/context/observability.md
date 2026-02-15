# Observability

## Logger Setup

### Declaration
Every class that logs must declare the logger in a `companion object`:

```kotlin
companion object {
    private val logger: Logger = Logger.getLogger(ClassName::class.java.name)
}
```

### Logger Type
- Use `java.util.logging.Logger` (project standard)
- Forbidden: `println`, `System.out`, `System.err`, SLF4J

## Log Message Format

### Pattern
```
[$identifier] {Action description}
```

### Examples
```kotlin
logger.info("[$petId] Starting pet registration")
logger.info("[$petId] Pet registered successfully")
logger.warning("[$petId] Failed to register pet: ${error.message}")
logger.fine("[$petId] Pet details - name=$name, species=$species")
```

## Log Levels by Layer

| Layer | Level | When |
|-------|-------|------|
| Handler | INFO | Request received, response sent |
| Handler | WARNING | Request processing failed |
| Handler | FINE | Request/response details |
| UseCase | INFO | Operation start, operation success |
| UseCase | WARNING | Business rule violation, operation failure |
| UseCase | FINE | Input parameters, intermediate state |
| Adapter | INFO | External call made, response received |
| Adapter | WARNING | External call failed |
| Adapter | FINE | Request/response payloads |

## Reactive Logging Operators

```kotlin
// Log on success
.doOnNext { logger.info("[$id] Operation completed") }

// Log on error
.doOnError { error -> logger.warning("[$id] Failed: ${error.message}") }

// Log on subscribe (operation start)
.doOnSubscribe { logger.info("[$id] Starting operation") }
```

## Metrics

### Spring Actuator
- Health endpoint: `GET /management/health` (port 9090)
- Base path: `/management`
- Exposed endpoints: `health`

> **TODO:** Consider enabling additional actuator endpoints (`metrics`, `prometheus`) for production monitoring.

## Checklist for New Code

- [ ] Logger declared in companion object
- [ ] Using `java.util.logging.Logger` (not SLF4J, not println)
- [ ] All log messages include `[$identifier]` bracket pattern
- [ ] INFO at operation boundaries (start + success)
- [ ] WARNING at failure points
- [ ] FINE for debug/parameter details
- [ ] No PII in log messages (no emails, passwords, tokens)
- [ ] Error logging uses `doOnError` (not try-catch)
