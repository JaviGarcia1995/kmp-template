# SQLDelight Architecture

The notes sample uses one `Note` table and no seed data.

```text
NotesRepository
    <- SqlDelightNotesRepositoryImpl
    <- NotesLocalDataSource
    <- SqlDelightNotesDataSource
    <- AppDatabaseQueries
```

## Rules

- Domain does not know SQLDelight types.
- Datasources only perform data access.
- Repositories map local records to domain models and coordinate timestamps.
- SQL work uses `DispatcherProvider.io`.
- Platform drivers live in `androidMain` and `iosMain`.
- Schema changes must keep queries, mappers, DI, and tests aligned.

The starter schema has no migrations because a generated project starts from a clean database.
