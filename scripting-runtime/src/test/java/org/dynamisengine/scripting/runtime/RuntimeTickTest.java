package org.dynamisengine.scripting.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import org.dynamisengine.core.entity.EntityId;
import org.dynamisengine.scripting.api.value.CanonTime;
import org.dynamisengine.scripting.api.value.WorldEvent;
import org.dynamisengine.scripting.canon.CanonTimekeeper;
import org.dynamisengine.scripting.canon.DefaultCanonLog;
import org.dynamisengine.scripting.chronicler.ArchetypeInstantiator;
import org.dynamisengine.scripting.chronicler.ChroniclerScheduler;
import org.dynamisengine.scripting.chronicler.DefaultChronicler;
import org.dynamisengine.scripting.chronicler.QuestGraph;
import org.dynamisengine.scripting.chronicler.TriggerEvaluator;
import org.dynamisengine.scripting.chronicler.WorldEventEmitter;
import org.dynamisengine.scripting.dsl.DslCompiler;
import org.dynamisengine.scripting.dsl.PredicateDsl;
import org.dynamisengine.scripting.oracle.BudgetLedger;
import org.dynamisengine.scripting.oracle.CommitPhase;
import org.dynamisengine.scripting.oracle.DefaultWorldOracle;
import org.dynamisengine.scripting.oracle.RuleRegistry;
import org.dynamisengine.scripting.oracle.ShapePhase;
import org.dynamisengine.scripting.oracle.ValidatePhase;
import org.dynamisengine.scripting.percept.DefaultPerceptBus;
import org.dynamisengine.scripting.percept.FidelityModel;
import org.dynamisengine.scripting.percept.PerceptDownsampler;
import org.dynamisengine.scripting.spi.ChroniclerNodeArchetype;
import org.junit.jupiter.api.Test;

class RuntimeTickTest {

    @Test
    void executeAdvancesCanonTimeAndMatchesTimekeeper() {
        RuntimeHarness harness = RuntimeHarness.create(false);

        RuntimeTickResult result = harness.runtimeTick.execute();

        assertEquals(1L, harness.timekeeper.current().tick());
        assertEquals(harness.timekeeper.current(), result.canonTime());
    }

    @Test
    void chroniclerProposalIsCommittedToCanonLog() {
        RuntimeHarness harness = RuntimeHarness.create(true);

        RuntimeTickResult result = harness.runtimeTick.execute();

        assertTrue(result.worldEventsProposed() >= 1);
        assertTrue(result.worldEventsCommitted() >= 1);
        assertFalse(harness.canonLog.queryByCausalLink("worldevent:runtime.tick.node").isEmpty());
    }

    private static final class RuntimeHarness {
        private final DefaultCanonLog canonLog;
        private final CanonTimekeeper timekeeper;
        private final RuntimeTick runtimeTick;

        private RuntimeHarness(DefaultCanonLog canonLog, CanonTimekeeper timekeeper, RuntimeTick runtimeTick) {
            this.canonLog = canonLog;
            this.timekeeper = timekeeper;
            this.runtimeTick = runtimeTick;
        }

        private static RuntimeHarness create(boolean emitWorldEvent) {
            DefaultCanonLog canonLog = new DefaultCanonLog();
            CanonTimekeeper timekeeper = new CanonTimekeeper();
            AtomicLong commitCounter = new AtomicLong(1L);

            RuleRegistry ruleRegistry = new RuleRegistry();
            ValidatePhase validatePhase = new ValidatePhase(ruleRegistry, new BudgetLedger());
            ShapePhase shapePhase = new ShapePhase(ruleRegistry);
            CommitPhase commitPhase = new CommitPhase(canonLog, timekeeper, commitCounter);
            DefaultWorldOracle oracle = new DefaultWorldOracle(
                    validatePhase,
                    shapePhase,
                    commitPhase,
                    List.of(),
                    canonLog);

            DslCompiler compiler = new DslCompiler();
            PredicateDsl predicateDsl = new PredicateDsl(compiler);
            TriggerEvaluator triggerEvaluator = new TriggerEvaluator(predicateDsl, canonLog);
            QuestGraph questGraph = new QuestGraph();
            List<ChroniclerNodeArchetype> archetypes = emitWorldEvent
                    ? List.of(alwaysInstantiateArchetype())
                    : List.of();
            ArchetypeInstantiator archetypeInstantiator = new ArchetypeInstantiator(archetypes);
            ChroniclerScheduler scheduler = new ChroniclerScheduler(questGraph, triggerEvaluator, 10);
            WorldEventEmitter emitter = new WorldEventEmitter();
            DefaultChronicler chronicler = new DefaultChronicler(
                    questGraph,
                    triggerEvaluator,
                    scheduler,
                    archetypeInstantiator,
                    emitter);

            DefaultPerceptBus perceptBus =
                    new DefaultPerceptBus(new FidelityModel(), new PerceptDownsampler(), canonLog, 100);
            DegradationMonitor degradationMonitor =
                    new DegradationMonitor(new RuntimeConfiguration.DegradationTierThresholds(5, 15, 30));

            RuntimeTick runtimeTick = new RuntimeTick(
                    timekeeper,
                    chronicler,
                    oracle,
                    perceptBus,
                    degradationMonitor,
                    RuntimeConfiguration.defaults());
            return new RuntimeHarness(canonLog, timekeeper, runtimeTick);
        }
    }

    private static ChroniclerNodeArchetype alwaysInstantiateArchetype() {
        return new ChroniclerNodeArchetype() {
            @Override
            public String archetypeId() {
                return "runtime.tick.archetype";
            }

            @Override
            public String archetypeName() {
                return "Runtime Tick Archetype";
            }

            @Override
            public boolean canInstantiate(org.dynamisengine.scripting.api.CanonLog canonLog, CanonTime currentTime) {
                return true;
            }

            @Override
            public WorldEvent instantiate(org.dynamisengine.scripting.api.CanonLog canonLog, CanonTime currentTime) {
                return WorldEvent.of(
                        "runtime.tick.node",
                        archetypeId(),
                        Map.of("tick", currentTime.tick()),
                        5,
                        currentTime);
            }
        };
    }
}
