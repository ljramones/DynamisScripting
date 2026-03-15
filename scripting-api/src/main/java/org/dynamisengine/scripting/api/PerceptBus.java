package org.dynamisengine.scripting.api;

import java.util.function.Consumer;
import org.dynamisengine.core.entity.EntityId;
import org.dynamisengine.scripting.api.value.Percept;

public interface PerceptBus {
    void subscribe(EntityId agentId, Consumer<Percept> listener);

    void unsubscribe(EntityId agentId);

    void deliver(Percept percept);

    int subscriberCount();
}
