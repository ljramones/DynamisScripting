package org.dynamisscripting.spi;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import org.dynamisscripting.api.CanonLog;
import org.dynamisscripting.api.value.CanonEvent;
import org.dynamisscripting.api.value.CanonTime;
import org.dynamisscripting.api.value.WorldPatch;
import org.dynamisscripting.spi.result.PatchValidationResult;
import org.junit.jupiter.api.Test;

public abstract class WorldPatchValidatorContractTest {
    protected abstract WorldPatchValidator validator();

    @Test
    void validatorIdReturnsNonNullNonEmpty() {
        String validatorId = validator().validatorId();
        assertNotNull(validatorId);
        assertFalse(validatorId.isBlank());
    }

    @Test
    void validateReturnsNonNullPatchValidationResult() {
        PatchValidationResult result = validator().validate(samplePatch(), emptyCanonLog());
        assertNotNull(result);
        assertNotNull(result.errors());
        assertNotNull(result.warnings());
    }

    @Test
    void validateDoesNotThrowForEmptyWorldPatch() {
        WorldPatch emptyPatch = WorldPatch.of("1.0.0", List.of(), List.of(), List.of());
        assertDoesNotThrow(() -> validator().validate(emptyPatch, emptyCanonLog()));
    }

    private static WorldPatch samplePatch() {
        return WorldPatch.of("1.0.0", List.of("ruleA"), List.of("scheduleA"), List.of("assetA"));
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
