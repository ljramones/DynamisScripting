package org.dynamisengine.scripting.spi;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import org.dynamisengine.core.entity.EntityId;
import org.dynamisengine.scripting.api.CanonLog;
import org.dynamisengine.scripting.api.value.CanonEvent;
import org.dynamisengine.scripting.api.value.CanonTime;
import org.dynamisengine.scripting.api.value.Intent;
import org.dynamisengine.scripting.api.value.WorldEvent;
import org.dynamisengine.scripting.spi.result.ShapeOutcome;
import org.dynamisengine.scripting.spi.result.ValidationOutcome;
import org.junit.jupiter.api.Test;

public abstract class ArbitrationRuleContractTest {
    protected abstract ArbitrationRule validateRule();

    protected abstract ArbitrationRule shapeRule();

    @Test
    void ruleIdReturnsNonNullNonEmpty() {
        String ruleId = validateRule().ruleId();
        assertNotNull(ruleId);
        assertFalse(ruleId.isBlank());
    }

    @Test
    void priorityIsNonNegative() {
        assertTrue(validateRule().priority() >= 0);
    }

    @Test
    void phaseIsNonNull() {
        assertNotNull(validateRule().phase());
    }

    @Test
    void validateRuleReturnsValidationOutcomeWithReasonCode() {
        ValidationOutcome outcome = validateRule().evaluate(sampleIntent(), emptyCanonLog());
        assertNotNull(outcome);
        assertNotNull(outcome.reasonCode());
    }

    @Test
    void shapeRuleReturnsShapeOutcomeAndResultWhenShaped() {
        ShapeOutcome outcome = shapeRule().shape(sampleIntent(), emptyCanonLog());
        assertNotNull(outcome);
        if (outcome.shaped()) {
            assertNotNull(outcome.result());
        }
    }

    @Test
    void validateRuleShapeThrowsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class,
                () -> validateRule().shape(sampleIntent(), emptyCanonLog()));
    }

    @Test
    void appliesToReturnsBooleanWithoutThrowing() {
        assertDoesNotThrow(() -> validateRule().appliesTo(sampleIntent()));
    }

    private static Intent sampleIntent() {
        return Intent.of(
                EntityId.of(1L),
                "test.intent",
                List.of(EntityId.of(2L)),
                "contract assertion",
                0.75D,
                CanonTime.of(10L, 100L),
                Intent.RequestedScope.PUBLIC);
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
