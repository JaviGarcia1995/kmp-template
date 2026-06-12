# KmpTemplate

Starter template for Android and iOS using Kotlin Multiplatform and shared Compose UI.

## Use This Template

1. Select **Use this template** on GitHub and create a new repository.
2. Clone the new repository.
3. Run the interactive setup script from the project root:

```console
$ ./scripts/setup-template.sh
KMP Template setup

Project name (for example, MyNotes): SampleNotes
Base package (for example, com.example.mynotes): com.example.samplenotes

Project name: SampleNotes
Package:      com.example.samplenotes

Apply these changes? [y/N]: y
```

The script updates:

- Gradle project name
- Kotlin packages and source directories
- Android namespaces and application ID
- iOS bundle ID, display name, Swift entry point, and Xcode references
- SQLDelight package and source directory
- Compose resource imports
- Architecture scripts and documentation references

It requires a clean Git working tree, validates the supplied values, checks for path collisions,
audits remaining template references, and prevents accidental second execution.

## Local Android SDK

`local.properties` is intentionally ignored by Git. Android Studio normally creates it
automatically. If necessary, create it locally:

```properties
sdk.dir=/absolute/path/to/Android/sdk
```

Alternatively, define `ANDROID_HOME` before running Gradle from a terminal:

```console
$ export ANDROID_HOME="$HOME/Library/Android/sdk"
```

On Linux, the SDK is commonly located at `$HOME/Android/Sdk`.

## Included Sample

The template contains a small persistent notes feature demonstrating:

- Typed list and editor destinations
- MVVM with `StateFlow`
- Repository contract in `domain`
- SQLDelight datasource and repository implementation in `data`
- Koin dependency injection
- Shared Compose UI
- Representative ViewModel and repository tests

The sample is intentionally small and is designed to be replaced.

## Structure

- `shared`: domain, data, presentation, shared UI, SQLDelight, and platform implementations
- `androidApp`: Android application host
- `iosApp`: functional SwiftUI host with direct KMP framework integration

The architecture is organized by packages:

```text
domain -> data -> presentation -> ui
```

See [`docs/`](docs/README.md) for architecture details.

## Run Android

Open the project in Android Studio, select the `androidApp` run configuration, and run it on an
emulator or device.

From a terminal:

```console
$ ./gradlew :androidApp:assembleDebug
```

## Run iOS

Open `iosApp/iosApp.xcodeproj` in Xcode, select the `iosApp` scheme and an iPhone simulator, then
run the application.

The Xcode build phase invokes Gradle to build and embed the shared framework.

## Validation

Run the checks relevant to your change:

```console
$ ./scripts/check-layer-boundaries.sh
$ ./gradlew :shared:testDebugUnitTest
$ ./gradlew :shared:detektShared
$ ./gradlew :shared:compileDebugKotlinAndroid
$ ./gradlew :androidApp:assembleDebug
$ ./gradlew :shared:compileKotlinIosSimulatorArm64
$ ./gradlew :shared:linkDebugFrameworkIosSimulatorArm64
```

## Project Identity

The current project identity is:

- Project: `KmpTemplate`
- Package: `com.example.kmptemplate`

The setup script replaces these values throughout the repository.
