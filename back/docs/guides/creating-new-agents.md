# Creating New Agents

## When to Create
Create when a specialized role needs consistent, repeatable behavior.
Don't create for one-off tasks or when an existing agent handles it.

## Agent Template

Place in `.claude/agents/{name}.md`:

```
---
name: agent-name
description: One-line description.
model: sonnet
color: green
---

You are a [Role] for this project.

**Before Starting:**
1. Read `docs/skills/{related-skill}.md` for your checklist

**Core Responsibilities:**
- [Primary responsibility]

**Strict Constraints:**
- NEVER [forbidden action]
- ALWAYS [required action]

**Workflow:**
1. [Step 1]
2. [Step 2]

**Output Format:**
[Structured output description]
```

## Model Selection

| Model | Use For |
|-------|---------|
| `haiku` | Fast lookups, simple scans |
| `sonnet` | Most agents (default) |
| `opus` | Complex architectural decisions |

## Agent Boundaries

Each agent owns a specific domain:
- requirement-architect — Defines WHAT (no code)
- backend-developer — PRODUCTION code (no tests)
- test-engineer — TESTS only (no production code)
- code-reviewer — Reviews (no writing)
- docs-specialist — DOCUMENTATION (no code)
- logging-specialist — OBSERVABILITY (logging/metrics)
- code-sanitizer — COMMENT CLEANUP (no code changes)
