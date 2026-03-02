package org.dynamisscripting.spi;

import org.dynamisscripting.api.CanonLog;
import org.dynamisscripting.api.value.CanonTime;
import org.dynamisscripting.api.value.WorldEvent;

public interface ChroniclerNodeArchetype {
    String archetypeId();

    String archetypeName();

    boolean canInstantiate(CanonLog canonLog, CanonTime currentTime);

    WorldEvent instantiate(CanonLog canonLog, CanonTime currentTime);

    default int maxConcurrentInstances() {
        return 1;
    }
}
