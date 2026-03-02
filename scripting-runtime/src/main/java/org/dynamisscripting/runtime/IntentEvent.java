package org.dynamisscripting.runtime;

import org.dynamis.core.event.EngineEvent;
import org.dynamis.core.event.EventPriority;
import org.dynamisscripting.api.value.Intent;

public record IntentEvent(Intent intent) implements EngineEvent {
    public IntentEvent {
        if (intent == null) {
            throw new RuntimeException("IntentEvent", "intent must not be null");
        }
    }

    @Override
    public EventPriority priority() {
        return EventPriority.HIGH;
    }
}
