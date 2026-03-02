package org.dynamisscripting.runtime;

import org.dynamis.core.entity.EntityId;

public record AgentTickDebt(
        EntityId agentId,
        long lastUpdatedTick,
        long currentCanonTick,
        AgentCognitiveTier tier) {

    public AgentTickDebt {
        if (agentId == null) {
            throw new RuntimeException("AgentTickDebt", "agentId must not be null");
        }
        if (tier == null) {
            throw new RuntimeException("AgentTickDebt", "tier must not be null");
        }
    }

    public long ticksBehind() {
        return currentCanonTick - lastUpdatedTick;
    }
}
