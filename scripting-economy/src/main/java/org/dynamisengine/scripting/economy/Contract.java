package org.dynamisengine.scripting.economy;

import org.dynamisengine.core.entity.EntityId;
import org.dynamisengine.scripting.api.value.CanonTime;

public record Contract(
        String contractId,
        EntityId partyA,
        EntityId partyB,
        double value,
        String terms,
        ContractState state,
        CanonTime createdAt,
        CanonTime expiresAt) {

    public Contract {
        if (contractId == null || contractId.isBlank()) {
            throw new EconomyException("Contract", "contractId must not be null or blank");
        }
        if (partyA == null || partyB == null) {
            throw new EconomyException("Contract", "partyA and partyB must not be null");
        }
        if (value <= 0.0D) {
            throw new EconomyException("Contract", "value must be greater than 0");
        }
        if (terms == null) {
            throw new EconomyException("Contract", "terms must not be null");
        }
        if (state == null) {
            throw new EconomyException("Contract", "state must not be null");
        }
        if (createdAt == null || expiresAt == null) {
            throw new EconomyException("Contract", "createdAt and expiresAt must not be null");
        }
        if (!expiresAt.isAfter(createdAt)) {
            throw new EconomyException("Contract", "expiresAt must be after createdAt");
        }
    }

    public static Contract of(
            String contractId,
            EntityId partyA,
            EntityId partyB,
            double value,
            String terms,
            ContractState state,
            CanonTime createdAt,
            CanonTime expiresAt) {
        return new Contract(contractId, partyA, partyB, value, terms, state, createdAt, expiresAt);
    }

    public boolean isExpired(CanonTime currentTime) {
        if (currentTime == null) {
            throw new EconomyException("isExpired", "currentTime must not be null");
        }
        return currentTime.isAfter(expiresAt);
    }
}
