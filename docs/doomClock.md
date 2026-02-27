Systemic Economics, GOAP Adversaries, Declarative DSLs, and Society Vectors in a Canonical Simulation Architecture
Why an economic layer turns “doom clocks” into simulation physics
Your ADR already frames the world as a three‑force system—Law (Oracle) + Time (Chronicler) + Mind (DynamisAI)—with strict authority over canon (“only the Scripting Engine may mutate canonical world state”) and the sacred causality chain AI → Canon → Graph. Within that model, a “doom clock” is not merely pacing flavor: Chronicler explicitly evaluates a main spine where “world consequences compound if unchecked.” 

In tabletop design, clocks are a well-defined technique for tracking the approach of trouble and the accumulation of pressure. The Blades in the Dark rules define a “progress clock” as segmented tracking for “ongoing effort… or the approach of impending trouble,” with examples like escalating alert levels until an alarm is raised. 
 That’s very close to your Chronicler conceptually—except your engine can make the pressure world-native (money, manpower, logistics, influence) rather than purely authorial.

Where the “economic layer makes everything honest” is that it translates pacing into scarcity and institutions rather than an invisible hand. Work on virtual economies explicitly treats “markets, institutions, and money” as design tools for shaping incentives under artificial scarcity. 
 This is the research‑grade version of the intuition you stated: when pursuers stop coming because the payer is broke (or the cost curve explodes), the pressure release is explained by canon, not by designer mercy.

There’s also a strong precedent in AAA for algorithmic pacing, but typically as a bespoke pacing subsystem. Valve’s Valve talk on the Left 4 Dead “AI Director” describes “dramatic game pacing” as algorithmically creating peaks and valleys of intensity, explicitly warning that constant combat is fatiguing and long inactivity is boring. 
 Your architectural leap is to make pacing and escalation emerge from canonical economics+bureaucracy, rather than being primarily a director-driven dial.

Finally, the **Robin Hunicke / Marc LeBlanc / Robert Zubek MDA framework emphasizes that “interaction between coded subsystems creates complex, dynamic (often unpredictable) behavior,” and warns that designers must consider subsystem interdependencies when changing mechanics. 
 This is a rigorous rationale for why an economy is not “just another system”: it’s a global coupling layer that makes constraints legible and self-consistent across narrative, AI, and world simulation.

Contracts and bounties as canonical market objects rather than bespoke “systems”
If you want bounties to “emerge,” the key is to model them as contract objects with lifecycle and settlement rules, not as encounter triggers. Multi-agent systems research already has a canonical template for this: the **Reid G. Smith Contract Net Protocol describes task allocation as negotiation between nodes with tasks and nodes that can execute them, emphasizing distributed control of cooperative task execution (“task-sharing”) through bids and awards. 

In your game terms, a bounty/contract can be represented as a market object that supports:

a call/offer (posted bounty),
proposals/bids (which assassins or guilds are willing/able),
award (assignment),
execution,
outcome and settlement (payment, penalties, reputation effects).
This directly supports your “Option B” conclusion: a “simple binary” contract is just the degenerate case of a richer market contract (single bidder, full prepay, no history). Choosing the richer object model early prevents later schema migration because you can leave fields unpopulated until you need them.

There is also a strong fit between “contract objects in canon” and your CanonLog/event sourcing approach. **Martin Fowler’s description of event sourcing is essentially: store “all changes… as a sequence of events,” enabling reconstruction of past states and building higher-level facilities on top of the event log. 
 That is precisely what your ADR defines for CanonLog: an append-only stream with replay, branching, causal links, and predicate evaluation over canonical deltas.

This matters because a contract/bounty is not only current state (“active/inactive”)—it’s also history (“failures in last 30 days,” “average time-to-completion,” “trust score drift”). Those histories become first-class inputs into costs, availability pools, and faction willingness to escalate.

Planning-driven adversaries and GOAP as “programmable behavior” that scales
The GOAP claim in your conversation is strongly supported by the most-cited classic example: Jeff Orkin’s case study on the AI of **F.E.A.R. (originally presented around *Game Developers Conference). Orkin describes using A not only for pathfinding but to “plan sequences of actions,” while keeping a very small FSM (“only three states”). 
 That is the exact pattern your architecture wants: a compact runtime evaluator + rich data-driven definitions that can be tuned without recompiling.

