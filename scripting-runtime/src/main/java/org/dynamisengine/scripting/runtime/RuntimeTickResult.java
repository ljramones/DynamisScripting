package org.dynamisengine.scripting.runtime;

import org.dynamisengine.scripting.api.value.CanonTime;

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
