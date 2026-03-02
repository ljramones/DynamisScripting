package org.dynamisscripting.economy;

import org.dynamis.core.entity.EntityId;
import org.dynamisscripting.api.value.CanonTime;

public record BountyRecord(
        String bountyId,
        EntityId targetAgentId,
        EntityId placingFactionId,
        double value,
        ContractState state,
        CanonTime placedAt) {

    public BountyRecord {
        if (bountyId == null || bountyId.isBlank()) {
            throw new EconomyException("BountyRecord", "bountyId must not be null or blank");
        }
        if (targetAgentId == null || placingFactionId == null) {
            throw new EconomyException("BountyRecord", "targetAgentId and placingFactionId must not be null");
        }
        if (value <= 0.0D) {
            throw new EconomyException("BountyRecord", "value must be > 0");
        }
        if (state == null) {
            throw new EconomyException("BountyRecord", "state must not be null");
        }
        if (state != ContractState.PENDING && state != ContractState.ACTIVE && state != ContractState.DISSOLVED) {
            throw new EconomyException("BountyRecord", "state must be PENDING, ACTIVE, or DISSOLVED");
        }
        if (placedAt == null) {
            throw new EconomyException("BountyRecord", "placedAt must not be null");
        }
    }

    public static BountyRecord of(
            String bountyId,
            EntityId targetAgentId,
            EntityId placingFactionId,
            double value,
            ContractState state,
            CanonTime placedAt) {
        return new BountyRecord(bountyId, targetAgentId, placingFactionId, value, state, placedAt);
    }
}
