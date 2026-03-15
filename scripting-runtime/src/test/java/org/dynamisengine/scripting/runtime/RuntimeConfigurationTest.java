package org.dynamisengine.scripting.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class RuntimeConfigurationTest {

    @Test
    void defaultsReturnsExpectedValues() {
        RuntimeConfiguration config = RuntimeConfiguration.defaults();

        assertEquals(16_666_667L, config.tickRateNanos());
        assertEquals(10, config.maxNodeActivationsPerTick());
        assertEquals(100, config.perceptStormThreshold());
        assertEquals(3, config.agentUpdateRateMultiplier());
        assertEquals(5, config.degradationThresholds().tier1TicksBehind());
        assertEquals(15, config.degradationThresholds().tier2TicksBehind());
        assertEquals(30, config.degradationThresholds().tier3TicksBehind());
    }

    @Test
    void builderSetsAllFields() {
        RuntimeConfiguration config = RuntimeConfiguration.builder()
                .tickRateNanos(1_000_000L)
                .maxNodeActivationsPerTick(7)
                .perceptStormThreshold(80)
                .agentUpdateRateMultiplier(2)
                .degradationThresholds(new RuntimeConfiguration.DegradationTierThresholds(3, 6, 9))
                .build();

        assertEquals(1_000_000L, config.tickRateNanos());
        assertEquals(7, config.maxNodeActivationsPerTick());
        assertEquals(80, config.perceptStormThreshold());
        assertEquals(2, config.agentUpdateRateMultiplier());
        assertEquals(3, config.degradationThresholds().tier1TicksBehind());
        assertEquals(6, config.degradationThresholds().tier2TicksBehind());
        assertEquals(9, config.degradationThresholds().tier3TicksBehind());
    }

    @Test
    void builderRejectsNonPositiveValues() {
        assertThrows(IllegalArgumentException.class, () -> RuntimeConfiguration.builder()
                .tickRateNanos(0L)
                .maxNodeActivationsPerTick(1)
                .perceptStormThreshold(1)
                .agentUpdateRateMultiplier(1)
                .degradationThresholds(new RuntimeConfiguration.DegradationTierThresholds(1, 2, 3))
                .build());

        assertThrows(IllegalArgumentException.class, () -> RuntimeConfiguration.builder()
                .tickRateNanos(1L)
                .maxNodeActivationsPerTick(-1)
                .perceptStormThreshold(1)
                .agentUpdateRateMultiplier(1)
                .degradationThresholds(new RuntimeConfiguration.DegradationTierThresholds(1, 2, 3))
                .build());
    }
}
