---
name: cmp-svg-icon-pipeline
description: Integrate SVG icons into Compose Multiplatform projects and complete the per-platform pipeline (Android and iOS), including VectorDrawable conversion, shared UI wiring, Android launcher icon, and iOS AppIcon preparation/update. Use when the user provides an SVG and asks for an end-to-end operational icon with a simple, reusable, and non-overengineered approach.
---

# CMP SVG Icon Pipeline

## Overview
Process an input SVG and apply a standard flow so the icon is correctly integrated into Compose Multiplatform, Android, and iOS.
Prioritize minimal changes, naming consistency, and fast validation.

## Workflow
1. Confirm scope: shared UI icon, app launcher icon, or both.
2. Normalize icon name to `snake_case` and define target destination.
3. Convert SVG to XML vector for `composeResources/drawable` when shared UI applies.
4. Integrate icon in `CustomIcons` or `AppIcons` layer according to existing pattern.
5. Apply Android launcher steps (`mipmap`, adaptive icon, manifest) if app icon was requested.
6. Apply iOS steps (`Assets.xcassets/AppIcon.appiconset`) if iOS module exists.
7. Run minimum validation and report platform-specific pending items.

## Decision Rules
1. Reutilizar patrones existentes del repo antes de crear nuevos wrappers.
2. Do not create an additional mapping layer if the icon is only used from `CustomIcons`.
3. Keep resource name stable across all points (file, import, function).
4. If iOS module is missing in the repo, do not invent structure: leave an explicit pending checklist item.

## Platform Steps
See operational details and checklist in:
- [references/pipeline.md](references/pipeline.md)

## Validation Checklist
1. `./gradlew :shared:compileAndroidMain`
2. Verify generated imports/resources in `shared`.
3. If Android launcher changed: compile `androidApp` and verify icon.
4. If iOS changes were made and module is available: validate full `AppIcon.appiconset`.

## Output Contract
Always deliver:
1. Created/modified files.
2. What was applied in CMP/Android/iOS.
3. What remains pending (if module or platform assets are missing).
4. `breaking change: yes/no`.
