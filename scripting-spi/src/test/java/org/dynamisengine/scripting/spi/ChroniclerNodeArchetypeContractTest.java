package org.dynamisengine.scripting.spi;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import org.dynamisengine.scripting.api.CanonLog;
import org.dynamisengine.scripting.api.value.CanonEvent;
import org.dynamisengine.scripting.api.value.CanonTime;
import org.dynamisengine.scripting.api.value.WorldEvent;
import org.junit.jupiter.api.Test;

public abstract class ChroniclerNodeArchetypeContractTest {
    protected abstract ChroniclerNodeArchetype archetype();

    @Test
    void archetypeIdReturnsNonNullNonEmpty() {
        String archetypeId = archetype().archetypeId();
        assertNotNull(archetypeId);
        assertFalse(archetypeId.isBlank());
    }

    @Test
    void maxConcurrentInstancesIsAtLeastOne() {
        assertTrue(archetype().maxConcurrentInstances() >= 1);
    }

    @Test
    void instantiateReturnsNonNullWorldEventWhenInstantiable() {
        CanonLog canonLog = emptyCanonLog();
        CanonTime currentTime = CanonTime.of(12L, 250L);

        if (archetype().canInstantiate(canonLog, currentTime)) {
            WorldEvent worldEvent = archetype().instantiate(canonLog, currentTime);
            assertNotNull(worldEvent);
            assertNotNull(worldEvent.nodeId());
            assertFalse(worldEvent.nodeId().isBlank());
        }
    }

    private static CanonLog emptyCanonLog() {
        return new CanonLog() {
            @Override
            public void append(CanonEvent event) {
            }

            @Override
            public List<CanonEvent> query(CanonTime from, CanonTime to) {
                return List.of();
            }

            @Override
            public List<CanonEvent> queryByCausalLink(String causalLink) {
                return List.of();
            }

            @Override
            public Optional<CanonEvent> findByCommitId(long commitId) {
                return Optional.empty();
            }

            @Override
            public CanonLog fork(long atCommitId) {
                return this;
            }

            @Override
            public void replay(long fromCommitId, Consumer<CanonEvent> handler) {
            }

            @Override
            public long latestCommitId() {
                return 0L;
            }

            @Override
            public CanonTime latestCanonTime() {
                return CanonTime.ZERO;
            }
        };
    }
}
