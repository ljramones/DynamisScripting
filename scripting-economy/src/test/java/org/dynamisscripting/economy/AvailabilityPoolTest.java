package org.dynamisscripting.economy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.Test;

class AvailabilityPoolTest {
    @Test
    void registerAndConsumeDecrementsAvailability() {
        AvailabilityPool pool = new AvailabilityPool();
        pool.register("agent", 10L);

        assertTrue(pool.consume("agent", 3L));
        assertEquals(7L, pool.available("agent"));
    }

    @Test
    void consumeMoreThanAvailableReturnsFalse() {
        AvailabilityPool pool = new AvailabilityPool();
        pool.register("agent", 2L);

        assertTrue(!pool.consume("agent", 3L));
        assertEquals(2L, pool.available("agent"));
    }

    @Test
    void unknownResourceTypeThrows() {
        AvailabilityPool pool = new AvailabilityPool();
        assertThrows(EconomyException.class, () -> pool.consume("unknown", 1L));
        assertThrows(EconomyException.class, () -> pool.available("unknown"));
    }

    @Test
    void restoreIncrementsPool() {
        AvailabilityPool pool = new AvailabilityPool();
        pool.register("agent", 1L);
        pool.restore("agent", 4L);

        assertEquals(5L, pool.available("agent"));
    }

    @Test
    void concurrentConsumeSucceedsExactlyTenTimes() throws InterruptedException, ExecutionException {
        AvailabilityPool pool = new AvailabilityPool();
        pool.register("agent", 10L);

        ExecutorService executor = Executors.newFixedThreadPool(10);
        try {
            List<Callable<Boolean>> tasks = new ArrayList<>();
            for (int i = 0; i < 10; i++) {
                tasks.add(() -> pool.consume("agent", 1L));
            }
            List<Future<Boolean>> futures = executor.invokeAll(tasks);
            int successes = 0;
            for (Future<Boolean> future : futures) {
                if (future.get()) {
                    successes++;
                }
            }

            assertEquals(10, successes);
            assertEquals(0L, pool.available("agent"));
        } finally {
            executor.shutdownNow();
        }
    }
}
