# DynamisScripting

The scripting and world simulation engine for the Dynamis game engine ecosystem. DynamisScripting is the **Dungeon Master** — it owns canonical world state, enforces world law, advances narrative time, and derives agent percepts. DynamisAI agents are the players. They never touch world truth directly.

This is not game scripting in the traditional sense. It is a **three-force simulation**:

> **Law** (Oracle) + **Time** (Chronicler) + **Mind** (DynamisAI)

Emergence happens in the interaction of all three.

---

## The Core Invariant

> **Only the Scripting Engine may mutate canonical world state.**  
> **Only DynamisAI may mutate internal agent cognition.**

No exceptions. Ever.

```
AI → Intent → Oracle → CanonLog
Chronicler → WorldEvent → Oracle → CanonLog
CanonLog → PerceptBus → Agents (filtered views only)
```

---

## Architecture

DynamisScripting is an 11-module Maven multi-module project. Each module has a single, non-overlapping responsibility.

```
scripting-api           Pure contracts and value types. What everything depends on.
    └── scripting-spi   Extension points. How third parties extend without forking.
            ├── scripting-dsl       Predicate and Rewrite DSL engine.
            ├── scripting-canon     CanonLog — the single source of truth.
            │       ├── scripting-oracle      Validate → Shape → Commit pipeline.
            │       ├── scripting-chronicler  Doom Clock. Proactive world engine.
            │       ├── scripting-percept     Agent percept derivation and delivery.
            │       └── scripting-society     Society Vector Model.
            └── scripting-economy   Economics — the vertical slice proof of life.
                    └── scripting-runtime     Tick loop. Wires everything together.
                                └── scripting-ashford   Demo world. Data files only.
```

---

## The Two Scripting Subsystems

### Oracle — Law (Reactive)

The rules adjudicator. The only system permitted to write canonical world state.

**Three-phase pipeline:**
1. **Validate** — Is this action legal under current rules and budgets?
2. **Shape** — Can this be transformed into the nearest legal equivalent?
3. **Commit** — Write the canonical world delta to CanonLog with causal link.

Oracle never sets agent beliefs, forces emotions, overrides cognition, or writes NPC intent.

### Chronicler — Time (Proactive)

The Doom Clock. Advances independently of agent action.

**Owns:** long-running world momentum, main story spine, side quest subgraphs, event cascade evaluation, deadline triggering, off-screen consequence resolution, background faction simulation.

**Chronicler never commits directly.** It proposes `WorldEvent`s to Oracle. Graph trigger predicates are pure functions of canon state — no belief-state triggers.

---

## CanonLog

The single source of truth. An ordered, append-only stream of canonical commits.

Every commit contains:
- `commitId` — monotonic, never reused
- `canonTime` — simulation time (never wall time)
- `causalLink` — the `IntentId` or `WorldEventId` that caused this commit
- typed world delta

CanonLog enables full replay, branch-point simulation, deterministic debugging, and percept derivation. If you can't explain why something happened by following causal links in the CanonLog, a boundary has been violated.

---

## The Event-Driven Topology

All cross-system interaction is event-driven. No direct method calls across boundaries.

```
IntentStream      →  Oracle      →  CanonLog
WorldEventStream  →  Oracle      →  CanonLog
CanonLog          →  PerceptBus  →  Agents (filtered, fidelity-degraded views)
CanonLog          →  Chronicler  (graph predicate evaluation)
CanonLog          →  Telemetry / Debug UI
```

---

## Time Contract

| Clock | Owner | Purpose |
|---|---|---|
| **CanonTime** | Oracle | Simulation truth. All commits, triggers, budgets, ordering. |
| **AgentTime** | DynamisAI | Internal agent update cadence. May be lower-rate and async. |
| **WallTime** | OS | Never used for simulation truth. |

**CanonTime never waits.** The world advances deterministically even when agents lag.

---

## The Predicate DSL

Graph trigger predicates and Oracle shape rules are compiled expressions, not general scripts.

**Permitted:**
- Boolean logic and comparisons
- Time windows (`canonTime in [t1, t2]`)
- Bounded aggregates over CanonLog history (`count(events where …) >= k`)
- Canonical metric queries (`publicSuspicion > 0.6`)
- Region and role predicates
- Negation and gating (`unless magistrateArrived`)

