# ADR-001: Scripting Engine / AI Engine Boundary

**Status:** Accepted and Frozen  
**Date:** 2026-02-27  
**Project:** Dynamis Scripting Engine + DynamisAI — Dynamis open-source game engine stack

> **Frozen.** Further discussion is content, not architecture. New canonical dimensions, faction profiles, quest archetypes, and GOAP weight sets are additive data — they do not require revisiting this document. Reopen only if a new structural concept arises that nothing in this model can express.


---

## Context

DynamisAI is a AAA-grade cognitive agent simulation engine built in Java JDK 25. It operates alongside a scripting layer that governs world rules, time, and narrative structure. As both systems grow in power, the boundary between them must be non-negotiable and explicitly documented to prevent architectural drift — especially under pressure at 3am, or when a future contributor reads the code cold.

The full Dynamis engine stack provides the canonical world substrate:

| Module | Role in Canon |
|---|---|
| DynamisSky | Environmental truth — sun position, weather, time-of-day |
| DynamisTerran | Surface truth — geometry, occlusion, affordances, persistence |
| DynamisPhysics | Dynamic truth — forces, constraints, lawful physical outcomes |
| DynamisAudio | Acoustic truth — sound propagation, occlusion, fidelity |
| DynamisVFX | Consequence presentation — manifests canon visually, never authors it |
| DynamicLightEngine | Visual rendering — never a source of world truth |

DynamisAI sits above all of these as the only system that *experiences* this world subjectively.

---

## The Core Invariant

> **Only the Scripting Engine may mutate canonical world state.**  
> **Only DynamisAI may mutate internal agent cognition.**

No exceptions. Ever.

The sacred causality chain:

```
AI → Canon → Graph
Never AI → Graph directly.
```

---

## The D&D Mental Model

The scripting engine is the **Dungeon Master**. DynamisAI agents are the **Players**.

The DM owns the world. Players inhabit it.

**The DM does not tell players what they feel.**  
**Players do not tell the DM what the world is.**

Critically: in this architecture, **all NPCs are players too**. The scripting engine must never puppet NPC minds. It publishes obligations, constraints, and events. Agents react.

Litmus test for any disputed responsibility:

> *"Would the DM know this, or only the player?"*

- DM knows it → Scripting (Oracle / Chronicler)
- Player knows it → DynamisAI

---

## The Two Scripting Subsystems

The scripting engine is not a monolith. It consists of two distinct components with different natures.

### Oracle (Reactive — Law)

The rules adjudicator. Stateless in spirit.

**Oracle owns:**
- Canonical world state
- Rule enforcement and arbitration
- Action validation, shaping, and commit
- Resource budgets (these are world physics, not AI tuning)
- CanonTime
- Canonical event emission into the CanonLog

**Oracle arbitration pipeline:**
1. **Validate** — Is this action legal under current rules and budgets?
2. **Shape** — Can this be transformed into the nearest legal equivalent?
3. **Commit** — Write the canonical world delta.

Oracle never:
- Sets an agent's belief directly
- Forces an emotion
- Overrides cognition
- Acts as a novelist

Oracle may gate, rewrite, budget, delay, and sequence. It may never author minds.

---

### Chronicler (Proactive — Time)

The Doom Clock. The world engine. Advances independently of agent action.

**Chronicler owns:**
- Long-running world momentum
- Main story spine (the "Front")
- Side quest subgraphs
- Event cascade evaluation
- Deadline triggering
- Off-screen consequence resolution
- Background faction simulation
- Archetype/template instantiation for emergent nodes

**Chronicler never commits directly.** It proposes WorldEvents to Oracle.

The Chronicler evaluates a **narrative graph** every CanonTime tick:

- **Main spine** — the doom clock arc; world consequences compound if unchecked
- **Side quest subgraphs** — self-contained short stories with their own triggers and terminals
- **Handoff nodes** — optional connections from side quests back to the main spine

**Graph trigger predicates are pure functions of canon state.** No belief-state triggers. If you need belief-dependent narrative, do it indirectly: an agent's public accusation becomes a canonical fact; rumor intensity above a threshold is a canonical metric. These are objective measurements.

**Archetype instantiation (Option A.5):** Chronicler may instantiate new story nodes from authored templates when canonical predicates warrant. This yields emergent quests without losing determinism. Authors define archetypes; the engine instantiates them when conditions are met.

---

## DynamisAI (Cognition — Players)

**DynamisAI owns:**
- Percept weighting and filtering
- The four-layer cognition model (see below)
- Belief graph and memory
- Emotional state and drives
- Planning and intent formation
- Dialogue generation (jLLama + emotional TTS)
- Risk tolerance
- Mistakes
- Rumor propagation

