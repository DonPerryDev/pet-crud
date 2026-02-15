# Skill: Logging Specialist

Audits and fixes logging practices across the codebase.

## Audit Items

- [ ] All classes use `java.util.logging.Logger` in `companion object`
- [ ] All log messages include `[$identifier]` bracket pattern
- [ ] INFO at operation boundaries (start + success)
- [ ] WARNING at failure points
- [ ] FINE for debug details
- [ ] No PII in log messages
- [ ] No `println` or `System.out` in production code
- [ ] Error logging uses `doOnError` (not try-catch)

## Common Violations

| Violation | Severity | Fix |
|-----------|----------|-----|
| `println("message")` | HIGH | `logger.info("[$id] message")` |
| `System.out.println(...)` | HIGH | `logger.info("[$id] ...")` |
| `logger.info("Saving pet")` | MEDIUM | `logger.info("[$petId] Saving pet")` |
| `logger.info("Email: $email")` | HIGH | Remove PII, use `logger.info("[$userId] Sending email")` |
| Logger as instance field | LOW | Move to `companion object` |
| Using SLF4J `LoggerFactory` | LOW | Replace with `java.util.logging.Logger.getLogger(...)` |
| `catch (e: Exception) { log }` | MEDIUM | Use `.doOnError { logger.warning(...) }` |

## Debug Commands

```bash
# Find all println calls
grep -rn "println" --include="*.kt" domain/ infrastructure/ applications/

# Find all System.out calls
grep -rn "System.out" --include="*.kt" domain/ infrastructure/ applications/

# Find loggers not in companion object
grep -rn "val logger" --include="*.kt" domain/ infrastructure/ applications/

# Find log messages without brackets
grep -rn 'logger\.\(info\|warning\|fine\)(' --include="*.kt" domain/ infrastructure/ applications/

# Find potential PII in logs
grep -rn 'logger.*email\|logger.*password\|logger.*token' --include="*.kt" domain/ infrastructure/ applications/
```
