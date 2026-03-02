package org.dynamisscripting.runtime;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;
import org.dynamis.core.entity.EntityId;
import org.dynamis.event.EventBus;
import org.dynamisscripting.api.CanonLog;
import org.dynamisscripting.api.IntentBus;
import org.dynamisscripting.api.value.CanonEvent;
import org.dynamisscripting.api.value.CanonTime;
import org.dynamisscripting.api.value.WorldPatch;
import org.dynamisscripting.canon.CanonTimekeeper;
import org.dynamisscripting.dsl.DslCompiler;
import org.dynamisscripting.spi.CanonDimensionProvider;

public final class ScriptingRuntime {
    private final CanonLog canonLog;
    private final CanonTimekeeper timekeeper;
    private final DegradationMonitor degradationMonitor;
    private final RuntimeTick runtimeTick;
    private final DslCompiler dslCompiler;
    private final List<CanonDimensionProvider> dimensions;
    private final AtomicLong commitIdCounter;
    private final EventBus eventBus;
    private final IntentBus intentBus;
    private final AtomicBoolean running;
    private final ReentrantLock patchLock;

    ScriptingRuntime(
            CanonLog canonLog,
            CanonTimekeeper timekeeper,
            DegradationMonitor degradationMonitor,
            RuntimeTick runtimeTick,
            DslCompiler dslCompiler,
            List<CanonDimensionProvider> dimensions,
            AtomicLong commitIdCounter,
            EventBus eventBus,
            IntentBus intentBus) {
        this.canonLog = requireNonNull(canonLog, "canonLog");
        this.timekeeper = requireNonNull(timekeeper, "timekeeper");
        this.degradationMonitor = requireNonNull(degradationMonitor, "degradationMonitor");
        this.runtimeTick = requireNonNull(runtimeTick, "runtimeTick");
        this.dslCompiler = requireNonNull(dslCompiler, "dslCompiler");
        this.dimensions = List.copyOf(requireNonNull(dimensions, "dimensions"));
        this.commitIdCounter = requireNonNull(commitIdCounter, "commitIdCounter");
        this.eventBus = requireNonNull(eventBus, "eventBus");
        this.intentBus = requireNonNull(intentBus, "intentBus");
        this.running = new AtomicBoolean(false);
        this.patchLock = new ReentrantLock();
    }

    public RuntimeTickResult tick() {
        return runtimeTick.execute();
    }

    public void applyPatch(WorldPatch patch) {
        if (patch == null) {
            throw new RuntimeException("applyPatch", "patch must not be null");
        }

        patchLock.lock();
        try {
            for (CanonDimensionProvider dimension : dimensions) {
                dimension.onWorldPatchApplied(patch);
            }
            dslCompiler.invalidateCache();

            long commitId = Math.max(commitIdCounter.get(), canonLog.latestCommitId() + 1L);
            commitIdCounter.set(commitId + 1L);
            CanonEvent patchEvent = CanonEvent.of(
                    commitId,
                    timekeeper.current(),
                    "worldpatch:" + patch.version(),
                    "WorldPatchApplied(" + patch.version() + ")");
            canonLog.append(patchEvent);
            eventBus.publish(new CanonLogEvent(patchEvent));
        } finally {
            patchLock.unlock();
        }
    }

    public void seedEvent(CanonEvent event) {
        // Demo/test seeding only. Not for production use. Bypasses Oracle arbitration.
        if (event == null) {
            throw new RuntimeException("seedEvent", "event must not be null");
        }
        patchLock.lock();
        try {
            canonLog.append(event);
            commitIdCounter.set(Math.max(commitIdCounter.get(), event.commitId() + 1L));
            eventBus.publish(new CanonLogEvent(event));
        } finally {
            patchLock.unlock();
        }
    }

    @SuppressFBWarnings(
            value = {"EI_EXPOSE_REP"},
            justification = "Runtime intentionally exposes CanonLog only via the immutable interface contract")
    public CanonLog canonLog() {
        return canonLog;
    }

    public EventBus eventBus() {
        return eventBus;
    }

    public IntentBus intentBus() {
        return intentBus;
    }

    public DegradationMonitor degradationMonitor() {
        return degradationMonitor;
    }

    public CanonTime currentTime() {
        return timekeeper.current();
    }

    public void registerAgent(EntityId agentId) {
        degradationMonitor.registerAgent(agentId);
    }

    public void recordAgentUpdate(EntityId agentId) {
        degradationMonitor.recordAgentUpdate(agentId, timekeeper.current().tick());
    }

    public boolean isRunning() {
        return running.get();
    }

    public void start() {
        running.set(true);
    }

    public void stop() {
        running.set(false);
    }

    private static <T> T requireNonNull(T value, String field) {
        if (value == null) {
            throw new RuntimeException("ScriptingRuntime", field + " must not be null");
        }
        return value;
    }
}