**DynamisAI never:**
- Writes canonical world state
- Mutates the quest graph directly
- Invents percepts (it may invent beliefs)

---

## The Four-Layer Cognition Model

Replace "hallucination" with this precise model:

```
Percept → Interpretation → Inference → Rumor
```

| Layer | Definition | Can be wrong? |
|---|---|---|
| **Percept** | Grounded stimulus delivered from canon | No — it is a canon-derived view |
| **Interpretation** | Agent's classification of the stimulus | Yes |
| **Inference** | Agent's conclusion from interpretation | Yes |
| **Rumor** | Agent-to-agent transmitted belief | Often |

**Rule:** *Agents may invent beliefs. They may never invent percepts.*

The debug overlay must expose full provenance chains:  
`Percept → Interpretation → Inference → Rumor source → confidence decay`

---

## CanonLog

The single source of truth. An ordered, append-only stream of canonical commits.

**Every commit contains:**
- `commitId` (monotonic)
- `canonTime`
- `causalLink` (the IntentId or WorldEventId that caused this commit)
- typed world delta

**CanonLog enables:**
- Replay (re-emit the stream)
- Branching / what-if (fork the stream at any commitId)
- Debugging (follow causalLink back to cause)
- Chronicler evaluation (graph predicates watch CanonLog deltas)
- Percept derivation (PerceptBus watches CanonLog)

---

## The Event-Driven Topology

All cross-system interaction is event-driven. No direct method calls across boundaries.

```
IntentStream       →  Oracle  →  CanonLog
WorldEventStream   →  Oracle  →  CanonLog
CanonLog           →  PerceptBus  →  Agents (filtered)
CanonLog           →  Chronicler  (graph predicate evaluation)
CanonLog           →  Telemetry / Debug UI
```

### Intent Stream (AI → Oracle)

Structured proposals from agents. Required fields:
- `agentId`
- `intentType`
- `targets`
- `rationale`
- `confidence`
- `canonTimeSnapshot` (the CanonTime the agent used when forming this intent)
- `requestedScope` (public / private / stealth)

### WorldEvent Stream (Chronicler → Oracle)

Proposals from the Chronicler. Required fields:
- `nodeId`
- `archetype` (for template instantiation)
- `parameters`
- `priority`
- `canonTime`

### PerceptBus (CanonLog → Agents)

Derives agent-specific views of canonical events via deterministic filtering. Filtering is based solely on canon:
- Proximity and distance
- Geometric occlusion (DynamisTerran)
- Acoustic propagation (DynamisAudio)
- Line-of-sight
- Role / channel (guard captain sees official reports)
- Sensory range

**PerceptBus may:**
- Deliver at full fidelity
- Withhold
- Downsample
- Delay (deterministically, computed from canon — distance, medium, time-of-day)
- Redact
- Degrade precision

**PerceptBus may never introduce new facts.** It is a view layer, not a truth layer.

The percept filtering layer is physically implemented by the combination of DynamisAudio (acoustic fidelity), DynamisTerran (geometric occlusion), and DynamisPhysics (ray queries). These modules collectively *are* the PerceptBus substrate.

---

## Time Contract

| Clock | Owner | Purpose |
|---|---|---|
| **CanonTime** | Oracle | Simulation truth. All commits, triggers, budgets, and ordering. |
| **AgentTime** | DynamisAI | Internal agent update cadence. May be lower-rate and async. |
| **WallTime** | OS | Never used for simulation truth. |

**Async LLM results are late packets.** Any intent proposal carries the `canonTimeSnapshot` it was based on. If CanonTime has advanced beyond relevance when the result arrives, the result is discarded or re-grounded.

---

## Budgets as World Physics

Budgets (accusations per day, rumor broadcasts per hour, LLM calls per minute, conflict events per window) are **Oracle-owned canon constraints**, not AI tuning parameters.

When a budget is exhausted and Oracle rejects or shapes a proposal:

1. Oracle commits a canonical constraint outcome ("accusation dismissed by crowd")
2. PerceptBus delivers that outcome in a character-relevant form to the agent

**Invisible walls feel artificial. Visible constraints feel systemic.** Agents must experience budget exhaustion as a percept, not a silent failure. This turns limits into believable friction.

---

## World Substrate Percept Contribution Contracts

Each canonical world substrate module declares what percept-filtering facts it produces:

| Module | Percept Contribution |
|---|---|
| DynamisSky | Visibility, weather severity, time-of-day ambient noise |
| DynamisTerran | Geometric occlusion, line-of-sight, surface affordances, traversability |
| DynamisPhysics | Contact events, ray query results, dynamic occlusion from moving objects |
| DynamisAudio | Acoustic occlusion, sound propagation fidelity, hearing-range gates |
| DynamisVFX | Visual salience signals (fire visible, explosion flash) — advisory only |

