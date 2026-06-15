# KMP Template Agent Instructions

## Stack

- Kotlin Multiplatform for Android and iOS
- Compose Multiplatform with shared UI
- Clean Architecture by packages in `shared`
- MVVM with `StateFlow`
- Koin
- SQLDelight
- Coroutines and Flow

## Architecture

```text
domain -> data -> presentation -> ui
```

- Repository interfaces belong in `domain/repository`.
- Repository implementations and datasources belong in `data`.
- ViewModels and UI state belong in `presentation`.
- Compose screens remain render-only and depend on presentation contracts.
- Navigation destinations are typed and serializable.
- Do not add use cases that only delegate to a repository.
- Do not introduce generic abstractions without a current consumer.

## Multiplatform Rules

- `commonMain` must not import Android or iOS APIs.
- Use `DispatcherProvider` instead of direct platform dispatchers.
- Use `TimeProvider` instead of reading system time directly.
- Keep platform implementations in `androidMain` and `iosMain`.
- Keep the Compose Resources bridge in `androidApp/build.gradle.kts`. AGP 9 consumes
  local Android-KMP projects as JARs, so shared Compose assets must be added to the
  application through the Variant API. Verify the final APK contains both default
  and localized `strings.commonMain.cvr` files after changing this integration.

## Dependencies

- Declare versions and libraries in `gradle/libs.versions.toml`.
- Use version catalog aliases from Gradle build files.
- Shared networking is not installed by default.
- If networking is added, prefer Ktor for shared KMP code. Do not use Retrofit in `commonMain`.

## Validation

Run the narrowest applicable checks after each iteration:

```bash
./scripts/check-layer-boundaries.sh
./gradlew :shared:testAndroidHostTest
./gradlew :shared:detektShared
./gradlew :shared:compileAndroidMain
./gradlew :androidApp:assembleDebug
./gradlew :shared:compileKotlinIosSimulatorArm64
```

Do not advance to a new phase while the current platform gate is failing.
