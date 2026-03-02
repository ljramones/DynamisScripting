package org.dynamisscripting.api;

import java.util.function.Consumer;
import org.dynamisscripting.api.value.CanonTime;
import org.dynamisscripting.api.value.WorldEvent;

public interface Chronicler {
    void tick(CanonTime currentTime);

    void registerWorldEventListener(Consumer<WorldEvent> listener);
}
