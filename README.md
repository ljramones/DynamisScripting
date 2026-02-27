# DynamisScripting

Scripting-layer architecture for **DynamisAI**, part of the Dynamis engine stack.

This repository defines the boundary between:
- **Scripting Engine** (world law + world time)
- **DynamisAI** (agent cognition)

Canonical reference: `docs/scriptingArch.md` (ADR-001, accepted on 2026-02-27).

## Core Invariant

- Only the **Scripting Engine** may mutate canonical world state.
- Only **DynamisAI** may mutate internal agent cognition.

Causality chain:

```text
AI -> Canon -> Graph
Never AI -> Graph directly.
```

## Architecture Model

### Scripting Engine

Two subsystems:

1. **Oracle (Reactive / Law)**
- Owns canonical state, rule arbitration, budgets, CanonTime, CanonLog commits
- Pipeline: `Validate -> Shape -> Commit`
- Never sets beliefs/emotions or overrides cognition

2. **Chronicler (Proactive / Time)**
- Owns world momentum, story spine, side quest graphs, deadlines, off-screen consequences
- Evaluates narrative graph every CanonTime tick
- Proposes `WorldEvent`s to Oracle (never commits directly)

### DynamisAI (Mind / Players)

Owns perception weighting, memory/beliefs, emotions, planning, dialogue, mistakes, rumor spread.

Must never:
- Write canonical world state
- Mutate narrative graph directly
- Invent percepts (can invent beliefs)

## Event-Driven Topology

```text
IntentStream     -> Oracle -> CanonLog
WorldEventStream -> Oracle -> CanonLog
CanonLog         -> PerceptBus -> Agents
CanonLog         -> Chronicler
CanonLog         -> Telemetry/Debug UI
```

No direct cross-boundary method calls.

## Cognition Contract

Four-layer model:

```text
Percept -> Interpretation -> Inference -> Rumor
```

Rule: agents may invent beliefs, never percepts.

## Time Contract

- **CanonTime** (Oracle): simulation truth
- **AgentTime** (DynamisAI): agent cadence
- **WallTime** (OS): never simulation truth

Async LLM outputs are treated as late packets and must be discarded/re-grounded when stale.

## CanonLog

Append-only canonical stream with:
- `commitId` (monotonic)
- `canonTime`
- `causalLink`
- typed world delta

Enables replay, branching, causal debugging, and deterministic percept derivation.

## Degradation Policy

CanonTime never waits.

Agents degrade gracefully from Tier 0 to Tier 3 when behind:
- Full cognition -> stale-safe behavior -> constitutional behavior -> diegetic cover actions
- At high staleness, high-impact irreversible actions are restricted unless grounded by immediate percepts

## Repository Layout

Current scaffold:
- `src/` application/runtime code
- `tests/` mirrored automated tests
- `assets/` fixtures/static files
- `docs/` design and architecture decisions

## Build, Test, Lint

Toolchain is not initialized yet. Expected command surfaces:
- `make build` (or stack equivalent)
- `make test`
- `make lint`

When a stack is selected (Maven/Gradle/etc.), this README should be updated with exact commands.

## Non-Negotiable Constraints

Scripting must never:
- Mutate beliefs/emotions/cognition
- Let Chronicler commit directly
- Retcon canon without explicit retcon mode

DynamisAI must never:
- Write canon directly
- Bypass IntentStream
- Use WallTime for simulation truth
- Apply stale async results beyond relevance

## Status

This repo currently documents and enforces architecture boundaries first; implementation modules are expected to follow these rules as code is added.
