package org.dynamisscripting.economy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.dynamis.core.entity.EntityId;
import org.junit.jupiter.api.Test;

class FactionFundsTest {
    @Test
    void canAffordBasedOnAvailableBalance() {
        FactionFunds funds = FactionFunds.of(EntityId.of(1L), 1000.0D, 200.0D);

        assertTrue(funds.canAfford(800.0D));
        assertTrue(!funds.canAfford(801.0D));
    }

    @Test
    void debitReturnsNewInstanceAndPreservesOriginal() {
        FactionFunds original = FactionFunds.of(EntityId.of(1L), 500.0D, 50.0D);
        FactionFunds debited = original.debit(100.0D);

        assertNotSame(original, debited);
        assertEquals(500.0D, original.balance());
        assertEquals(400.0D, debited.balance());
    }

    @Test
    void debitInsufficientFundsThrows() {
        FactionFunds funds = FactionFunds.of(EntityId.of(1L), 50.0D, 0.0D);
        assertThrows(EconomyException.class, () -> funds.debit(60.0D));
    }

    @Test
    void creditIncreasesBalance() {
        FactionFunds funds = FactionFunds.of(EntityId.of(1L), 100.0D, 0.0D);
        assertEquals(150.0D, funds.credit(50.0D).balance());
    }

    @Test
    void reserveAdjustsAvailableAndRejectsOverReserve() {
        FactionFunds funds = FactionFunds.of(EntityId.of(1L), 100.0D, 20.0D);
        FactionFunds reserved = funds.reserve(30.0D);

        assertEquals(50.0D, reserved.availableBalance());
        assertThrows(EconomyException.class, () -> funds.reserve(81.0D));
    }

    @Test
    void availableBalanceReflectsReserve() {
        FactionFunds funds = FactionFunds.of(EntityId.of(1L), 250.0D, 25.0D);
        assertEquals(225.0D, funds.availableBalance());
    }
}
