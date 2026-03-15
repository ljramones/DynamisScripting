package org.dynamisengine.scripting.runtime;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.dynamisengine.core.entity.EntityId;
import org.dynamisengine.scripting.runtime.RuntimeConfiguration.DegradationTierThresholds;

public final class DegradationMonitor {
    private static final long UNINITIALIZED_TICK = -1L;

    private final DegradationTierThresholds thresholds;
    private final ConcurrentHashMap<EntityId, Long> lastUpdatedTicks;

    public DegradationMonitor(DegradationTierThresholds thresholds) {
        if (thresholds == null) {
            throw new RuntimeException("DegradationMonitor", "thresholds must not be null");
        }
        this.thresholds = thresholds;
        this.lastUpdatedTicks = new ConcurrentHashMap<>();
    }

    public void registerAgent(EntityId agentId) {
        if (agentId == null) {
            throw new RuntimeException("registerAgent", "agentId must not be null");
        }
        lastUpdatedTicks.putIfAbsent(agentId, UNINITIALIZED_TICK);
    }

    public void recordAgentUpdate(EntityId agentId, long canonTick) {
        if (agentId == null) {
            throw new RuntimeException("recordAgentUpdate", "agentId must not be null");
        }
        lastUpdatedTicks.put(agentId, canonTick);
    }

    public AgentCognitiveTier getTier(EntityId agentId, long currentCanonTick) {
        if (agentId == null) {
            throw new RuntimeException("getTier", "agentId must not be null");
        }
        Long lastUpdatedTick = lastUpdatedTicks.get(agentId);
        if (lastUpdatedTick == null || lastUpdatedTick < 0L) {
            return AgentCognitiveTier.TIER_3;
        }

        long debt = Math.max(0L, currentCanonTick - lastUpdatedTick);
        if (debt < thresholds.tier1TicksBehind()) {
            return AgentCognitiveTier.TIER_0;
        }
        if (debt < thresholds.tier2TicksBehind()) {
            return AgentCognitiveTier.TIER_1;
        }
        if (debt < thresholds.tier3TicksBehind()) {
            return AgentCognitiveTier.TIER_2;
        }
        return AgentCognitiveTier.TIER_3;
    }

    public AgentTickDebt getDebt(EntityId agentId, long currentCanonTick) {
        if (agentId == null) {
            throw new RuntimeException("getDebt", "agentId must not be null");
        }
        long lastUpdatedTick = lastUpdatedTicks.getOrDefault(agentId, UNINITIALIZED_TICK);
        return new AgentTickDebt(agentId, lastUpdatedTick, currentCanonTick, getTier(agentId, currentCanonTick));
    }

    public Map<EntityId, AgentCognitiveTier> allTiers(long currentCanonTick) {
        Map<EntityId, AgentCognitiveTier> snapshot = new HashMap<>();
        for (Map.Entry<EntityId, Long> entry : lastUpdatedTicks.entrySet()) {
            snapshot.put(entry.getKey(), getTier(entry.getKey(), currentCanonTick));
        }
        return Map.copyOf(snapshot);
    }

    public int agentCount() {
        return lastUpdatedTicks.size();
    }

    public java.util.Set<EntityId> registeredAgents() {
        return java.util.Set.copyOf(lastUpdatedTicks.keySet());
    }
}
