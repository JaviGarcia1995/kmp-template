# Contributing

Contributions that keep the template small, understandable, and useful for Android and iOS are
welcome.

## Before You Start

- Open an issue before making a large architectural or dependency change.
- Keep changes focused and independently reviewable.
- Do not add abstractions, modules, or dependencies without a current use case.
- Do not include secrets, signing files, `local.properties`, IDE metadata, or build artifacts.

## Development Setup

You need:

- JDK 17
- Android Studio and an Android SDK
- Xcode on macOS for iOS validation

Clone the repository and configure your local Android SDK through `local.properties` or
`ANDROID_HOME`.

Do not run `scripts/setup-template.sh` when contributing to the source template. That script is
intended for repositories created with **Use this template**.

## Architecture Rules

The shared code follows:

```text
domain -> data -> presentation -> ui
```

- Repository contracts belong in `domain/repository`.
- Datasources and repository implementations belong in `data`.
- ViewModels and UI state belong in `presentation`.
- Compose UI remains render-only.
- `commonMain` must not import platform APIs.
- Use `DispatcherProvider` and `TimeProvider` instead of direct platform access.
- Do not add use cases that only delegate to a repository.
- If shared networking is introduced, use Ktor rather than Retrofit in `commonMain`.

See [`AGENTS.md`](AGENTS.md) and [`docs/`](docs/README.md) for the complete project conventions.

## Template Compatibility

When adding or renaming a file that contains the project name, package, application ID, bundle ID,
or generated resource package, update `scripts/setup-template.sh`.

Test identity-related changes in a repository created from the template or in a separate Git
worktree. Do not execute the setup script in the source template checkout.

## Validation

Run the narrowest checks relevant to your change. Before opening a pull request, run:

```console
$ ./scripts/check-layer-boundaries.sh
$ ./gradlew :shared:testDebugUnitTest
$ ./gradlew :shared:detektShared
$ ./gradlew :shared:compileDebugKotlinAndroid
$ ./gradlew :androidApp:assembleDebug
$ ./gradlew :shared:compileKotlinIosSimulatorArm64
$ ./gradlew :shared:linkDebugFrameworkIosSimulatorArm64
```

If an iOS check cannot be run because macOS or Xcode is unavailable, state that explicitly in the
pull request.

## Pull Requests

A pull request should:

- explain the problem and the chosen solution;
- remain limited to one logical change;
- include tests for changed behavior where practical;
- keep Android and iOS buildable;
- update documentation when behavior or setup changes;
- list validations that were run and any remaining limitations.