**Forbidden (enforced at compile time):**
- Loops
- Mutation
- IO
- WallTime access
- Any non-deterministic construct

---

## Canonical System Dimensions

Oracle arbitrates, Chronicler schedules consequences for, and DynamisAI agents experience through cognition:

| Dimension | What it models |
|---|---|
| Economics | Contracts, funds, costs, markets |
| Reputation | Multi-axis social standing |
| Authority | Legitimate power and jurisdiction |
| Information Markets | Who knows what and what it costs |
| Loyalty and Obligation | Oaths, debts, feudal ties |
| Supply Chains | Resource flows between locations |
| Law and Precedent | History of Oracle rulings |
| Time as Resource | Agent capacity and attention |
| Cultural Orientation | Honor-shame, guilt-innocence, fear-power weighting |

Adding a new dimension requires no engine redesign — only a new canonical state object, a declarative rule bundle, GOAP weight entries, and Chronicler archetypes.

---

## Society Vector Model

Factions, cultures, and species are represented as vectors across canonical system dimensions. Interaction outcomes are deterministic functions of vector relationships computed within Oracle.

Three interaction modes:
- **Alignment** — vectors point the same direction; cooperation natural
- **Opposition** — vectors point opposite directions; structural friction
- **Orthogonality** — one society weights a dimension highly, the other near zero; produces incomprehension, often more dramatically interesting than conflict

---

## Agent Degradation Tiers

When AgentTime falls behind CanonTime, agents degrade gracefully:

| Tier | Condition | Behavior |
|---|---|---|
| 0 | Fresh percepts, LLM available | Full cognition |
| 1 | Moderate staleness | Stale snapshot; non-LLM planner active |
| 2 | High staleness or LLM debt | Constitutional behavior; action-conservative |
| 3 | Must-not-act-wrong state | Diegetic cover action (idle, patrol) |

**CanonTime never waits. Agents never stop existing. They degrade gracefully.**

---

## The Four Litmus Tests

**DM Test** — *"Would the DM know this, or only the player?"*
DM knows → scripting. Player knows → DynamisAI.

**Replay Test** — *"If I replay the CanonLog, should this reappear?"*
Yes → it must be canon. No → it belongs to cognition or telemetry.

**Debug Test** — *"Can I explain why this happened by following causal links in the CanonLog?"*
If not, a boundary has been violated.

**Degradation Test** — *"If a subsystem slows or fails, does CanonTime still progress?"*
If not, cognition is too tightly coupled to canon.

---

## Modules

| Module | Artifact ID | Purpose |
|---|---|---|
| `scripting-api` | `dynamis-scripting-api` | Pure contracts and value types |
| `scripting-spi` | `dynamis-scripting-spi` | Extension points for third parties |
| `scripting-dsl` | `dynamis-scripting-dsl` | Predicate and Rewrite DSL engine |
| `scripting-canon` | `dynamis-scripting-canon` | CanonLog implementation |
| `scripting-oracle` | `dynamis-scripting-oracle` | Validate → Shape → Commit pipeline |
| `scripting-chronicler` | `dynamis-scripting-chronicler` | Proactive world engine |
| `scripting-percept` | `dynamis-scripting-percept` | PerceptBus — agent percept derivation |
| `scripting-society` | `dynamis-scripting-society` | Society Vector Model |
| `scripting-economy` | `dynamis-scripting-economy` | Economics canonical dimension |
| `scripting-runtime` | `dynamis-scripting-runtime` | Tick loop and runtime assembly |
| `scripting-ashford` | `dynamis-scripting-ashford` | Demo world — data files only |

---

## Dependencies

| Dependency | Scope |
|---|---|
| `org.dynamis:dynamis-core` | compile |
| `org.dynamis:dynamis-event` | compile |
| `org.dynamis:dynamis-expression` | compile (`scripting-dsl` only) |

Install locally before building:
```bash
cd ../DynamisCore && mvn install
cd ../DynamisEvent && mvn install
cd ../DynamisExpression && mvn install
```

---

## Requirements

- Java 25+

---

## License

Apache 2.0 — see [LICENSE](LICENSE) for details.

---

## Reference

- [ADR-001: Scripting Engine / AI Engine Boundary](docs/ADR-001.md) — **Read this before touching anything.**
