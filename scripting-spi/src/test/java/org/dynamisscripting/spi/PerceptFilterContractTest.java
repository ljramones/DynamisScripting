package org.dynamisscripting.spi;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import org.dynamis.core.entity.EntityId;
import org.dynamisscripting.api.CanonLog;
import org.dynamisscripting.api.value.CanonEvent;
import org.dynamisscripting.api.value.CanonTime;
import org.dynamisscripting.api.value.Percept;
import org.junit.jupiter.api.Test;

public abstract class PerceptFilterContractTest {
    protected abstract PerceptFilter filter();

    @Test
    void filterIdReturnsNonNullNonEmpty() {
        String filterId = filter().filterId();
        assertNotNull(filterId);
        assertFalse(filterId.isBlank());
    }

    @Test
    void shouldDeliverReturnsBooleanWithoutThrowing() {
        Percept percept = samplePercept();
        assertDoesNotThrow(() -> filter().shouldDeliver(percept, EntityId.of(1L), emptyCanonLog()));
    }

    @Test
    void degradeReturnsNonNullPercept() {
        Percept degraded = filter().degrade(samplePercept(), EntityId.of(1L), emptyCanonLog());
        assertNotNull(degraded);
    }

    @Test
    void degradeNeverImprovesFidelity() {
        Percept original = samplePercept();
        Percept degraded = filter().degrade(original, EntityId.of(1L), emptyCanonLog());
        assertNotNull(degraded);
        assertTrue(degraded.fidelity() <= original.fidelity());
    }

    @Test
    void priorityIsNonNegative() {
        assertTrue(filter().priority() >= 0);
    }

    private static Percept samplePercept() {
        return Percept.of(EntityId.of(1L), "auditory.signal", "payload", 0.8D, 1L);
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
