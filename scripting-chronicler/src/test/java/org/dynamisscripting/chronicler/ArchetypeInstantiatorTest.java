package org.dynamisscripting.chronicler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.dynamisscripting.api.CanonLog;
import org.dynamisscripting.api.value.CanonTime;
import org.dynamisscripting.api.value.WorldEvent;
import org.dynamisscripting.canon.DefaultCanonLog;
import org.dynamisscripting.spi.ChroniclerNodeArchetype;
import org.junit.jupiter.api.Test;

class ArchetypeInstantiatorTest {
    @Test
    void tryInstantiateReturnsWorldEventWhenCanInstantiateTrue() {
        ArchetypeInstantiator instantiator = new ArchetypeInstantiator(List.of());
        ChroniclerNodeArchetype archetype = archetype("a", true);

        Optional<WorldEvent> event = instantiator.tryInstantiate(archetype, new DefaultCanonLog(), CanonTime.of(1, 0));

        assertTrue(event.isPresent());
    }

    @Test
    void tryInstantiateReturnsEmptyWhenCanInstantiateFalse() {
        ArchetypeInstantiator instantiator = new ArchetypeInstantiator(List.of());
        ChroniclerNodeArchetype archetype = archetype("a", false);

        Optional<WorldEvent> event = instantiator.tryInstantiate(archetype, new DefaultCanonLog(), CanonTime.of(1, 0));

        assertTrue(event.isEmpty());
    }

    @Test
    void duplicateArchetypeIdThrows() {
        ArchetypeInstantiator instantiator = new ArchetypeInstantiator(List.of());
        instantiator.registerArchetype(archetype("dup", true));

        assertThrows(ChroniclerException.class, () -> instantiator.registerArchetype(archetype("dup", true)));
    }

    @Test
    void evaluateAllReturnsOnlyInstantiableWorldEvents() {
        ArchetypeInstantiator instantiator = new ArchetypeInstantiator(List.of(archetype("a", true), archetype("b", false)));

        List<WorldEvent> events = instantiator.evaluateAll(new DefaultCanonLog(), CanonTime.of(2, 0));

        assertEquals(1, events.size());
        assertEquals("a-node", events.get(0).nodeId());
    }

    private static ChroniclerNodeArchetype archetype(String id, boolean canInstantiate) {
        return new ChroniclerNodeArchetype() {
            @Override
            public String archetypeId() {
                return id;
            }

            @Override
            public String archetypeName() {
                return id;
            }

            @Override
            public boolean canInstantiate(CanonLog canonLog, CanonTime currentTime) {
                return canInstantiate;
            }

            @Override
            public WorldEvent instantiate(CanonLog canonLog, CanonTime currentTime) {
                return WorldEvent.of(id + "-node", id, Map.of(), 1, currentTime);
            }
        };
    }
}
