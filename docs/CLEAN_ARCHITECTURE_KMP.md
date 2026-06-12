# Clean Architecture in KMP

The template uses package boundaries inside a single `shared` Gradle module.

```text
domain -> data -> presentation -> ui
```

## Responsibilities

- `domain`: models and repository contracts; no framework dependencies.
- `data`: datasources, mappers, and repository implementations.
- `presentation`: ViewModels, immutable UI state, and UI effects.
- `ui`: shared Compose rendering and user callbacks.
- `database`: SQLDelight setup and platform driver abstractions.
- `di`: Koin wiring.

Use cases are optional. Add one only when it contains policy, validation, composition, or reusable
business logic. Direct repository delegation does not justify a use case.

## Enforced Boundaries

- Domain cannot import outer layers.
- Data cannot import presentation or UI.
- Presentation cannot import data or database.
- UI and navigation cannot import domain or data.
- `commonMain` cannot use direct platform dispatchers or system time.

Run `./scripts/check-layer-boundaries.sh` to validate these rules.
