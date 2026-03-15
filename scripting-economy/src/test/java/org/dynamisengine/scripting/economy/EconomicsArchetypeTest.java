package org.dynamisengine.scripting.economy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.dynamisengine.core.entity.EntityId;
import org.dynamisengine.scripting.api.value.CanonEvent;
import org.dynamisengine.scripting.api.value.CanonTime;
import org.dynamisengine.scripting.api.value.Intent;
import org.dynamisengine.scripting.api.value.WorldEvent;
import org.dynamisengine.scripting.canon.DefaultCanonLog;
import org.junit.jupiter.api.Test;

class EconomicsArchetypeTest {
    @Test
    void canInstantiateFalseWithLessThanTwoActiveContracts() {
        DefaultCanonLog log = new DefaultCanonLog();
        log.append(CanonEvent.of(1L, CanonTime.of(1L, 1L), "intent:1:CONTRACT_ACTIVATE", intent("CONTRACT_ACTIVATE")));

        EconomicsArchetype archetype = new EconomicsArchetype();

        assertFalse(archetype.canInstantiate(log, CanonTime.of(2L, 2L)));
    }

    @Test
    void canInstantiateTrueWithTwoOrMoreActiveContracts() {
        DefaultCanonLog log = new DefaultCanonLog();
        log.append(CanonEvent.of(1L, CanonTime.of(1L, 1L), "intent:1:CONTRACT_ACTIVATE", intent("CONTRACT_ACTIVATE")));
        log.append(CanonEvent.of(2L, CanonTime.of(2L, 2L), "intent:2:CONTRACT_ACTIVATE", intent("CONTRACT_ACTIVATE")));

        EconomicsArchetype archetype = new EconomicsArchetype();

        assertTrue(archetype.canInstantiate(log, CanonTime.of(3L, 3L)));
    }

    @Test
    void instantiateProducesExpectedWorldEvent() {
        DefaultCanonLog log = new DefaultCanonLog();
        log.append(CanonEvent.of(1L, CanonTime.of(1L, 1L), "intent:1:CONTRACT_ACTIVATE", intent("CONTRACT_ACTIVATE")));
        log.append(CanonEvent.of(2L, CanonTime.of(2L, 2L), "intent:2:CONTRACT_ACTIVATE", intent("CONTRACT_ACTIVATE")));

        EconomicsArchetype archetype = new EconomicsArchetype();
        WorldEvent event = archetype.instantiate(log, CanonTime.of(3L, 3L));

        assertEquals("economics.market_tension", event.nodeId());
        assertEquals(archetype.archetypeId(), event.archetype());
    }

    @Test
    void maxConcurrentInstancesIsThree() {
        assertEquals(3, new EconomicsArchetype().maxConcurrentInstances());
    }

    private static Intent intent(String type) {
        return Intent.of(
                EntityId.of(1L),
                type,
                List.of(),
                "value=100",
                0.9D,
                CanonTime.ZERO,
                Intent.RequestedScope.PUBLIC);
    }
}
