package org.dynamisscripting.oracle;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.dynamis.core.event.EngineEvent;
import org.dynamis.event.EventBus;
import org.dynamisscripting.api.CanonLog;
import org.dynamisscripting.api.value.CanonEvent;
import org.dynamisscripting.api.value.CanonTime;
import org.dynamisscripting.api.value.Intent;
import org.dynamisscripting.api.value.WorldEvent;
import org.dynamisscripting.canon.CanonTimekeeper;

public final class CommitPhase {
    private final CanonLog canonLog;
    private final CanonTimekeeper timekeeper;
    private final AtomicLong commitIdCounter;
    private final EventBus eventBus;
    private final Function<CanonEvent, EngineEvent> canonEventMapper;

    public CommitPhase(CanonLog canonLog, CanonTimekeeper timekeeper, AtomicLong commitIdCounter) {
        this(canonLog, timekeeper, commitIdCounter, null, null);
    }

    @SuppressFBWarnings(
            value = {"EI_EXPOSE_REP2"},
            justification = "Optional EventBus is intentionally shared for runtime telemetry fanout")
    public CommitPhase(
            CanonLog canonLog,
            CanonTimekeeper timekeeper,
            AtomicLong commitIdCounter,
            EventBus eventBus,
            Function<CanonEvent, EngineEvent> canonEventMapper) {
        if (canonLog == null || timekeeper == null || commitIdCounter == null) {
            throw new OracleException("COMMIT", "canonLog, timekeeper, and commitIdCounter must not be null");
        }
        this.canonLog = canonLog;
        this.timekeeper = timekeeper;
        this.commitIdCounter = commitIdCounter;
        this.eventBus = eventBus;
        this.canonEventMapper = canonEventMapper;
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
        if (eventBus == null || canonEventMapper == null) {
            return;
        }
        EngineEvent mappedEvent = canonEventMapper.apply(event);
        if (mappedEvent != null) {
            eventBus.publish(mappedEvent);
        }
    }

    public record CommitPhaseResult(boolean committed, long commitId, CanonTime canonTime) {
    }
}
