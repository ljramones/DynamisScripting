package org.dynamisengine.scripting.runtime;

import org.dynamisengine.scripting.api.value.CanonTime;

public record RuntimeTickResult(
        CanonTime canonTime,
        int worldEventsProposed,
        int worldEventsCommitted,
        long tickDurationNanos,
        long chroniclerNanos,
        long evaluationErrors,
        long cacheHits,
        long cacheMisses,
        int cacheSize) {

    public RuntimeTickResult(CanonTime canonTime, int worldEventsProposed, int worldEventsCommitted, long tickDurationNanos) {
        this(canonTime, worldEventsProposed, worldEventsCommitted, tickDurationNanos, 0, 0, 0, 0, 0);
    }

    public RuntimeTickResult {
        if (canonTime == null) {
            throw new RuntimeException("RuntimeTickResult", "canonTime must not be null");
        }
    }
}
