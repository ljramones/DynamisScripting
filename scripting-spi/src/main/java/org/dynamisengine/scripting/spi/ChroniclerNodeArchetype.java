package org.dynamisengine.scripting.spi;

import org.dynamisengine.scripting.api.CanonLog;
import org.dynamisengine.scripting.api.value.CanonTime;
import org.dynamisengine.scripting.api.value.WorldEvent;

public interface ChroniclerNodeArchetype {
    String archetypeId();

    String archetypeName();

    boolean canInstantiate(CanonLog canonLog, CanonTime currentTime);

    WorldEvent instantiate(CanonLog canonLog, CanonTime currentTime);

    default int maxConcurrentInstances() {
        return 1;
    }
}
