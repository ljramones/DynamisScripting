# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

This is a Java 25 Maven multi-module project. No external dependencies beyond the JDK yet.

```bash
mvn compile                          # compile all modules
mvn test                             # run all tests
mvn test -pl scripting-spi           # run tests for a single module
mvn test -pl scripting-spi -Dtest=ArbitrationRuleContractTest  # run a single test class
mvn package                          # build JARs
mvn clean install                    # full clean build + install to local repo
```

No linter or formatter is configured yet. The compiler targets Java 25 (`maven.compiler.release=25`).

## Architecture Overview

DynamisScripting is the scripting layer for the Dynamis game engine stack. It implements a **three-force simulation model**: **Law** (Oracle) + **Time** (Chronicler) + **Mind** (DynamisAI, separate repo). The canonical reference is `docs/adr.md` (ADR-001, frozen).

### The Core Invariant

- Only the **Scripting Engine** may mutate canonical world state.
- Only **DynamisAI** may mutate internal agent cognition.
- Causality chain: `AI → Canon → Graph` — never `AI → Graph` directly.

### Module Dependency Layers

```
scripting-api          Public interfaces (WorldOracle, CanonLog, IntentBus, PerceptBus, etc.)
scripting-spi          Extension points (requires api) — CanonDimensionProvider, ArbitrationRule,
                       PerceptFilter, ChroniclerNodeArchetype, SocietyVectorDimension, etc.
scripting-dsl          Predicate & rewrite DSL (requires api)
scripting-canon        CanonLog implementation + time (requires api, spi)
scripting-oracle       Oracle: Validate → Shape → Commit pipeline (requires api, spi, dsl, canon)
scripting-chronicler   Chronicler: narrative graph, triggers, archetypes (requires api, spi, dsl, canon)
scripting-percept      PerceptBus: deterministic canon-to-agent view filtering (requires api, spi)
scripting-society      Society vectors, cultural distance, drift (requires api, spi)
scripting-economy      Contracts, bounties, faction funds, cost tables (requires api, spi)
scripting-runtime      Wires all modules together; tick loop, degradation monitor (requires all above)
scripting-ashford      Demo/testbed scenario (requires runtime only)
```

All modules use Java Platform Module System (`module-info.java`). Each module exports a single package matching its artifact name.

### Event-Driven Topology

Cross-boundary communication is strictly event-driven — no direct method calls across module boundaries at the architecture level.

```
IntentStream     →  Oracle  →  CanonLog
WorldEventStream →  Oracle  →  CanonLog
CanonLog         →  PerceptBus  →  Agents (filtered)
CanonLog         →  Chronicler  (graph predicate evaluation)
```

### Key Design Rules

- **Oracle** (scripting-oracle) arbitrates via a three-phase pipeline: Validate → Shape → Commit. It never sets beliefs, emotions, or cognition.
- **Chronicler** (scripting-chronicler) proposes WorldEvents to Oracle — it never commits directly. Graph trigger predicates must be pure functions of canon state (no belief-state triggers).
- **PerceptBus** (scripting-percept) is a view layer that may filter, delay, downsample, or redact — but never introduces new facts.
- **Budgets** are Oracle-owned canon constraints (world physics), not AI tuning knobs. Budget exhaustion must be delivered as a percept, not a silent failure.
- **DSL** (scripting-dsl) is declarative-first: pure predicate expressions over canon state, bounded aggregates over CanonLog history, and rewrite rules for Oracle shaping. No loops, no mutation, no IO.
- **Society vectors** (scripting-society) are continuous weighted dimension profiles per faction; interaction outcomes are deterministic functions of vector relationships.
- **CanonLog** is append-only with `commitId`, `canonTime`, `causalLink`, and typed world deltas. It enables replay, branching, and causal debugging.

### Time Model

- **CanonTime** (Oracle): simulation truth — CanonTime never waits.
- **AgentTime** (DynamisAI): agent cadence, may lag behind.
- **WallTime**: never used for simulation truth.

### Current State

All source files are empty stubs (interfaces/classes with no methods). The SPI module has empty contract test stubs. The architecture is documented and frozen; implementation follows.

## Conventions

- Group ID: `org.dynamisscripting`
- Package pattern: `org.dynamisscripting.<module-name>` (e.g., `org.dynamisscripting.oracle`)
- SPI tests use the naming pattern `*ContractTest` for provider contract verification
- Use Conventional Commits (e.g., `feat:`, `fix:`, `docs:`)