Orkin also explicitly notes that one of those FSM states (“UseSmartObject”) is “really just a specialized data-driven version” where the animation/interaction is specified through a SmartObject in the game database—i.e., behavior authored as data rather than hard-coded branching. 
 This is a strong empirical precedent for your “declarative tech choices” direction: in practice, AAA AI teams use planning + data-driven interaction primitives to avoid brittle scripted combinatorics.

In your boundary model, feeding a GOAP domain to the AI layer is architecturally clean as long as it only produces intent proposals and never commits canon. Your ADR’s invariant (“Only the Scripting Engine may mutate canonical world state”) and the chain (AI → Canon → Graph) already enforce that discipline. The result is exactly the assassin fantasy you described: assassins can re-plan, abort when risk spikes, buy information, and try again later—while their actual effects remain constrained by Oracle legality, budgets, and economics.

Information asymmetry and rumor markets as a deeper gameplay layer than combat
The “information asymmetry gameplay loop” you identified has a rigorous foundation in economics: **George Akerlof’s classic “Market for Lemons” formalizes how quality uncertainty and asymmetric information can degrade market outcomes (adverse selection). 
 In your scenario, the bounty market and the counter-market for defensive intelligence are naturally asymmetric: the employer knows more about resources and intent, while the target knows more about their own movement and defenses. That asymmetry creates a real “information economy” in which truth, deception, and surveillance are tradable.

Game AI research on imperfect information reinforces why this should live in your Percept/Interpretation/Inference/Rumor stack instead of being hand-waved. Work on RTS bots operating under fog-of-war highlights that many game AIs “cheat” by using perfect information, and shows approaches for agents to act competently without cheating under incomplete information. 
 That’s directly relevant to your rule that agents may invent beliefs but not percepts, and must act on canon-derived views rather than omniscient state.

Once information becomes “economic,” reputation becomes a currency (and a multiplier on cost). Trust/reputation systems literature describes reputation as aggregated community ratings that both (a) help decide whether to transact and (b) “provide an incentive for good behaviour,” improving market quality. 
 This maps tightly to your design idea of “contract failures in last 30 days change prices”: you are essentially building a reputation-driven market mechanism inside canon, where repeated failure increases risk premiums, reduces bidder participation, and makes escalation financially prohibitive—without any arbitrary cap.

This pair—imperfect information + reputation economics—creates the stealth/intel gameplay you described: the player doesn’t need to out-fight the assassin; they can out-buy, out-lie, out-warn, or bankrupt the employer. Importantly, in your architecture this is not “a stealth subsystem.” It’s a lawful consequence of: CanonLog → Percept filtering → belief/planning under uncertainty → contracts settled by economic rules.

Declarative DSL requirements implied by economics, contracts, and CanonLog
Your ADR already locks a key constraint that drives the entire DSL question: “Graph trigger predicates are pure functions of canon state,” and belief-state triggers are forbidden except indirectly through canonical measurements. That implies your trigger language must be a query language over canonical state and canonical history.

The moment you introduce contracts with pricing, insolvency, reputational multipliers, and escalation curves, you need bounded history queries like:

“count failures in the last 30 days,”
“sum funds deducted this week,”
“max active contracts in the last N hours,”
“rate of accusations per day over the last 7 days.”
This is not speculative: this is the exact class of operators that stream processing systems and continuous query languages were built to support. CQL defines a “sliding window” as a finite historical snapshot over an unbounded stream, explicitly supporting time-based and tuple-based windows. 
 Data stream management work highlights that unbounded streams are infeasible to store in full, so systems maintain “at best, a sliding window of recently arrived data,” with persistent queries that update over time. 
 The formal lesson for your DSL is: window semantics must be explicit (range/slide, event-time vs processing-time, overlap rules), because they determine determinism, performance, and meaning. A modern survey of stream window types notes that windowing concepts proliferate across academic and commercial systems, with many semantic inconsistencies—strong evidence that “bounded aggregates” are not just a feature checkbox but a specification burden. 

