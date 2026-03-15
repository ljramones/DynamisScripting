package org.dynamisengine.scripting.runtime;

import org.dynamisengine.core.event.EngineEvent;
import org.dynamisengine.core.event.EventPriority;
import org.dynamisengine.scripting.api.value.Intent;

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

    @Override
    public long timestamp() {
        return System.nanoTime();
    }
}
