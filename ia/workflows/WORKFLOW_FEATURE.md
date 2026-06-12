# WORKFLOW_FEATURE

## Objective
Standardize feature implementation (with functional detail and optional image) while preserving architecture, reuse, and simplicity.

## Input Contract
1. Functional objective.
2. In scope and out of scope.
3. User flow (steps/navigation).
4. UI states (loading/error/empty/success).
5. Data to read/save (if applicable).
6. Optional mock/image.

If context is missing: assume reasonable defaults and state assumptions.
If uncertainty affects architecture, contracts, persistence, or functional behavior, ask before implementing.

## Plan Mode (mandatory)
Plan by blocks, from lower risk to higher risk:
1. UI/components
2. presentation (ViewModel/state/validation)
3. domain (contracts/models)
4. data/persistence/DI

Each block must include: change, simplification rationale, pros/cons, risk.

## Process Guardrails (anti-overengineering)
Always apply before implementing:
1. **Simplify first**:
   - Prioritize deleting/reusing before creating.
   - Do not add new layers unless strictly necessary.
2. **Structural expansion control**:
   - Do not create a new file if the change fits clearly in an existing file.
   - Do not create a new public `data class`/mapper/helper without at least 2 real consumers or a clear readability benefit.
3. **No passthrough wrappers**:
   - Avoid functions/classes that only delegate calls without real logic.
4. **Minimal API**:
   - ViewModel public methods should be only for UI intents.
   - Internal helpers and technical setters should be `private`.
5. **Minimal state**:
   - Keeping UI state that is not rendered or used is forbidden.
6. **Explicit structural cost**:
   - Report in final output: created/deleted files and added/removed public types/functions.
   - For each created file/type, include: concrete benefit, simpler alternative discarded, and reason.
7. **Split threshold (mandatory)**:
   - Do not create new files unless at least one condition is true.
   - Conditions:
   - readability of current file is clearly degraded (guide threshold: ~350-400 lines hard to scan), or
   - there are at least 2 real consumers, or
   - split measurably improves readability of main flow (`Screen`/`ViewModel`).
8. **Cross-flow consistency**:
   - For sibling screens or destinations, evaluate alignment when changing shared patterns.
   - If not aligned, include explicit rationale.
9. **No private micro-abstractions**:
   - Avoid extracting one-line private helpers that do not reduce parent cognitive load.

## SOLID (pragmatic, not dogmatic)
Apply SOLID as a decision aid, never as a mandatory source of extra structure.
1. If strict SOLID compliance increases accidental complexity without clear present value, prioritize the simpler design.
2. Do not introduce interfaces, wrappers, mappers, or extra layers only to "satisfy SOLID" when there is no real variability or reuse need.
3. Before adding abstractions, explicitly justify:
   - concrete current problem,
   - simpler alternative discarded,
   - why extra structural cost is worth it now.
4. Prefer local, incremental improvements over broad theoretical redesigns.
5. If a SOLID principle is intentionally relaxed, document the trade-off and why it reduces risk/complexity.

## Implementation Gates

### Architecture and KMP
- Respect layers: `domain -> data -> presentation -> ui`.
- `presentation/ui` must not access `data`.
- `ViewModel` only in `presentation`.
- `commonMain` without platform imports.
- `DispatcherProvider` and `TimeProvider` are mandatory when applicable.
- UI must remain render-focused: do not keep business validation/orchestration/model-mapping in composables.

### Reuse and Simplicity
- Reuse components before creating new ones.
- Justify any new component.
- Typography must use global `TypeScale` styles as default source of truth.
- Avoid `sp` hardcoded values in UI text unless strictly justified.
- Avoid `UseCase`/1:1 gateways without real logic.
- Avoid fragmented contracts/wrappers without value.
- Keep a single source of truth for validation/navigation.
- Remove unused legacy within the approved scope.
- Always prioritize the simplest solution that meets requirements.
- Avoid designing for unrequested hypothetical scenarios (no over-anticipation).
- If two options are valid, choose the one with lower accidental complexity.
- Keep related screens structurally consistent when that reduces cognitive load.
- Prefer UI contracts shaped as `State + callbacks + Modifier`.

