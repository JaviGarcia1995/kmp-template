# REFACTOR_WORKFLOW

## Objective
Refactor template code to improve readability, reduce accidental complexity, and remove overengineering while preserving functional behavior.

## Input Contract
1. Exact scope (features/packages/files).
2. Refactor goal (readability, simplification, file reduction, etc.).
3. Constraints (what must not be changed).
4. Success criteria.
5. Mandatory validations.

If context is missing: assume reasonable defaults and state assumptions.  
If uncertainty affects public contracts, persistence, navigation, or functional behavior, ask before implementing.

## Core Rule
- **No functional regression**: do not change observable behavior unless explicitly requested.
- **Simplicity first**: delete/reuse before adding.
- **No auto-commit**: the agent must not run `git commit` or `git push`; only propose a commit message.

## Plan Mode (mandatory)
Plan by blocks, from lower risk to higher risk:
1. Scope inventory
2. Local simplification (functions/readability)
3. Structural reduction (file deletion/fusion)
4. Public API adjustments and final cleanup

Each block must include:
1. proposed change
2. simplification rationale
3. risk
4. simple rollback

## Process Guardrails
1. Do not create a new file if the change clearly fits an existing one.
2. Do not create new `data class`/mapper/wrapper without readability value or real reuse.
3. Do not add new layers to solve local issues.
4. UI must stay render-only:
   - move business decision/validation/orchestration to `presentation` (`ViewModel`/presentation helpers),
   - avoid keeping business sync logic in `LaunchedEffect` inside UI,
   - avoid UI-level mapping/normalization between permission/domain/presentation models.
5. Keep ViewModel public methods intent-oriented (`onXxx...`), technical helpers `private`.
6. Keeping dead UI state (not consumed by render) is forbidden.
7. Avoid passthrough wrappers (delegation without logic).
8. Explicit structural cost is mandatory before creating files/types:
   - Include in plan: concrete benefit, simpler alternative discarded, and why.
9. Do not split into new files unless at least one condition is true:
   - file readability is clearly degraded (guide threshold: ~350-400 lines hard to scan), or
   - there are at least 2 real consumers, or
   - split improves main flow readability (`Screen`/`ViewModel`) in a measurable way.
10. Keep consistency across sibling screens and related feature flows.
11. Avoid private micro-abstractions with no cognitive gain:
   - do not extract helpers that only rename a one-liner without reducing parent complexity.

## SOLID (pragmatic, not dogmatic)
Use SOLID as a quality lens, not as a trigger for extra architecture.
1. If strict SOLID compliance adds accidental complexity without clear practical benefit, prefer simpler code.
2. Do not add interfaces/wrappers/layers solely to satisfy SOLID when there is no real variability, reuse, or readability gain.
3. Before introducing abstractions, require explicit structural-cost justification:
   - concrete current pain,
   - simpler option rejected,
   - reason the added structure is worth it now.
4. Favor incremental refactors over broad theoretical redesigns.
5. When relaxing strict SOLID, document the trade-off and why it reduces complexity/risk.

## Blocked Anti-patterns
1. Premature abstraction for hypothetical scenarios.
2. Duplicating control flow between UI and ViewModel unnecessarily.
3. Double side effect for a single intent (double dismiss, double emit).
4. File micro-fragmentation with trivial one-use helpers.
5. Massive refactor without limits and incremental validation.
6. Blind SOLID compliance that introduces extra types/layers without measurable gain.

## File Reduction (mandatory to evaluate)

### Deletion criteria
Delete file if:
1. it has 0 usages, or
2. it only contains passthroughs with no logic, or
3. it contains a trivial one-use helper that can be local `private`.

### Fusion criteria
Fuse files if:
1. they always change together,
2. they belong to the same flow/screen,
3. one file only contains a few helper functions,
4. fusion reduces mental context switches without creating an unmanageable file.

### Safety limits
1. Do not mix layers (`domain/data/presentation/ui`) in the same file.
2. Do not break `expect/actual` or multiplatform separation.
3. Avoid “monster files”; split if readability clearly worsens.

## Change Budget
Define per iteration:
1. maximum files to modify,
2. maximum approximate line changes,
3. explicitly out-of-scope areas.
4. maximum new files/types allowed in this iteration (default: 0 unless justified).

If exceeded, split into iterations and close a stable version first.

## Definition of Done
1. Equivalent functional behavior (except approved changes).
2. Compilation passes for the affected module.
3. No architecture/layer violations.
4. Structural complexity lower than or equal to baseline.
5. `breaking change: yes/no` explicitly reported.
6. For each created file/type, structural-cost justification is explicitly reported.

## Minimum Validation
1. `./scripts/check-layer-boundaries.sh`
2. `./gradlew :shared:compileDebugKotlinAndroid`
3. `./gradlew :shared:detektShared`
4. Manual verification of the affected main flow.

## Standard Output
1. Applied plan.
2. Changes by layer.
3. Simplicity decisions (what was simplified and what was intentionally not added).
4. SOLID trade-offs (where strict SOLID was intentionally relaxed to avoid overengineering).
5. Deleted files (if applicable) + reason.
6. Fused files (if applicable) + reason.
7. Public API reduced/increased.
8. Executed validations and result.
9. Residual risks.
10. Proposed commit message (text only, without executing commit).

## Start Prompt
```text
Apply REFACTOR_WORKFLOW to refactor [scope] in the template.
Objective: [readability/simplification/file reduction].
Constraints: [no functional changes / others].
Mandatory validation: check-layer-boundaries + compile + detekt.
Do not auto-commit.
```
