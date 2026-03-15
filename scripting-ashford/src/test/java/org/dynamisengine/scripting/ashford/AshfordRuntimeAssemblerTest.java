package org.dynamisengine.scripting.ashford;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.dynamisengine.core.entity.EntityId;
import org.dynamisengine.scripting.runtime.ScriptingRuntime;
import org.junit.jupiter.api.Test;

class AshfordRuntimeAssemblerTest {

    @Test
    void assembleBuildsAndTicksRuntimeWithRegisteredAgents() {
        ScriptingRuntime runtime = AshfordRuntimeAssembler.assemble();
        assertNotNull(runtime);
        assertEquals(0L, runtime.currentTime().tick());

        runtime.tick();
        runtime.tick();
        runtime.tick();

        assertEquals(3L, runtime.currentTime().tick());
        assertTrue(runtime.degradationMonitor().allTiers(runtime.currentTime().tick()).containsKey(EntityId.of(1L)));
        assertTrue(runtime.degradationMonitor().allTiers(runtime.currentTime().tick()).containsKey(EntityId.of(2L)));
        assertTrue(runtime.degradationMonitor().allTiers(runtime.currentTime().tick()).containsKey(EntityId.of(3L)));
    }
}
