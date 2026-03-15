# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

This is a Java 25 Maven multi-module project.

```bash
mvn compile                          # compile all modules
mvn test                             # run all tests
mvn test -pl scripting-spi           # run tests for a single module
mvn package                          # build JARs
mvn clean install                    # full clean build + install to local repo
```

Modules with quality gates can also be verified directly:

```bash
mvn -pl scripting-api -am verify
mvn -pl scripting-spi -am verify
mvn -pl scripting-dsl -am verify
mvn -pl scripting-canon -am verify
mvn -pl scripting-oracle -am verify
mvn -pl scripting-chronicler -am verify
```

## Architecture Overview

DynamisScripting is the scripting layer for the Dynamis game engine stack. It implements a three-force simulation model:

- Law (Oracle)
- Time (Chronicler)
- Mind (DynamisAI, separate repo)

The canonical reference is [docs/ADR-001.md](docs/ADR-001.md) and is frozen.

## Core Invariant

- Only the Scripting Engine may mutate canonical world state.
- Only DynamisAI may mutate internal agent cognition.
- Causality chain: `AI -> Canon -> Graph` (never `AI -> Graph` directly).

## Module Layers

- `scripting-api`: Contracts and value records.
- `scripting-spi`: Extension points and abstract contract tests.
- `scripting-dsl`: Predicate and rewrite DSL compilation/evaluation over canon context.
- `scripting-canon`: CanonLog implementation and timekeeper.
- `scripting-oracle`: Validate -> Shape -> Commit arbitration.
- `scripting-chronicler`: Proactive narrative graph evaluation and WorldEvent proposals.
- `scripting-percept`: Canon-to-agent percept derivation/filtering.
- `scripting-society`: Society vectors and interaction modeling.
- `scripting-economy`: Initial canonical dimension vertical slice.
- `scripting-runtime`: Tick loop composition and integration.
- `scripting-ashford`: Demo world wiring.

## DSL Validation Note

`DslValidator` currently enforces sandbox rules using keyword scanning and simple regex checks. This is intentional for the scaffold phase.

- Implemented now: string/keyword checks for loops, mutation, IO, and wall-time usage.
- Deferred: full AST-based static analysis against parsed expression structure.

Any future AST-based validator must preserve current rejection categories and reason codes.

## Canon Time Invariant

`CanonTimekeeper` must never use wall-time APIs for simulation state progression.

- Forbidden for simulation logic: `System.currentTimeMillis()`, `System.nanoTime()`, `Instant`, and `LocalDateTime`.
- Canon time advances only via explicit deterministic inputs (`advance(deltaNanos)` and `advanceToTick(targetTick)`).
- `wallNanosAtInsert` in `CanonLogEntry` is telemetry-only and must never influence ordering, queries, arbitration, or replay logic.

## Oracle WorldEvent Bypass

`DefaultWorldOracle.commitWorldEvent()` intentionally bypasses `ValidatePhase` and `ShapePhase`.

- Rationale: `WorldEvent` inputs are Chronicler proposals that are pre-validated in Chronicler logic by design.
- Oracle still serializes them into CanonLog with canonical commit ordering and causal links.
- This bypass never grants direct canonical mutation authority to agents or non-Oracle systems.

## GraphLoader Deferral

`GraphLoader.loadFromYaml()` is intentionally a stub in the current phase.

- Current behavior: throws a descriptive `ChroniclerException` instructing callers to use `GraphLoader.buildManually()`.
- Rationale: YAML parsing and schema validation are deferred until the content pipeline task.
- Guardrail: Chronicler remains deterministic and testable with explicit in-memory graph definitions.

## Society Loader Deferral

`SocietyProfileLoader.loadFromYaml()` is intentionally a stub in the current phase.

- Current behavior: throws a descriptive `SocietyException` instructing callers to use `SocietyProfileLoader.build(...)`.
- Rationale: YAML parsing/schema validation for society content is deferred until the content pipeline task.

## Society Drift Constant

`CulturalDriftTracker.MAX_DRIFT_PER_TICK` is currently hardcoded to `0.01`.

- Current behavior: drifts exceeding this bound are rejected to prevent runaway canonical changes.
- Future improvement: move this constant to runtime configuration for world-specific tuning.

## Economy Cost Table Deferral

`ContractCostTable` hot-reload/parsing is intentionally partial in the current phase.

- Current behavior: `computeCost(...)` uses deterministic tier defaults and `applyPatch(...)` logs the applied patch version.
- Deferred: full data-driven/YAML-backed cost-table loading and atomic reload validation.

## Conventions

