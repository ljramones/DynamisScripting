package org.dynamisengine.scripting.runtime;

import org.dynamisengine.core.event.EngineEvent;
import org.dynamisengine.core.event.EventPriority;
import org.dynamisengine.scripting.api.value.CanonEvent;

public record CanonLogEvent(CanonEvent canonEvent) implements EngineEvent {
    public CanonLogEvent {
        if (canonEvent == null) {
            throw new RuntimeException("CanonLogEvent", "canonEvent must not be null");
        }
    }

    @Override
    public EventPriority priority() {
        return EventPriority.NORMAL;
    }

    @Override
    public long timestamp() {
        return System.nanoTime();
    }
}
