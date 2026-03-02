package org.dynamisscripting.runtime;

import org.dynamis.core.event.EngineEvent;
import org.dynamis.core.event.EventPriority;
import org.dynamisscripting.api.value.CanonEvent;

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
}
