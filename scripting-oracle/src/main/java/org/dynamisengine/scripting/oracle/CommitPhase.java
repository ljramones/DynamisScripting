package org.dynamisengine.scripting.oracle;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.dynamisengine.scripting.api.CanonLog;
import org.dynamisengine.scripting.api.value.CanonEvent;
import org.dynamisengine.scripting.api.value.CanonTime;
import org.dynamisengine.scripting.api.value.Intent;
import org.dynamisengine.scripting.api.value.WorldEvent;
import org.dynamisengine.scripting.canon.CanonTimekeeper;

public final class CommitPhase {
    private final CanonLog canonLog;
    private final CanonTimekeeper timekeeper;
    private final AtomicLong commitIdCounter;
    private final Consumer<CanonEvent> commitListener;

    public CommitPhase(CanonLog canonLog, CanonTimekeeper timekeeper, AtomicLong commitIdCounter) {
        this(canonLog, timekeeper, commitIdCounter, null);
    }

    @SuppressFBWarnings(
            value = {"EI_EXPOSE_REP2"},
            justification = "Shared CanonLog/commit counter references are intentional for deterministic runtime orchestration")
    public CommitPhase(
            CanonLog canonLog,
            CanonTimekeeper timekeeper,
            AtomicLong commitIdCounter,
            Consumer<CanonEvent> commitListener) {
        if (canonLog == null || timekeeper == null || commitIdCounter == null) {
            throw new OracleException("COMMIT", "canonLog, timekeeper, and commitIdCounter must not be null");
        }
        this.canonLog = canonLog;
        this.timekeeper = timekeeper;
        this.commitIdCounter = commitIdCounter;
        this.commitListener = commitListener;
    }

    public CommitPhaseResult run(Intent intent, String causalLink) {
        if (intent == null) {
            throw new OracleException("COMMIT", "intent must not be null");
        }
        if (causalLink == null || causalLink.isBlank()) {
            throw new OracleException("COMMIT", "causalLink must not be null or empty");
        }

        CanonTime canonTime = timekeeper.advance(1L);
        long commitId = commitIdCounter.getAndIncrement();
        CanonEvent event = CanonEvent.of(commitId, canonTime, causalLink, intent);
        canonLog.append(event);
        publishCanonLogEvent(event);
        return new CommitPhaseResult(true, commitId, canonTime);
    }

    public CommitPhaseResult runWorldEvent(WorldEvent worldEvent, String causalLink) {
        if (worldEvent == null) {
            throw new OracleException("COMMIT", "worldEvent must not be null");
        }
        if (causalLink == null || causalLink.isBlank()) {
            throw new OracleException("COMMIT", "causalLink must not be null or empty");
        }

        CanonTime canonTime = timekeeper.current();
        long commitId = commitIdCounter.getAndIncrement();
        CanonEvent event = CanonEvent.of(commitId, canonTime, causalLink, worldEvent);
        canonLog.append(event);
        publishCanonLogEvent(event);
        return new CommitPhaseResult(true, commitId, canonTime);
    }

    private void publishCanonLogEvent(CanonEvent event) {
        if (commitListener == null) {
            return;
        }
        commitListener.accept(event);
    }

    public record CommitPhaseResult(boolean committed, long commitId, CanonTime canonTime) {
    }
}
