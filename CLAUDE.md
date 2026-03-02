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

## Conventions

- Group ID: `org.dynamisscripting`
- Package root: `org.dynamisscripting.*`
- Use Conventional Commits (`feat:`, `fix:`, `docs:`, etc.)
- Keep canonical boundaries strict: no cognition state mutations outside DynamisAI
