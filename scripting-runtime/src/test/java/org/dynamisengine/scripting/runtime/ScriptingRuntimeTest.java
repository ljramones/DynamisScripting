package org.dynamisengine.scripting.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.dynamisengine.core.entity.EntityId;
import org.dynamisengine.scripting.api.CanonLog;
import org.dynamisengine.scripting.api.value.CanonTime;
import org.dynamisengine.scripting.api.value.WorldPatch;
import org.dynamisengine.scripting.economy.EconomicsBudgetRule;
import org.dynamisengine.scripting.economy.EconomicsDimension;
import org.dynamisengine.scripting.api.value.WorldEvent;
import org.dynamisengine.scripting.spi.CanonDimensionProvider;
import org.dynamisengine.scripting.spi.ChroniclerNodeArchetype;
import org.junit.jupiter.api.Test;

class ScriptingRuntimeTest {

    @Test
    void buildRequiresAtLeastOneDimension() {
        assertThrows(RuntimeException.class, () -> RuntimeBuilder.create().build());
    }

    @Test
    void fullAssemblyBuildsSuccessfully() {
        ScriptingRuntime runtime = RuntimeBuilder.create()
                .withDimension(new EconomicsDimension())
                .withArbitrationRule(new EconomicsBudgetRule(null))
                .build();

        assertEquals(0L, runtime.currentTime().tick());
    }

    @Test
    void tickAdvancesCanonTime() {
        ScriptingRuntime runtime = RuntimeBuilder.create()
                .withDimension(new EconomicsDimension())
                .build();

        runtime.tick();
        runtime.tick();

        assertEquals(2L, runtime.currentTime().tick());
    }

    @Test
    void applyPatchAppendsWorldPatchEventAndNotifiesDimension() {
        RecordingDimension recordingDimension = new RecordingDimension();
        ScriptingRuntime runtime = RuntimeBuilder.create()
                .withDimension(recordingDimension)
                .build();

        WorldPatch patch = WorldPatch.of("1.2.3", List.of("r1"), List.of("s1"), List.of("a1"));
        runtime.applyPatch(patch);

        assertTrue(recordingDimension.called);
        assertFalse(runtime.canonLog().queryByCausalLink("worldpatch:1.2.3").isEmpty());
    }

    @Test
    void registerAndRecordAgentUpdateMovesTier3ToTier0() {
        ScriptingRuntime runtime = RuntimeBuilder.create()
                .withDimension(new EconomicsDimension())
                .build();
        EntityId agent = EntityId.of(500L);

        runtime.registerAgent(agent);
        assertEquals(AgentCognitiveTier.TIER_3, runtime.degradationMonitor().getTier(agent, runtime.currentTime().tick()));

        runtime.recordAgentUpdate(agent);
        assertEquals(AgentCognitiveTier.TIER_0, runtime.degradationMonitor().getTier(agent, runtime.currentTime().tick()));
    }

    @Test
    void runningFlagTransitions() {
        ScriptingRuntime runtime = RuntimeBuilder.create()
                .withDimension(new EconomicsDimension())
                .build();

        assertFalse(runtime.isRunning());
        runtime.start();
        assertTrue(runtime.isRunning());
        runtime.stop();
        assertFalse(runtime.isRunning());
    }

    @Test
    void eventBusPublishesCanonLogEventWhenCommitOccurs() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger observed = new AtomicInteger(0);

        ScriptingRuntime runtime = RuntimeBuilder.create()
                .withDimension(new EconomicsDimension())
                .withArchetype(alwaysInstantiateArchetype())
                .build();

        runtime.eventBus().subscribe(CanonLogEvent.class, event -> {
            observed.incrementAndGet();
            latch.countDown();
        });

        runtime.tick();

        assertTrue(latch.await(3, TimeUnit.SECONDS));
        assertTrue(observed.get() >= 1);
    }

    private static ChroniclerNodeArchetype alwaysInstantiateArchetype() {
        return new ChroniclerNodeArchetype() {
            @Override
            public String archetypeId() {
                return "runtime.test.archetype";
            }

            @Override
            public String archetypeName() {
                return "Runtime Test Archetype";
            }

            @Override
            public boolean canInstantiate(CanonLog canonLog, CanonTime currentTime) {
                return true;
            }

            @Override
            public WorldEvent instantiate(CanonLog canonLog, CanonTime currentTime) {
                return WorldEvent.of(
                        "runtime.test.node",
                        archetypeId(),
                        Map.of("tick", currentTime.tick()),
                        5,
                        currentTime);
            }
        };
    }

    private static final class RecordingDimension implements CanonDimensionProvider {
        private boolean called;

        @Override
        public String dimensionId() {
            return "recording";
        }

        @Override
        public String dimensionName() {
            return "Recording Dimension";
        }

        @Override
        public List<String> canonicalObjectTypes() {
            return List.of("Record");
        }

        @Override
        public void onWorldPatchApplied(WorldPatch patch) {
            called = true;
        }
    }
}
