package org.dynamisscripting.runtime;

import java.util.function.Consumer;
import org.dynamis.event.EventBus;
import org.dynamisscripting.api.IntentBus;
import org.dynamisscripting.api.value.Intent;

public final class IntentBusImpl implements IntentBus {
    private final EventBus eventBus;

    public IntentBusImpl(EventBus eventBus) {
        if (eventBus == null) {
            throw new RuntimeException("IntentBusImpl", "eventBus must not be null");
        }
        this.eventBus = eventBus;
    }

    @Override
    public void emit(Intent intent) {
        if (intent == null) {
            throw new RuntimeException("emit", "intent must not be null");
        }
        eventBus.publish(new IntentEvent(intent));
    }

    @Override
    public void subscribe(Consumer<Intent> listener) {
        if (listener == null) {
            throw new RuntimeException("subscribe", "listener must not be null");
        }
        eventBus.subscribe(IntentEvent.class, event -> listener.accept(event.intent()));
    }
}