- Group ID: `org.dynamisengine.scripting`
- Package root: `org.dynamisengine.scripting.*`
- Use Conventional Commits (`feat:`, `fix:`, `docs:`, etc.)
- Keep canonical boundaries strict: no cognition state mutations outside DynamisAI

## Module Inventory

- `scripting-api`: Base contracts and value records used by all other modules.
- `scripting-spi`: Extension interfaces and result records for third-party customization.
- `scripting-dsl`: Deterministic predicate/rewrite DSL compiler, validator, and evaluator.
- `scripting-canon`: CanonLog storage/query/fork/replay and CanonTime ownership.
- `scripting-oracle`: Validate -> Shape -> Commit arbitration pipeline.
- `scripting-chronicler`: Narrative graph scheduler and WorldEvent proposal engine.
- `scripting-percept`: PerceptBus delivery/filtering/degradation/provenance layer.
- `scripting-society`: Society vectors, interaction modeling, and drift tracking.
- `scripting-economy`: Economics dimension, budgets, contracts, and vertical slice objects.
- `scripting-runtime`: Runtime assembly, tick orchestration, degradation monitoring, EventBus wiring.
- `scripting-ashford`: Demo world wiring and authored data-only content pack.

## Class Inventory

### scripting-api
- `CanonLog`: Canonical append/query/fork/replay API.
- `Chronicler`: Tick-driven WorldEvent proposer contract.
- `IntentBus`: Intent emission/subscription contract.
- `PerceptBus`: Agent percept subscription/delivery contract.
- `WorldOracle`: Validate/shape/commit arbitration contract.
- `CanonEvent`: Canonical commit value record.
- `CanonTime`: Simulation-only time record.
- `Intent`: Agent intent proposal value record.
- `Percept`: Agent percept value record derived from canon.
- `WorldEvent`: Chronicler world-event proposal value record.
- `WorldPatch`: Hot-reload patch descriptor record.

### scripting-spi
- `ArbitrationRule`: Oracle validation/shaping extension point.
- `CanonDimensionProvider`: Canonical dimension lifecycle extension point.
- `ChroniclerNodeArchetype`: Chronicler archetype instantiation extension point.
- `IntentInterceptor`: Oracle pre/post pipeline interception hook.
- `PerceptFilter`: Custom percept delivery/degradation hook.
- `SocietyVectorDimension`: Society-axis definition and interaction contribution contract.
- `WorldPatchValidator`: WorldPatch validation extension point.
- `PatchValidationResult`: WorldPatch validation result record.
- `ShapeOutcome`: SPI shape-evaluation result record.
- `ValidationOutcome`: SPI validation-evaluation result record.

### scripting-dsl
- `CanonEvaluationContext`: Canon variable resolver used by DSL evaluation.
- `ClauseTrace`: Single-clause explain-trace record.
- `DslCompiler`: Cached expression compiler for predicate/rewrite DSL.
- `DslEvaluationException`: Runtime expression-evaluation failure exception.
- `DslExplainTrace`: Structured predicate explain-trace record.
- `DslExpression`: Sealed base type for compiled DSL expressions.
- `DslValidationException`: Static DSL validation failure exception.
- `DslValidator`: Static sandbox validator and variable extractor.
- `PredicateDsl`: Predicate evaluation API with optional traces.
- `PredicateExpression`: Compiled predicate expression record.
- `RewriteDsl`: Rewrite evaluation API producing optional shaped intents.
- `RewriteExpression`: Compiled rewrite expression record.

### scripting-canon
- `CanonLogEntry`: CanonEvent plus internal sequence/telemetry metadata.
- `CanonLogException`: CanonLog operation failure exception.
- `CanonLogFork`: Fork helper façade over CanonLog branching.
- `CanonLogQuery`: Static helper queries over CanonLog.
- `CanonLogReplay`: Replay helper façade over CanonLog re-emission.
- `CanonTimekeeper`: CanonTime owner/advancer with no wall-time simulation coupling.
- `DefaultCanonLog`: Thread-safe append-only CanonLog implementation.

### scripting-oracle
- `BudgetLedger`: Thread-safe world-budget registry and consumption tracker.
- `CommitPhase`: Canon commit phase and canonical event emission hook point.
- `DefaultWorldOracle`: Full validate-shape-commit orchestration implementation.
- `OracleException`: Oracle pipeline exception type.
- `OracleExplainReport`: Structured explain report for arbitration outcomes.
- `RuleLoader`: Rule-bundle loader stub/hook point.
- `RuleRegistry`: Ordered, thread-safe arbitration-rule registry.
- `ShapePhase`: Oracle shape-phase executor.
- `ValidatePhase`: Oracle validation-phase executor.

