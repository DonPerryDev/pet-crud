# Creating New Skills

## When to Create
Create when a recurring role needs project-specific checklists.
Don't create for one-off tasks or when an existing skill can be extended.

## Skill Template

Place in `docs/skills/{role-name}.md`:

```
# Skill: [Role Name]

[1-2 sentence description]

## Checklist

- [ ] [Actionable item]
- [ ] [Actionable item]

## Common Issues

| Issue | Fix |
|-------|-----|
| [Problem] | [Solution] |
```

## Linking to Agents

In the agent definition (`.claude/agents/{name}.md`):
```
**Before Starting:**
1. Read `docs/skills/{skill-name}.md` for your checklist
```

## Guidelines

- Include project-specific patterns, not generic advice
- Keep checklists actionable and scannable
- Reference `docs/coding-standards.md` for patterns instead of duplicating
