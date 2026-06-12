---
name: compose-multiplatform-screen-builder
description: Design and refactor screens in Compose Multiplatform with a focus on simplicity, composable reuse, and clean architecture. Use when asked to create/redesign screens, align UI with mocks/images, reorganize feature components, or reduce overengineering in ui/presentation without breaking behavior.
---

# Compose Multiplatform Screen Design

## Overview
Design Compose Multiplatform screens with a pragmatic flow: first reuse, then simplify, and only create new composables when they add real value.
Use the existing feature structure as the baseline and keep UI contracts minimal.

## Workflow
1. Read functional scope and visual reference (mock, screenshot, or description).
2. Review existing feature first before proposing a new structure.
3. Identify reusable composables in `ui/components/<feature>` and `ui/components/common`.
4. Define a short plan by blocks: `ui/components` -> `ui/screens` -> `presentation` (only if needed).
5. Implement minimal changes to match design and behavior; avoid massive rewrites.
6. Validate compilation and main navigation for affected screen.

## Minimum Input
Request or infer:
1. Target screen and expected behavior.
2. In scope and out of scope.
3. Required states: loading/error/empty/success.
4. Visual source (image or textual reference).
5. Architecture constraints (if any).

If critical persistence, public-contract, or global-navigation information is missing, ask before changing.

## Decision Rules
1. Reuse before creating: if an equivalent composable exists, adapt it.
2. Keep components feature-cohesive: avoid moving pieces to `common` without real use in 2+ features.
3. Avoid passthrough wrappers: do not create containers that only forward props without logic.
4. Reduce large function signatures using simple `StateProps/Actions`.
5. Avoid anticipation: do not introduce layers for hypothetical scenarios.

## Screen Baseline
1. Keep a clear hierarchy between top bar, content, and actions.
2. Make content scrollable when it can grow vertically.
3. Keep strings in Compose resources.
4. Prefer `State + callbacks + Modifier` contracts.
5. Extract components only when they improve scanability or have multiple consumers.

## Recommended Organization
1. Screens: `ui/screens/<feature>/...`
2. Feature components: `ui/components/<feature>/...`
3. Subgroups only when they provide real clarity (`steps/`, `summary/`, `guide/`, `layout/`, `navigation/`).
4. Avoid value-less micro-files (6-15 lines) if they fragment readability.

## Architecture Guardrails
1. Respect layers: `domain -> data -> presentation -> ui`.
2. Do not access `data` from `ui` or `presentation`.
3. Do not use platform imports in `commonMain`.
4. Keep ViewModels in `presentation`.

## Validation Checklist
Execute according to scope:
1. `./scripts/check-layer-boundaries.sh`
2. `./gradlew :shared:compileDebugKotlinAndroid`
3. Tests for affected feature if they exist.
4. Manual verification: navigation, scroll, main callbacks.

## Output Contract
Always deliver:
1. Applied changes per file.
2. Simplicity decisions (what was avoided to prevent overengineering).
3. Pros/cons of key decisions if trade-offs existed.
4. Validation status (`OK`/`FAIL`) and executed command.
5. `breaking change: yes/no`.
