package org.dynamisengine.scripting.api;

import java.util.function.Consumer;
import org.dynamisengine.scripting.api.value.Intent;

public interface IntentBus {
    void emit(Intent intent);

    void subscribe(Consumer<Intent> listener);
}
