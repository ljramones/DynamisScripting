package org.dynamisengine.scripting.runtime;

public enum AgentCognitiveTier {
    TIER_0(0, "full cognition"),
    TIER_1(1, "stale snapshot"),
    TIER_2(2, "constitutional behavior"),
    TIER_3(3, "diegetic cover action");

    private final int level;
    private final String description;

    AgentCognitiveTier(int level, String description) {
        this.level = level;
        this.description = description;
    }

    public int level() {
        return level;
    }

    public String description() {
        return description;
    }
}
