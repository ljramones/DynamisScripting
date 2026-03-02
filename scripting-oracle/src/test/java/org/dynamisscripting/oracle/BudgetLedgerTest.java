package org.dynamisscripting.oracle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.Test;

class BudgetLedgerTest {
    static String testBudgetId(String name) {
        return "budget:" + name;
    }

    @Test
    void registerAndConsumeDecrementsRemaining() {
        BudgetLedger ledger = new BudgetLedger();
        ledger.registerBudget(testBudgetId("accusation"), 5L);

        boolean consumed = ledger.consume(testBudgetId("accusation"), 2L);

        assertTrue(consumed);
        assertEquals(3L, ledger.remaining(testBudgetId("accusation")));
    }

    @Test
    void consumeMoreThanAvailableReturnsFalseWithoutChangingRemaining() {
        BudgetLedger ledger = new BudgetLedger();
        ledger.registerBudget(testBudgetId("rumor"), 3L);

        boolean consumed = ledger.consume(testBudgetId("rumor"), 4L);

        assertFalse(consumed);
        assertEquals(3L, ledger.remaining(testBudgetId("rumor")));
    }

    @Test
    void unknownBudgetConsumeThrows() {
        BudgetLedger ledger = new BudgetLedger();
        assertThrows(OracleException.class, () -> ledger.consume(testBudgetId("unknown"), 1L));
    }

    @Test
    void unknownBudgetRemainingThrows() {
        BudgetLedger ledger = new BudgetLedger();
        assertThrows(OracleException.class, () -> ledger.remaining(testBudgetId("unknown")));
    }

    @Test
    void duplicateRegistrationThrows() {
        BudgetLedger ledger = new BudgetLedger();
        ledger.registerBudget(testBudgetId("x"), 1L);
        assertThrows(OracleException.class, () -> ledger.registerBudget(testBudgetId("x"), 2L));
    }

    @Test
    void resetRestoresCapacity() {
        BudgetLedger ledger = new BudgetLedger();
        ledger.registerBudget(testBudgetId("x"), 5L);
        ledger.consume(testBudgetId("x"), 3L);

        ledger.reset(testBudgetId("x"), 10L);

        assertEquals(10L, ledger.remaining(testBudgetId("x")));
    }

    @Test
    void resetAllRestoresAllBudgets() {
        BudgetLedger ledger = new BudgetLedger();
        ledger.registerBudget(testBudgetId("a"), 2L);
        ledger.registerBudget(testBudgetId("b"), 3L);
        ledger.consume(testBudgetId("a"), 1L);
        ledger.consume(testBudgetId("b"), 1L);

        ledger.resetAll(7L);

        assertEquals(7L, ledger.remaining(testBudgetId("a")));
        assertEquals(7L, ledger.remaining(testBudgetId("b")));
    }

    @Test
    void concurrentConsumeSucceedsExactlyTenTimes() throws Exception {
        BudgetLedger ledger = new BudgetLedger();
        String budgetId = testBudgetId("concurrent");
        ledger.registerBudget(budgetId, 10L);

        var executor = Executors.newFixedThreadPool(10);
        List<Callable<Boolean>> tasks = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            tasks.add(() -> ledger.consume(budgetId, 1L));
        }

        List<Future<Boolean>> futures = executor.invokeAll(tasks);
        executor.shutdown();

        int successCount = 0;
        for (Future<Boolean> future : futures) {
            if (future.get()) {
                successCount++;
            }
        }

        assertEquals(10, successCount);
        assertEquals(0L, ledger.remaining(budgetId));
    }
}
