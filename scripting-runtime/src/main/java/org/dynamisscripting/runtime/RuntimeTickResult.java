package org.dynamisscripting.runtime;

import org.dynamisscripting.api.value.CanonTime;

public record RuntimeTickResult(
        CanonTime canonTime,
        int worldEventsProposed,
        int worldEventsCommitted,
        long tickDurationNanos) {

    public RuntimeTickResult {
        if (canonTime == null) {
            throw new RuntimeException("RuntimeTickResult", "canonTime must not be null");
        }
    }
}
