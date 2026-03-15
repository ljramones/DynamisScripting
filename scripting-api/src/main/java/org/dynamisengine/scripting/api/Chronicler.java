package org.dynamisengine.scripting.api;

import java.util.function.Consumer;
import org.dynamisengine.scripting.api.value.CanonTime;
import org.dynamisengine.scripting.api.value.WorldEvent;

public interface Chronicler {
    void tick(CanonTime currentTime);

    void registerWorldEventListener(Consumer<WorldEvent> listener);
}
