package org.dynamisscripting.spi;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import org.dynamis.core.entity.EntityId;
import org.dynamisscripting.api.CanonLog;
import org.dynamisscripting.api.WorldOracle.CommitResult;
import org.dynamisscripting.api.WorldOracle.ValidationResult;
import org.dynamisscripting.api.value.CanonEvent;
import org.dynamisscripting.api.value.CanonTime;
import org.dynamisscripting.api.value.Intent;
import org.junit.jupiter.api.Test;

public abstract class IntentInterceptorContractTest {
    protected abstract IntentInterceptor interceptor();

    @Test
    void interceptorIdReturnsNonNullNonEmpty() {
        String interceptorId = interceptor().interceptorId();
        assertNotNull(interceptorId);
        assertFalse(interceptorId.isBlank());
    }

    @Test
    void beforeValidateReturnsNonNullIntent() {
        Intent result = interceptor().beforeValidate(sampleIntent());
        assertNotNull(result);
    }

    @Test
    void afterCommitDoesNotThrowWithEmptyCanonLog() {
        assertDoesNotThrow(() -> interceptor().afterCommit(
                sampleIntent(),
                new CommitResult(true, 1L, "commit.ok"),
                emptyCanonLog()));
    }

    @Test
    void afterRejectDoesNotThrow() {
        assertDoesNotThrow(() -> interceptor().afterReject(
                sampleIntent(),
                new ValidationResult(false, "validation.failed", "explanation")));
    }

    private static Intent sampleIntent() {
        return Intent.of(
                EntityId.of(1L),
                "social.accuse",
                List.of(EntityId.of(2L)),
                "contract test",
                0.7D,
                CanonTime.of(2L, 200L),
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
