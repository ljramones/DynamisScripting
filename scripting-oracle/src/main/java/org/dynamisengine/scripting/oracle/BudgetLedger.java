package org.dynamisengine.scripting.oracle;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public final class BudgetLedger {
    private final ConcurrentHashMap<String, AtomicLong> budgets;

    public BudgetLedger() {
        this.budgets = new ConcurrentHashMap<>();
    }

    public void registerBudget(String budgetId, long capacity) {
        validateBudgetId(budgetId);
        validateCapacity(capacity);

        AtomicLong previous = budgets.putIfAbsent(budgetId, new AtomicLong(capacity));
        if (previous != null) {
            throw new OracleException("registerBudget", "budgetId already registered: " + budgetId);
        }
    }

    public boolean consume(String budgetId, long amount) {
        validateBudgetId(budgetId);
        if (amount <= 0L) {
            throw new OracleException("consume", "amount must be > 0");
        }

        AtomicLong remaining = budgets.get(budgetId);
        if (remaining == null) {
            throw new OracleException("consume", "unknown budgetId: " + budgetId);
        }

        while (true) {
            long current = remaining.get();
            if (current < amount) {
                return false;
            }
            if (remaining.compareAndSet(current, current - amount)) {
                return true;
            }
        }
    }

    public long remaining(String budgetId) {
        validateBudgetId(budgetId);
        AtomicLong remaining = budgets.get(budgetId);
        if (remaining == null) {
            throw new OracleException("remaining", "unknown budgetId: " + budgetId);
        }
        return remaining.get();
    }

    public void reset(String budgetId, long capacity) {
        validateBudgetId(budgetId);
        validateCapacity(capacity);
        AtomicLong budget = budgets.get(budgetId);
        if (budget == null) {
            throw new OracleException("reset", "unknown budgetId: " + budgetId);
        }
        budget.set(capacity);
    }

    public void resetAll(long capacity) {
        validateCapacity(capacity);
        for (AtomicLong budget : budgets.values()) {
            budget.set(capacity);
        }
    }

    public Set<String> registeredBudgets() {
        return Set.copyOf(budgets.keySet());
    }

    private static void validateBudgetId(String budgetId) {
        if (budgetId == null || budgetId.isBlank()) {
            throw new OracleException("budget", "budgetId must not be null or empty");
        }
    }

    private static void validateCapacity(long capacity) {
        if (capacity < 0L) {
            throw new OracleException("budget", "capacity must be >= 0");
        }
    }
}
