package org.dynamisengine.scripting.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.dynamisengine.core.entity.EntityId;
import org.junit.jupiter.api.Test;

class DegradationMonitorTest {

    @Test
    void tiersFollowDebtThresholds() {
        RuntimeConfiguration.DegradationTierThresholds thresholds =
                new RuntimeConfiguration.DegradationTierThresholds(5, 15, 30);
        DegradationMonitor monitor = new DegradationMonitor(thresholds);
        EntityId agent = EntityId.of(101L);

        assertEquals(AgentCognitiveTier.TIER_3, monitor.getTier(agent, 10L));

        monitor.registerAgent(agent);
        assertEquals(AgentCognitiveTier.TIER_3, monitor.getTier(agent, 10L));

        monitor.recordAgentUpdate(agent, 10L);
        assertEquals(AgentCognitiveTier.TIER_0, monitor.getTier(agent, 10L));
        assertEquals(AgentCognitiveTier.TIER_1, monitor.getTier(agent, 15L));
        assertEquals(AgentCognitiveTier.TIER_2, monitor.getTier(agent, 25L));
        assertEquals(AgentCognitiveTier.TIER_3, monitor.getTier(agent, 40L));
    }

    @Test
    void allTiersSnapshotIncludesRegisteredAgents() {
        DegradationMonitor monitor = new DegradationMonitor(
                new RuntimeConfiguration.DegradationTierThresholds(5, 15, 30));
        EntityId a = EntityId.of(1L);
        EntityId b = EntityId.of(2L);
        monitor.registerAgent(a);
        monitor.registerAgent(b);
        monitor.recordAgentUpdate(a, 10L);

        Map<EntityId, AgentCognitiveTier> tiers = monitor.allTiers(10L);

        assertEquals(2, tiers.size());
        assertEquals(AgentCognitiveTier.TIER_0, tiers.get(a));
        assertEquals(AgentCognitiveTier.TIER_3, tiers.get(b));
    }

    @Test
    void concurrentRecordAgentUpdateIsThreadSafe() throws InterruptedException {
        DegradationMonitor monitor = new DegradationMonitor(
                new RuntimeConfiguration.DegradationTierThresholds(5, 15, 30));
        List<EntityId> agents = List.of(
                EntityId.of(11L),
                EntityId.of(12L),
                EntityId.of(13L),
                EntityId.of(14L),
                EntityId.of(15L),
                EntityId.of(16L),
                EntityId.of(17L),
                EntityId.of(18L),
                EntityId.of(19L),
                EntityId.of(20L));
        for (EntityId agent : agents) {
            monitor.registerAgent(agent);
        }

        ExecutorService pool = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(10);
        List<Throwable> failures = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            final int index = i;
            pool.submit(() -> {
                try {
                    monitor.recordAgentUpdate(agents.get(index), 25L + index);
                } catch (Throwable throwable) {
                    failures.add(throwable);
                } finally {
                    latch.countDown();
                }
            });
        }

        assertTrue(latch.await(5, TimeUnit.SECONDS));
        pool.shutdownNow();
        assertTrue(failures.isEmpty());
        assertFalse(monitor.allTiers(30L).isEmpty());
    }
}