### Blocked Anti-patterns
- Designing for unrequested hypothetical scenarios.
- Duplicating the same logic in Screen/ViewModel/Repository if it can live in one place.
- Introducing one-line mappers/abstractions without semantic value.
- Blind SOLID compliance that creates extra types/layers without measurable gain.
- Keeping `UiState` fields/flags that are not used in render.
- Double trigger of the same intent (example: double dismiss, double emit).
- Creating micro-fragmented files with one trivial function if it can be private/local.

### `UseCase` Criteria
- Create a `UseCase` only if it adds real business logic, source composition, non-trivial validation, or policy.
- If it only delegates to repository and adds no behavior, do not create it.

### Persistence (if applicable)
- `Repository` centralizes coordination, logic, and threading.
- `DataSource` only pure data access.
- Schema changes with `.sq` + `.sqm` migration.
- Keep mappers, builders/params, queries, DI, and tests aligned.

## Definition of Done
1. Build/compile for affected module passes.
2. Architecture/DI rules remain intact.
3. Tests added/adjusted or explicit justification provided.
4. Documentation updated if behavior/contract changed.

## Minimum Validation
1. `./scripts/check-layer-boundaries.sh`
2. `./gradlew :shared:build` (or equivalent focused compilation)
3. Tests for affected feature
4. If SQLDelight is involved: builders/params/repository tests
5. Manual verification of main flow

## Standard Output
1. Applied plan
2. Changes by layer
3. Simplicity decisions
4. SOLID trade-offs (where strict SOLID was intentionally relaxed to avoid overengineering)
5. Pros/Cons of key decisions
6. Executed validation
7. Residual risks

## Readability Checklist (mandatory)
Before closing the task, verify:
1. Main flow is understandable by reading only `Screen` + `ViewModel` without unnecessary jumps.
2. Public functions use intent-oriented names (`onXxx...`).
3. Parent methods do not accumulate technical detail; extract private helpers if readability improves.
4. No dead parameters/state.
5. No avoidable files or types were added.
6. Any divergence from sibling flow structure is intentional and documented.
7. ViewModel file does not mix excessive responsibilities without justification:
   - if `ViewModel + UiState + Step` harms scanability, split `UiState/Step` to dedicated file.

## Decision Rules & Guardrails
- If there are relevant doubts, ask before executing sensitive changes.
- Do not make breaking decisions by assumption (schema/models/navigation/public contracts).
- If additional scope appears during implementation, document as pending and do not include without confirmation.
- Explicitly declare `breaking change: yes/no` in final output.

## Blocker: Standard Question
When a critical decision is missing, use this format:
```text
Blocker detected:
- Pending decision: [topic]
- Options: [A] vs [B]
- Recommendation: [A/B] because [short reason]
Do you confirm I should continue with [option]?
```

## Commit Rule
- Group commits by topic/functional block (without mixing unrelated refactors).
- Exclude changes in `docs/`, `ia/`, and `.idea/` by default, unless explicitly requested.

## Minimum Manual QA
1. Complete main feature flow.
2. Visible and recoverable error path.
3. Persistence/reopen behavior (if applicable).
4. Forward/back navigation without losing critical state.

## Minimum Output Artifacts
1. Archivos modificados por capa (`domain/data/presentation/ui/di`).
2. Executed validation: commands + result (`OK`/`FAIL`) + reason if something was not run.
3. Impact: `breaking change: yes/no`; if `yes`, concrete impact and applied migration/compatibility.
4. Simplicity decisions: what was simplified/removed and what was intentionally not added to avoid overengineering.
5. Structural-cost report for every created file/type and why it beat the simpler alternative.

## Start Prompt
```text
Apply WORKFLOW_FEATURE to implement [feature].
Context: [detail].
Scope: [in scope].
Out of scope: [out of scope].
Mock/image: [optional].
Prioritize simplicity and avoid overengineering.
```
