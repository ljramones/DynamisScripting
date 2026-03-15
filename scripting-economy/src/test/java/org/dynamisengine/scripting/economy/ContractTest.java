package org.dynamisengine.scripting.economy;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.dynamisengine.core.entity.EntityId;
import org.dynamisengine.scripting.api.value.CanonTime;
import org.junit.jupiter.api.Test;

class ContractTest {
    @Test
    void isExpiredReflectsCurrentTime() {
        Contract contract = Contract.of(
                "c1",
                EntityId.of(1L),
                EntityId.of(2L),
                100.0D,
                "terms",
                ContractState.ACTIVE,
                CanonTime.of(1L, 100L),
                CanonTime.of(5L, 500L));

        assertFalse(contract.isExpired(CanonTime.of(3L, 300L)));
        assertTrue(contract.isExpired(CanonTime.of(6L, 600L)));
    }

    @Test
    void validationRejectsInvalidInputs() {
        assertThrows(
                EconomyException.class,
                () -> Contract.of(
                        "c1",
                        EntityId.of(1L),
                        EntityId.of(2L),
                        0.0D,
                        "terms",
                        ContractState.ACTIVE,
                        CanonTime.of(1L, 1L),
                        CanonTime.of(2L, 2L)));

        assertThrows(
                EconomyException.class,
                () -> Contract.of(
                        "c2",
                        null,
                        EntityId.of(2L),
                        100.0D,
                        "terms",
                        ContractState.ACTIVE,
                        CanonTime.of(1L, 1L),
                        CanonTime.of(2L, 2L)));

        assertThrows(
                EconomyException.class,
                () -> Contract.of(
                        "c3",
                        EntityId.of(1L),
                        EntityId.of(2L),
                        100.0D,
                        "terms",
                        ContractState.ACTIVE,
                        CanonTime.of(3L, 3L),
                        CanonTime.of(2L, 2L)));
    }
}
