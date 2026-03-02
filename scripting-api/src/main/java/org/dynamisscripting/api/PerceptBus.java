package org.dynamisscripting.api;

import java.util.function.Consumer;
import org.dynamis.core.entity.EntityId;
import org.dynamisscripting.api.value.Percept;

public interface PerceptBus {
    void subscribe(EntityId agentId, Consumer<Percept> listener);

    void unsubscribe(EntityId agentId);

    void deliver(Percept percept);

    int subscriberCount();
}
