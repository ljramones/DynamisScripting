package org.dynamisscripting.runtime;

public record RuntimeConfiguration(
        long tickRateNanos,
        int maxNodeActivationsPerTick,
        int perceptStormThreshold,
        int agentUpdateRateMultiplier,
        DegradationTierThresholds degradationThresholds) {

    public RuntimeConfiguration {
        validatePositive(tickRateNanos, "tickRateNanos");
        validatePositive(maxNodeActivationsPerTick, "maxNodeActivationsPerTick");
        validatePositive(perceptStormThreshold, "perceptStormThreshold");
        validatePositive(agentUpdateRateMultiplier, "agentUpdateRateMultiplier");
        if (degradationThresholds == null) {
            throw new IllegalArgumentException("degradationThresholds must be provided");
        }
    }

    public static RuntimeConfiguration defaults() {
        return new RuntimeConfiguration(
                16_666_667L,
                10,
                100,
                3,
                new DegradationTierThresholds(5, 15, 30));
    }

    public static Builder builder() {
        RuntimeConfiguration defaults = defaults();
        return new Builder()
                .tickRateNanos(defaults.tickRateNanos())
                .maxNodeActivationsPerTick(defaults.maxNodeActivationsPerTick())
                .perceptStormThreshold(defaults.perceptStormThreshold())
                .agentUpdateRateMultiplier(defaults.agentUpdateRateMultiplier())
                .degradationThresholds(defaults.degradationThresholds());
    }

    public record DegradationTierThresholds(int tier1TicksBehind, int tier2TicksBehind, int tier3TicksBehind) {
        public DegradationTierThresholds {
            validatePositive(tier1TicksBehind, "tier1TicksBehind");
            validatePositive(tier2TicksBehind, "tier2TicksBehind");
            validatePositive(tier3TicksBehind, "tier3TicksBehind");
            if (tier1TicksBehind >= tier2TicksBehind || tier2TicksBehind >= tier3TicksBehind) {
                throw new IllegalArgumentException("thresholds must be increasing: tier1 < tier2 < tier3");
            }
        }
    }

    public static final class Builder {
        private long tickRateNanos;
        private int maxNodeActivationsPerTick;
        private int perceptStormThreshold;
        private int agentUpdateRateMultiplier;
        private DegradationTierThresholds degradationThresholds;

        private Builder() {
        }

        public Builder tickRateNanos(long value) {
            this.tickRateNanos = value;
            return this;
        }

        public Builder maxNodeActivationsPerTick(int value) {
            this.maxNodeActivationsPerTick = value;
            return this;
        }

        public Builder perceptStormThreshold(int value) {
            this.perceptStormThreshold = value;
            return this;
        }

        public Builder agentUpdateRateMultiplier(int value) {
            this.agentUpdateRateMultiplier = value;
            return this;
        }

        public Builder degradationThresholds(DegradationTierThresholds value) {
            this.degradationThresholds = value;
            return this;
        }

        public RuntimeConfiguration build() {
            return new RuntimeConfiguration(
                    tickRateNanos,
                    maxNodeActivationsPerTick,
                    perceptStormThreshold,
                    agentUpdateRateMultiplier,
                    degradationThresholds);
        }
    }

    private static void validatePositive(long value, String field) {
        if (value <= 0L) {
            throw new IllegalArgumentException(field + " must be > 0");
        }
    }
}
