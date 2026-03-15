package org.dynamisengine.scripting.api;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import org.dynamisengine.scripting.api.value.CanonEvent;
import org.dynamisengine.scripting.api.value.CanonTime;

public interface CanonLog {
    void append(CanonEvent event);

    List<CanonEvent> query(CanonTime from, CanonTime to);

    List<CanonEvent> queryByCausalLink(String causalLink);

    Optional<CanonEvent> findByCommitId(long commitId);

    CanonLog fork(long atCommitId);

    void replay(long fromCommitId, Consumer<CanonEvent> handler);

    long latestCommitId();

    CanonTime latestCanonTime();
}
