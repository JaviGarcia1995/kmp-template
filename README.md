# KMP Template

Starter project for Android and iOS using Kotlin Multiplatform and shared Compose UI.

## Current Sample

The template includes a small persistent notes feature with:

- Typed list and editor destinations
- `StateFlow`-based ViewModel
- Repository contract in `domain`
- SQLDelight datasource and repository implementation in `data`
- Koin dependency injection
- Shared Compose UI
- Representative ViewModel and repository tests

## Modules

- `shared`: domain, data, presentation, shared UI, SQLDelight, and platform implementations
- `androidApp`: Android application host
- `iosApp`: SwiftUI application host with direct KMP framework integration

## Validation

```bash
./scripts/check-layer-boundaries.sh
./gradlew :shared:testDebugUnitTest
./gradlew :shared:detektShared
./gradlew :androidApp:assembleDebug
./gradlew :shared:compileKotlinIosSimulatorArm64
./gradlew :shared:linkDebugFrameworkIosSimulatorArm64
```

The default identity is intentionally generic:

- Project: `KmpTemplate`
- Package: `com.example.kmptemplate`

An interactive setup script will replace these placeholders in a later phase.
