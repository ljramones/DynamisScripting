package org.dynamisengine.scripting.spi;

import org.dynamisengine.core.entity.EntityId;
import org.dynamisengine.scripting.api.CanonLog;
import org.dynamisengine.scripting.api.value.Percept;

public interface PerceptFilter {
    String filterId();

    boolean shouldDeliver(Percept percept, EntityId agentId, CanonLog canonLog);

    Percept degrade(Percept percept, EntityId agentId, CanonLog canonLog);

    default int priority() {
        return 100;
    }
}