On the “declarative, hot-reloadable” side, there’s also direct precedent in game scripting research: Cornell’s work on declarative processing for games describes a scripting approach where files are “structured to look like an imperative programming language, even though they are processed declaratively,” explicitly aiming to make authoring accessible while retaining declarative evaluation benefits. 
 This aligns with your “Java owns evaluation, data owns definition” stance, while avoiding the overhead and sandboxing risks of embedding a fully general procedural language everywhere.

Finally, your CanonLog design is already event-sourcing in spirit (append-only canonical commits enabling replay and branching). Event sourcing’s core idea—state changes are captured as events and can reconstruct past states—gives a strong theoretical backing for why your DSL should be a safe, deterministic query layer over an event log, not “arbitrary code that runs whenever.” 

Architecturally, that implies a DSL with three capability tiers:

a pure expression/predicate tier (boolean conditions over canon),
a bounded-aggregate tier (windowed counts/sums over CanonLog),
a limited rewrite/mapping tier for Shape (pattern-match → candidate substitutes → deterministic selection), consistent with your “budgets as world physics” approach.
Society vectors and cultural-distance modeling as a computable faction layer
Your “society as a vector” insight has direct parallels in cross-cultural research. **Geert Hofstede’s cultural dimensions framework operationalizes culture as numeric scores along multiple axes (e.g., power distance, individualism, uncertainty avoidance), explicitly expressed on scales. 
 International business research then computes “cultural distance” by aggregating differences across those dimensions; a large meta-analytic review notes that **Bruce Kogut and Harbir Singh popularized a Euclidean distance index using Hofstede’s dimensions, and that this approach became dominant in subsequent studies. 

The same review is also important as a warning label: cultural-distance research has been criticized for simplification, statistical assumptions (e.g., uncorrelated dimensions), and dependence on potentially outdated measurement sets. 
 That critique matters for your engine because it suggests the correct stance: society vectors should be treated as useful generative parameters (for predictable frictions and affordances), not as “scientific truth about real peoples.”

Your honor-culture discussion also has strong empirical grounding. The Russell Sage Foundation summary of Cohen et al.’s work describes experiments where norms of a “culture of honor” manifest in cognition/emotion/physiology and explicitly tie insult to reputation threat. 
 A broader handbook chapter on cultures of honor traces anthropological foundations and subsequent social-psych research lines, reinforcing that “honor” can function as a core concern shaping responses, institutions, and conflict dynamics. 

For your faction modeling, an especially relevant refinement is the “honor/face/dignity” cultural logics framework: Leung & Cohen’s work (as previewed in open text) argues that cultures organize behavior according to different logics (honor, face, dignity), and the same individual trait pattern can predict opposite behavior across cultural contexts. 
 That is exactly what you want from a simulation perspective: culture as a multiplier on interpretation and goal weighting, not a cosmetic label.

Also crucial: the shame/guilt/fear typology you referenced is widely discussed but contested. A research critique explicitly argues that shame/guilt divisions are not cleanly demarcated and have limited empirical support as categorical distinctions. 
 In practice, that suggests a robust implementation strategy: model these as continuous axes/weights (vectors), allow mixtures, and validate via observed emergent behavior rather than assuming clean partitions.

Tying back to your architecture, the society-vector idea slots cleanly into the existing ADR constraints:

culture cannot directly write beliefs (it modulates Interpretation and planning weights), consistent with the “agents invent beliefs, never percepts” rule,
inter-faction interaction outcomes should be computed in Oracle from canonical society vectors (remaining deterministic and replayable via CanonLog), matching your replay/debug litmus tests,
Chronicler can select archetype graphs based on the friction profile between vectors, while still keeping trigger predicates purely canonical.
In other words: “Klingon vs Ferengi” becomes an authored set of weights and institutional rules, and the predicted friction is an emergent property of distance/misalignment across those dimensions—conceptually aligned with how cultural distance is operationalized in parts of the academic literature, but implemented as a game-facing, tunable, non-essentialist tool. 
