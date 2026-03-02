package org.dynamisscripting.chronicler;

import java.util.function.Consumer;
import org.dynamisscripting.api.value.WorldEvent;

public final class WorldEventEmitter {
    private Consumer<WorldEvent> listener;

    public WorldEventEmitter() {
    }

    public void registerListener(Consumer<WorldEvent> listener) {
        if (listener == null) {
            throw new ChroniclerException("registerListener", "listener must not be null");
        }
        this.listener = listener;
    }

    public void emit(WorldEvent event) {
        if (listener == null) {
            throw new ChroniclerException("emit", "no listener registered");
        }
        listener.accept(event);
    }

    public boolean hasListener() {
        return listener != null;
    }
}
