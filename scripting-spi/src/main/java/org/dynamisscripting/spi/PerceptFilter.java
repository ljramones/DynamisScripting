package org.dynamisscripting.spi;

import org.dynamis.core.entity.EntityId;
import org.dynamisscripting.api.CanonLog;
import org.dynamisscripting.api.value.Percept;

public interface PerceptFilter {
    String filterId();

    boolean shouldDeliver(Percept percept, EntityId agentId, CanonLog canonLog);

    Percept degrade(Percept percept, EntityId agentId, CanonLog canonLog);

    default int priority() {
        return 100;
    }
}
