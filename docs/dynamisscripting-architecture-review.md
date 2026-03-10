# DynamisScripting Architecture Review

Date: 2026-03-10  
Scope: Deep boundary ratification for `DynamisScripting` (review/documentation only)

## 1. Repo Overview

Observed modules:

- `scripting-api`
- `scripting-spi`
- `scripting-dsl`
- `scripting-canon`
- `scripting-oracle`
- `scripting-chronicler`
- `scripting-percept`
- `scripting-society`
- `scripting-economy`
- `scripting-runtime`
- `scripting-ashford`

Observed implementation shape:

- `scripting-api` defines core contracts (`IntentBus`, `PerceptBus`, `WorldOracle`, `CanonLog`, `Chronicler`) and canonical value models (`Intent`, `WorldEvent`, `CanonEvent`, `CanonTime`, etc.).
- `scripting-runtime` assembles and drives tick orchestration (`RuntimeBuilder`, `RuntimeTick`, `ScriptingRuntime`), event wiring, patch/seed hooks, and agent degradation tracking.
- `scripting-oracle`, `scripting-chronicler`, `scripting-canon`, `scripting-percept` implement world-law/time/canon/percept pipelines.
- `scripting-society` and `scripting-economy` add domain simulation dimensions.
- `scripting-ashford` is a demo vertical slice/data assembly module.

Dependency signals from poms/module-info:

- Depends directly on `DynamisCore` and `DynamisEvent`.
- No direct code dependencies on `DynamisWorldEngine`, `DynamisSession`, `DynamisContent`, `DynamisLocalization`, `DynamisECS`, or `DynamisSceneGraph`.
- Internal modules export many concrete implementation packages (not only API-level contracts).

## 2. Strict Ownership Statement

### What DynamisScripting should own

- Script/runtime-host boundary and execution context for authored scripting logic.
- Script-facing contracts for intent submission and percept consumption.
- Deterministic script-rule evaluation and script error/reporting boundaries.
- Narrow facades that let scripts consume world/content/session/localization data without owning those subsystems.

### What is appropriate for a scripting subsystem

- Binding/API exposure layers.
- Script runtime lifecycle at script engine scope.
- Rule expression/DSL compilation/execution support.
- Script-scoped plugin/extension points (`scripting-spi`).

### What it must never own

- World authority and world-scope orchestration as a competing source of truth to `DynamisWorldEngine`.
- ECS ownership, scene hierarchy ownership, or render policy.
- Session persistence authority.
- Runtime content authority and localization authority.
- Feature-domain monolith ownership that turns scripting into a generalized gameplay/world engine.

## 3. Dependency Rules

### Allowed dependencies for DynamisScripting

- `DynamisCore` for shared base types/exceptions.
- `DynamisEvent` for event transport mechanics.
- Narrow contract dependencies for consumed systems (world/session/content/localization) via facades only.

### Forbidden dependencies for DynamisScripting

- Direct ownership dependencies into lower-level substrate internals (ECS storage internals, SceneGraph internals, render/GPU internals).
- Taking over persistence/session/profile state authority.
- Owning runtime content catalogs/localization catalogs as authoritative stores.

### Who may depend on DynamisScripting

- Higher-level world/gameplay orchestration that wants script execution as one subsystem.
- Feature layers needing script execution interfaces.

### Boundary requirement

- `DynamisScripting` should be consumed as a runtime binding/execution subsystem under world authority, not as the top-level world authority itself.

## 4. Public vs Internal Boundary

### Canonical public surface (recommended)

- `scripting-api` contracts and value types.
- Minimal subset of `scripting-runtime` facade types needed to assemble/run scripting.
- `scripting-spi` extension contracts.

### Internal/implementation areas (should remain internal)

- Concrete Oracle/Chronicler/Canon/Percept/Economy/Society implementations.
- Demo assembly and authored scenario scaffolding (`scripting-ashford`).
- Runtime wiring internals (`RuntimeBuilder` graph/phase assembly details).

### Boundary concern

- Current module exports expose many concrete implementation packages (`oracle`, `chronicler`, `canon`, `percept`, `economy`, `society`, runtime concrete classes).
- This broad public surface risks freezing policy-heavy implementation details as de facto API.

## 5. Policy Leakage / Overlap Findings

## Major clean boundaries confirmed

- No direct dependencies on Session/Content/Localization/WorldEngine repos; this avoids immediate hard coupling.
- Event usage is through `DynamisEvent` abstractions.
- Internal module decomposition is coherent and technically disciplined.

## Policy leakage / overlap identified

- **Major overlap risk with `DynamisWorldEngine`:** repo explicitly claims canonical world-state authority and includes world tick orchestration (`RuntimeTick`, `ScriptingRuntime`, `DefaultWorldOracle`, `DefaultChronicler`). This directly competes with the ratified WorldEngine boundary unless constrained to “simulation policy engine under WorldEngine authority.”
- **Potential overlap with gameplay orchestration ownership:** economy/society/chronicler/oracle modules embed high-level simulation policy and domain behavior, not just scripting runtime bindings.
- **Public API overexposure risk:** broad package exports make policy-heavy internals externally consumable, increasing boundary drift.
- **Session/Content/Localization relation currently under-specified:** no direct coupling now, but no explicit contract boundary in code/docs for how these authorities should be consumed via facades.

## 6. Ratification Result

**Judgment: needs boundary tightening**

Why:

- The repository is internally coherent, but current scope is broader than a scripting/runtime binding layer.
- It currently operates as a world-simulation authority stack (oracle/chronicler/canon/percept/economy/society + tick orchestration), which conflicts with the already-ratified `DynamisWorldEngine` ownership line unless explicitly subordinated.
- Public surface breadth increases long-term architectural lock-in risk.

## 7. Recommended Next Step

1. Ratify an explicit split of responsibilities between `DynamisWorldEngine` and `DynamisScripting`:
   - WorldEngine: world authority/orchestration owner.
   - Scripting: simulation policy/rule execution subsystem invoked by WorldEngine.
2. Define facade contracts for `Session`, `Content`, and `Localization` consumption so scripting does not absorb their authority.
3. Next repo to review: **DynamisUI** (to clarify scripting/UI/control-plane boundary and prevent policy drift at presentation/runtime interaction edges).

---

This document is a boundary-ratification review artifact. It does not perform refactors in this pass.