### scripting-chronicler
- `ArchetypeInstantiator`: Registered archetype evaluator/instantiator.
- `ChroniclerException`: Chronicler operation exception type.
- `ChroniclerScheduler`: Tick evaluation and activation throttling scheduler.
- `DefaultChronicler`: Chronicler implementation that emits WorldEvents only.
- `GraphLoader`: Quest graph loader (YAML deferred) and manual builder helper.
- `NodeState`: Story-node lifecycle enum.
- `QuestGraph`: In-memory story graph with node state tracking.
- `StoryNode`: Story-node definition record.
- `TriggerEvaluator`: DSL-backed trigger evaluator and trace producer.
- `WorldEventEmitter`: Listener-based WorldEvent emission bridge to Oracle.

### scripting-percept
- `DefaultPerceptBus`: Per-agent percept delivery/filter/degradation bus implementation.
- `FidelityLevel`: Percept fidelity tier enum.
- `FidelityModel`: Deterministic fidelity computation model.
- `PerceptContributionRegistry`: Cross-module percept contribution registry.
- `PerceptDelay`: Deterministic delay model by medium/distance.
- `PerceptDeliveryPolicy`: Agent-specific delivery policy record.
- `PerceptDownsampler`: Deterministic event-storm downsampling policy.
- `PerceptException`: PerceptBus exception type.
- `PerceptProvenanceChain`: Debug provenance record for delivered percepts.

### scripting-society
- `CulturalDriftTracker`: Canonical drift applier and drift-history query helper.
- `DimensionWeight`: Dimension weighting/parameter record for a society.
- `InteractionFunction`: Deterministic society-interaction computation engine.
- `InteractionMode`: Alignment/opposition/orthogonality enum.
- `InteractionOutcome`: Society-interaction result record.
- `SocietyException`: Society module exception type.
- `SocietyProfile`: Society vector profile record.
- `SocietyProfileLoader`: Profile loader stub (YAML deferred) and programmatic builder.
- `SocietyRegistry`: Thread-safe profile registry.

### scripting-economy
- `AvailabilityPool`: Thread-safe resource availability tracker.
- `BountyRecord`: Canonical bounty record.
- `Contract`: Canonical contract record.
- `ContractCostTable`: Deterministic cost lookup table with deferred full reload.
- `ContractState`: Contract/bounty lifecycle enum.
- `EconomicsArchetype`: Chronicler archetype for economic escalation events.
- `EconomicsBudgetRule`: Oracle arbitration rule for economic budget checks.
- `EconomicsDimension`: Canon-dimension provider for economics.
- `EconomyException`: Economy module exception type.
- `FactionFunds`: Immutable faction fund-balance record and operations.

### scripting-runtime
- `AgentCognitiveTier`: Cognitive degradation tier enum.
- `AgentTickDebt`: Agent lag snapshot record.
- `CanonLogEvent`: EventBus event wrapping committed CanonEvent.
- `DegradationMonitor`: Agent tier/debt tracker.
- `IntentBusImpl`: EventBus-backed IntentBus implementation.
- `IntentEvent`: EventBus event wrapping Intent.
- `RuntimeBuilder`: Fluent runtime assembler and wiring root.
- `RuntimeConfiguration`: Runtime configuration record and builder.
- `RuntimeException`: Runtime module exception type.
- `RuntimeTick`: Canonical tick executor.
- `RuntimeTickResult`: Tick execution result record.
- `ScriptingRuntime`: Runtime façade for ticking, patching, event access, and seeding.

### scripting-ashford
- `AshfordDebugOverlay`: Console debug presenter for runtime/canon/degradation state.
- `AshfordDemo`: Standalone demo entrypoint with wall-time pacing only.
- `AshfordRuntimeAssembler`: Programmatic demo-only runtime assembly and seeding.

## Architectural Invariants

1. Only Oracle may append to CanonLog via commit paths.
2. Chronicler never commits directly — proposals only via WorldEventEmitter.
3. PerceptBus never introduces new canonical facts — view layer only.
4. CanonTime never uses wall time — simulation time only.

## Deferred Items

- GraphLoader YAML loading.
- SocietyProfileLoader YAML loading.
- ContractCostTable full data-driven hot reload and validation.
- DslValidator AST-based static analysis (keyword scanning currently used).
- Full PerceptContributionRegistry integration with DynamisTerran/DynamisAudio/DynamisPhysics.

## Known Tooling Limitations

- `maven-dependency-plugin:dependency:analyze` fails on JDK 25 classfiles (`Unsupported class file major version 69`) in this workspace/toolchain.
- Release audits should use `mvn dependency:tree` (and `mvn dependency:list` if needed) until the plugin supports Java 25 bytecode.
