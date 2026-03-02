package org.dynamisscripting.api;

import java.util.function.Consumer;
import org.dynamisscripting.api.value.Intent;

public interface IntentBus {
    void emit(Intent intent);

    void subscribe(Consumer<Intent> listener);
}
