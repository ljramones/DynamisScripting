package org.dynamisscripting.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.dynamis.core.entity.EntityId;
import org.dynamisscripting.api.value.CanonEvent;
import org.dynamisscripting.api.value.CanonTime;
import org.dynamisscripting.api.value.WorldPatch;
import org.dynamisscripting.economy.EconomicsArchetype;
import org.dynamisscripting.economy.EconomicsBudgetRule;
import org.dynamisscripting.economy.EconomicsDimension;
import org.dynamisscripting.economy.FactionFunds;
import org.junit.jupiter.api.Test;

class RuntimeIntegrationTest {

    @Test
    void multiTickSimulationWithMidPatchStaysHealthy() {
        EntityId factionA = EntityId.of(1001L);
        EntityId agent = EntityId.of(9001L);

        ScriptingRuntime runtime = RuntimeBuilder.create()
                .withDimension(new EconomicsDimension())
                .withArbitrationRule(new EconomicsBudgetRule(null))
                .withArchetype(new EconomicsArchetype())
                .build();
        runtime.registerAgent(agent);

        runtime.canonLog().append(CanonEvent.of(
                1L,
                CanonTime.ZERO,
                "setup:factionA:funds",
                FactionFunds.of(factionA, 10_000.0D, 0.0D)));

        runtime.tick();
        runtime.tick();
        runtime.applyPatch(WorldPatch.of("2.0.0", List.of("economy.rule"), List.of(), List.of()));
        runtime.tick();
        runtime.tick();
        runtime.tick();

        assertEquals(5L, runtime.currentTime().tick());
        List<CanonEvent> events = runtime.canonLog().query(CanonTime.ZERO, runtime.currentTime());
        assertTrue(events.size() >= 1);
        for (CanonEvent event : events) {
            assertNotNull(event.causalLink());
            assertFalse(event.causalLink().isBlank());
        }

        assertTrue(runtime.degradationMonitor().allTiers(runtime.currentTime().tick()).containsKey(agent));
        assertFalse(runtime.canonLog().queryByCausalLink("worldpatch:2.0.0").isEmpty());
    }
}
