package org.dynamisscripting.dsl;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import org.dynamisscripting.api.CanonLog;
import org.dynamisscripting.api.value.CanonEvent;
import org.dynamisscripting.api.value.CanonTime;

final class TestCanonLog implements CanonLog {
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
}