PerceptBus queries these contracts to produce deterministic, module-agnostic percept views without knowing module internals.

---

## Degradation and Stress Policy

**CanonTime never waits. The world advances deterministically even when agents lag.**

### Agent Cognitive Tier Model

When AgentTime falls behind CanonTime, agents degrade gracefully:

| Tier | Condition | Behavior |
|---|---|---|
| **Tier 0** | Fresh percepts, beliefs current, LLM available | Full cognition |
| **Tier 1** | Moderate staleness | Stale belief snapshot; LLM results advisory if still relevant, otherwise discarded; non-LLM planner active |
| **Tier 2** | High staleness or LLM debt | Constitutional behavior: follow obligations, seek safety, continue last plan, reduce high-impact intents, canned dialogue |
| **Tier 3** | Must-not-act-wrong state | Diegetic cover action: idle, sleep, wait, patrol. Never blocks CanonTime. |

**Agents never stop existing. They degrade gracefully.**

At Tier ≥ 2, agents are action-conservative:
- **Allowed:** movement, routine tasks, self-preservation, schedule compliance, low-impact social actions
- **Disallowed:** high-impact irreversible intents (murder, arrest, burn evidence, major accusations) unless triggered by immediate grounded percepts

### Event Storm Policy

When rumor spikes or cascade triggers overwhelm the system:
- Oracle enforces global and per-agent event budgets per time window
- Chronicler throttles: maximum node activations per tick
- PerceptBus downsamples non-critical percepts (delivers summaries); this is safe because percepts are views, not truth

### Chronicler / Agent Collision Policy

Oracle is the single serializing authority. Commits within a tick are deterministically ordered. Shaping resolves contention without violating canon.

### Hot Reload Policy

Hot reload is a **WorldPatch** — a canonical event, not a state mutation.

- WorldPatch affects future trigger evaluation and future arbitration rules
- WorldPatch may invalidate pending intents (communicated to agents as a percept)
- Agents adapt over time; canon is never retconned unless explicitly in retcon mode
- Long-term agent memory is never touched by hot reload unless a designer explicitly enables retcon mode

---

## The Four Litmus Tests

**DM Test**  
*"Would the DM know this, or only the player?"*  
DM knows → scripting. Player knows → DynamisAI.

**Replay Test**  
*"If I replay the CanonLog, should this reappear?"*  
Yes → it must be canon. No → it belongs to cognition or telemetry.

**Debug Test**  
*"Can I explain why this happened by following causal links in the CanonLog?"*  
If not, a boundary has been violated.

**Degradation Test**  
*"If a subsystem slows or fails, does CanonTime still progress and does behavior remain explainable?"*  
If not, cognition is too tightly coupled to canon.

---

## Forbidden Actions (The Hard Negative Constraints)

### Scripting Engine Must Never:
- Set an agent's belief directly
- Force an agent's emotional state
- Override agent cognition
- Write NPC intent
- Trigger story nodes from agent belief state directly
- Retcon canon without explicit retcon mode
- Let Chronicler commit world state without Oracle arbitration

### DynamisAI Must Never:
- Write canonical world state directly
- Mutate the quest graph directly
- Invent percepts (only beliefs)
- Bypass the Intent Stream
- Use WallTime for simulation logic
- Allow async LLM results to apply after CanonTime has moved past relevance

---

## Declarative DSL Capability Targets

The scripting engine is declarative-first. Java owns evaluation. Data owns definition.

**Purely declarative (data files):** budgets, action legality matrices, beat windows and deadlines, quest graph structure, side-quest trigger predicates, shaping policy mappings, sensor parameters (hearing radius, LoS rules by weather/time-of-day). These ship as hotfixable data and validate on load.

**Predicate DSL (expression engine, not general scripting):** trigger logic compiled to a pure AST, evaluated deterministically over canon state. Required capabilities:

- Boolean logic and comparisons
- Time windows (`canonTime in [t1, t2]`)
- Bounded aggregates over CanonLog history (`count(events where …) >= k` within last N hours)
- Canonical metric queries (`publicSuspicion > 0.6`)
- Region and role predicates
- Negation and gating (`unless magistrateArrived`)

No loops. No mutation. No IO. No WallTime access. Pure functions of canon.

**Rewrite DSL (Oracle Shape rules):** declarative pattern-match on intent type and context, candidate substitution generation, priority-weighted deterministic scoring, explicit reason codes for observability. Still no loops, still no mutation.

**Procedural escape hatch:** rare, explicitly fenced, never used for canon ownership. Only for complex placement logic or bespoke quest effects that cannot be expressed as templates.

