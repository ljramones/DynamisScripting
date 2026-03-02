package org.dynamisscripting.economy;

import org.dynamis.core.logging.DynamisLogger;
import org.dynamisscripting.api.value.WorldPatch;

public final class ContractCostTable {
    private static final DynamisLogger LOGGER = DynamisLogger.get(ContractCostTable.class);

    public double computeCost(String contractType, String tier, String region) {
        if (tier == null) {
            LOGGER.warn("Unknown tier 'null' for contractType=" + contractType + ", region=" + region);
            return 500.0D;
        }
        return switch (tier) {
            case "HIGH" -> 1000.0D;
            case "MEDIUM" -> 500.0D;
            case "LOW" -> 100.0D;
            default -> {
                LOGGER.warn("Unknown tier '" + tier + "' for contractType=" + contractType + ", region=" + region);
                yield 500.0D;
            }
        };
    }

    public void applyPatch(WorldPatch patch) {
        if (patch == null) {
            throw new EconomyException("applyPatch", "patch must not be null");
        }
        LOGGER.info("ContractCostTable patch applied: " + patch.version());
    }
}
