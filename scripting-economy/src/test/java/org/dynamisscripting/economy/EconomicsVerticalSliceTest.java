package org.dynamisscripting.economy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.dynamis.core.entity.EntityId;
import org.dynamisscripting.api.value.CanonEvent;
import org.dynamisscripting.api.value.CanonTime;
import org.dynamisscripting.api.value.Intent;
import org.dynamisscripting.api.value.Percept;
import org.dynamisscripting.api.value.WorldEvent;
import org.dynamisscripting.canon.CanonTimekeeper;
import org.dynamisscripting.canon.DefaultCanonLog;
import org.dynamisscripting.chronicler.ArchetypeInstantiator;
import org.dynamisscripting.chronicler.ChroniclerScheduler;
import org.dynamisscripting.chronicler.DefaultChronicler;
import org.dynamisscripting.chronicler.QuestGraph;
import org.dynamisscripting.chronicler.TriggerEvaluator;
import org.dynamisscripting.chronicler.WorldEventEmitter;
import org.dynamisscripting.dsl.DslCompiler;
import org.dynamisscripting.dsl.PredicateDsl;
import org.dynamisscripting.oracle.BudgetLedger;
import org.dynamisscripting.oracle.CommitPhase;
import org.dynamisscripting.oracle.DefaultWorldOracle;
import org.dynamisscripting.oracle.RuleRegistry;
import org.dynamisscripting.oracle.ShapePhase;
import org.dynamisscripting.oracle.ValidatePhase;
import org.dynamisscripting.percept.DefaultPerceptBus;
import org.dynamisscripting.percept.FidelityModel;
import org.dynamisscripting.percept.PerceptDownsampler;
import org.dynamisscripting.spi.ChroniclerNodeArchetype;
import org.junit.jupiter.api.Test;

class EconomicsVerticalSliceTest {
    @Test
    void verticalSliceProofOfLife() {
        DefaultCanonLog canonLog = new DefaultCanonLog();
        CanonTimekeeper timekeeper = new CanonTimekeeper();
        AtomicLong commitIdCounter = new AtomicLong(2L);
        CommitPhase commitPhase = new CommitPhase(canonLog, timekeeper, commitIdCounter);
        BudgetLedger budgetLedger = new BudgetLedger();
        RuleRegistry ruleRegistry = new RuleRegistry();
        EconomicsBudgetRule budgetRule = new EconomicsBudgetRule(canonLog);
        ruleRegistry.register(budgetRule);
        ValidatePhase validatePhase = new ValidatePhase(ruleRegistry, budgetLedger);
        ShapePhase shapePhase = new ShapePhase(ruleRegistry);
        DefaultWorldOracle oracle = new DefaultWorldOracle(validatePhase, shapePhase, commitPhase, List.of(), canonLog);
        DefaultPerceptBus perceptBus = new DefaultPerceptBus(new FidelityModel(), new PerceptDownsampler(), canonLog, 100);

        EntityId factionA = EntityId.of(100L);
        EntityId factionB = EntityId.of(200L);

        CanonEvent setupFunds = CanonEvent.of(
                1L,
                CanonTime.of(1L, 1L),
                "setup:factionA:funds",
                FactionFunds.of(factionA, 10_000.0D, 0.0D));
        canonLog.append(setupFunds);
        assertTrue(canonLog.findByCommitId(1L).isPresent());

        Intent contractActivate = Intent.of(
                factionA,
                "CONTRACT_ACTIVATE",
                List.of(factionB),
                "value=500",
                0.9D,
                timekeeper.current(),
                Intent.RequestedScope.PUBLIC);
        var commitResult = oracle.commit(contractActivate);
        assertTrue(commitResult.committed());

        List<CanonEvent> intentEvents = canonLog.query(CanonTime.ZERO, canonLog.latestCanonTime())
                .stream()
                .filter(event -> event.causalLink().startsWith("intent:"))
                .toList();
        assertFalse(intentEvents.isEmpty());
        assertTrue(commitResult.commitId() > 0L);

        EconomicsArchetype baseArchetype = new EconomicsArchetype();
        AtomicInteger canInstantiateCalls = new AtomicInteger();
        ChroniclerNodeArchetype countingArchetype = new ChroniclerNodeArchetype() {
            @Override
            public String archetypeId() {
                return baseArchetype.archetypeId();
            }

            @Override
            public String archetypeName() {
                return baseArchetype.archetypeName();
            }

            @Override
            public boolean canInstantiate(org.dynamisscripting.api.CanonLog log, CanonTime now) {
                canInstantiateCalls.incrementAndGet();
                return baseArchetype.canInstantiate(log, now);
            }

            @Override
            public WorldEvent instantiate(org.dynamisscripting.api.CanonLog log, CanonTime now) {
                return baseArchetype.instantiate(log, now);
            }

            @Override
            public int maxConcurrentInstances() {
                return baseArchetype.maxConcurrentInstances();
            }
        };

        ArchetypeInstantiator archetypeInstantiator = new ArchetypeInstantiator(List.of(countingArchetype));
        TriggerEvaluator evaluator = new TriggerEvaluator(new PredicateDsl(new DslCompiler()), canonLog);
        QuestGraph graph = new QuestGraph();
        ChroniclerScheduler scheduler = new ChroniclerScheduler(graph, evaluator, 10);
        WorldEventEmitter emitter = new WorldEventEmitter();
        DefaultChronicler chronicler = new DefaultChronicler(graph, evaluator, scheduler, archetypeInstantiator, emitter);
        List<WorldEvent> proposedEvents = new ArrayList<>();
        chronicler.registerWorldEventListener(proposedEvents::add);

        chronicler.tick(timekeeper.current());
        assertTrue(canInstantiateCalls.get() > 0);

        List<Percept> receivedPercepts = new ArrayList<>();
        perceptBus.subscribe(factionB, receivedPercepts::add);
        perceptBus.deliver(Percept.of(
                factionB,
                "CONTRACT_ACTIVATED",
                "contract details",
                1.0D,
                canonLog.latestCommitId()));
        assertEquals(1, receivedPercepts.size());
        assertEquals("CONTRACT_ACTIVATED", receivedPercepts.getFirst().perceptType());

        List<CanonEvent> allEvents = canonLog.query(CanonTime.ZERO, canonLog.latestCanonTime());
        assertTrue(allEvents.size() >= 2);
        assertTrue(allEvents.stream().allMatch(event -> event.causalLink() != null && !event.causalLink().isBlank()));
        assertTrue(intentEvents.stream().anyMatch(event -> event.causalLink().startsWith("intent:")));
    }
}
