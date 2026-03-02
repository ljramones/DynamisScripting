package org.dynamisscripting.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.dynamis.core.entity.EntityId;
import org.dynamisscripting.api.value.WorldPatch;
import org.dynamisscripting.economy.EconomicsBudgetRule;
import org.dynamisscripting.economy.EconomicsDimension;
import org.dynamisscripting.spi.CanonDimensionProvider;
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
