package org.dynamisengine.scripting.economy;

import java.util.List;
import org.dynamisengine.scripting.api.value.WorldPatch;
import org.dynamisengine.scripting.spi.CanonDimensionProvider;

public final class EconomicsDimension implements CanonDimensionProvider {
    private final ContractCostTable costTable = new ContractCostTable();

    @Override
    public String dimensionId() {
        return "economics";
    }

    @Override
    public String dimensionName() {
        return "Economics — Contracts, Funds, and Markets";
    }

    @Override
    public List<String> canonicalObjectTypes() {
        return List.of("Contract", "FactionFunds", "BountyRecord", "AvailabilityPool");
    }

    @Override
    public void onWorldPatchApplied(WorldPatch patch) {
        costTable.applyPatch(patch);
    }
}
