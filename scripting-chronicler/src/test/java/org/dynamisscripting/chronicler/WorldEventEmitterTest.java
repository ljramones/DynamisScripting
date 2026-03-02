package org.dynamisscripting.chronicler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import org.dynamisscripting.api.value.CanonTime;
import org.dynamisscripting.api.value.WorldEvent;
import org.junit.jupiter.api.Test;

class WorldEventEmitterTest {
    @Test
    void emitWithoutListenerThrows() {
        WorldEventEmitter emitter = new WorldEventEmitter();
        WorldEvent event = WorldEvent.of("n1", "authored", Map.of(), 1, CanonTime.of(1, 0));

        assertThrows(ChroniclerException.class, () -> emitter.emit(event));
    }

    @Test
    void emitWithListenerInvokesListener() {
        WorldEventEmitter emitter = new WorldEventEmitter();
        AtomicReference<WorldEvent> captured = new AtomicReference<>();
        emitter.registerListener(captured::set);
        WorldEvent event = WorldEvent.of("n1", "authored", Map.of(), 1, CanonTime.of(1, 0));

        emitter.emit(event);

        assertEquals("n1", captured.get().nodeId());
    }

    @Test
    void hasListenerReflectsRegistrationState() {
        WorldEventEmitter emitter = new WorldEventEmitter();
        assertFalse(emitter.hasListener());

        emitter.registerListener(event -> { });

        assertTrue(emitter.hasListener());
    }
}