**WorldPatch delivery:** rule bundles, graph definitions, and budget tables are versioned data patches. Oracle and Chronicler load, validate, and swap atomically. CanonLog records `WorldPatchApplied(version)` so replay stays honest.

---

## Canonical System Dimensions

The scripting engine manages a set of canonical world systems. Each system is a dimension of world state that Oracle arbitrates, Chronicler schedules consequences for, and DynamisAI agents experience through cognition.

Current defined dimensions (additive — new dimensions require no engine redesign):

| Dimension | What it models | Primary canonical objects |
|---|---|---|
| **Economics** | Contracts, funds, costs, markets | BountyRecord, FactionFunds, ContractCost, AvailabilityPool |
| **Reputation** | Multi-axis social standing | ReputationScore per agent per faction |
| **Authority** | Legitimate power and jurisdiction | AuthorityLevel, JurisdictionMap, EnforcementCapacity |
| **Information Markets** | Who knows what and what it costs | IntelRecord, RumorPrice, NetworkCoverage |
| **Loyalty and Obligation** | Oaths, debts, feudal ties | ObligationRecord, LoyaltyWeight, DebtLedger |
| **Supply Chains** | Resource flows between locations | SupplyRoute, ResourceLevel, FlowRate |
| **Law and Precedent** | History of Oracle rulings | PrecedentLog, JurisdictionRules |
| **Time as Resource** | Agent capacity and attention | AgentCapacityPool, CommitmentLedger |
| **Cultural Orientation** | Honor-shame, guilt-innocence, fear-power weighting | CulturalVector per faction |

Adding a new dimension means: a new canonical state object, a new declarative rule bundle, a new set of GOAP weight entries, and a new family of Chronicler archetypes. No engine redesign.

---

## Society Vector Model

Societies — factions, cultures, species, organizations — are represented as **vectors across canonical system dimensions**. Each dimension has a defined scale with explicit behavioral meaning at key points. No culture is purely one type; all are weighted mixtures.

```yaml
faction: klingon_empire
systems:
  honor_shame:
    weight: 0.95
    public_shame_multiplier: 3.0
    honor_restoration_actions: [challenge, victory, honorable_death]
  economics:
    weight: 0.3
    contract_sanctity: low
  fear_power:
    weight: 0.4
    spiritual_authority: stovokor
    death_valence: positive
  goap_weights:
    survival: 0.4
    honor: 0.95
    wealth: 0.2
```

**New dimensions are additive and declarative.** Adding a cultural or economic dimension to the engine requires no Java changes — only a new canonical dimension definition and updated faction profiles.

**Interaction outcomes are computed as deterministic functions of vector relationships within Oracle.** Three interaction modes emerge from vector comparison:

- **Alignment** — vectors point the same direction; cooperation is natural
- **Opposition** — vectors point opposite directions; structural friction
- **Orthogonality** — one society weights a dimension highly, the other near zero; produces incomprehension rather than conflict, which is often more dramatically interesting than direct opposition

**Agents experience interaction outcomes via cognition.** Cultural vector distance shapes GOAP goal weights, Interpretation layer filtering, and Rumor propagation patterns. **Narrative consequences arise via Chronicler** — high cultural distance between co-located factions biases Chronicler toward conflict arc archetypes. Slow canonical drift of vectors over time (through sustained interaction, occupation, trade) is a valid Chronicler-managed process.

**The litmus test for new content:** if a new faction, culture, or species can be expressed as a profile in the existing dimension space — it is content, not architecture. If it requires a new structural concept that nothing in the engine can model — that is the signal to revisit the architecture.

---

## Consequences

**This architecture enables:**
- Full replay and branch-point simulation from CanonLog
- Swappable AI backends (scripted bots, human players, RL agents, remote LLM) without touching world logic
- Swappable world rules without touching AI cognition
- Deterministic debugging via causal link chains
- Graceful degradation under LLM load or event storms
- Hot reload without sim restart
- Emergent narrative from the lawful interaction of three forces: agent intent, world law, and world time

**This architecture requires:**
- Strict discipline on the mutation law — no convenience exceptions
- Tooling investment: CanonLog timeline explorer, intent inspector, belief graph viewer, budget dashboard, graph visualizer
- Aggressive budget enforcement to prevent entropy
- A conscious choice on narrative vs. sandbox philosophy for each shipped experience

---

## The Three-Force Model

This architecture is not "game scripting plus AI behavior."

It is a **three-force simulation**:

> **Law** (Oracle) + **Time** (Chronicler) + **Mind** (DynamisAI)

Emergence happens in the interaction of all three. Narrative coherence comes from the Chronicler, not from rewriting intent artificially. The scripting engine is a deterministic rule adjudicator with optional narrative shaping constraints — never a novelist.

---

*ADR-001 | DynamisAI Architecture | Dynamis Engine Stack*
