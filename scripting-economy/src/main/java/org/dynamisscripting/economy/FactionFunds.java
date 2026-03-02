package org.dynamisscripting.economy;

import org.dynamis.core.entity.EntityId;

public record FactionFunds(EntityId factionId, double balance, double reservedBalance) {
    public FactionFunds {
        if (factionId == null) {
            throw new EconomyException("FactionFunds", "factionId must not be null");
        }
        if (balance < 0.0D) {
            throw new EconomyException("FactionFunds", "balance must be >= 0");
        }
        if (reservedBalance < 0.0D) {
            throw new EconomyException("FactionFunds", "reservedBalance must be >= 0");
        }
        if (reservedBalance > balance) {
            throw new EconomyException("FactionFunds", "reservedBalance must be <= balance");
        }
    }

    public static FactionFunds of(EntityId factionId, double balance, double reservedBalance) {
        return new FactionFunds(factionId, balance, reservedBalance);
    }

    public double availableBalance() {
        return balance - reservedBalance;
    }

    public boolean canAfford(double amount) {
        if (amount < 0.0D) {
            throw new EconomyException("canAfford", "amount must be >= 0");
        }
        return availableBalance() >= amount;
    }

    public FactionFunds debit(double amount) {
        if (amount <= 0.0D) {
            throw new EconomyException("debit", "amount must be > 0");
        }
        if (amount > balance) {
            throw new EconomyException("debit", "insufficient funds");
        }
        return new FactionFunds(factionId, balance - amount, reservedBalance);
    }

    public FactionFunds credit(double amount) {
        if (amount <= 0.0D) {
            throw new EconomyException("credit", "amount must be > 0");
        }
        return new FactionFunds(factionId, balance + amount, reservedBalance);
    }

    public FactionFunds reserve(double amount) {
        if (amount <= 0.0D) {
            throw new EconomyException("reserve", "amount must be > 0");
        }
        if (amount > availableBalance()) {
            throw new EconomyException("reserve", "insufficient available balance");
        }
        return new FactionFunds(factionId, balance, reservedBalance + amount);
    }
}
