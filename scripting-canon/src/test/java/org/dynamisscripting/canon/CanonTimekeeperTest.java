package org.dynamisscripting.canon;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.dynamisscripting.api.value.CanonTime;
import org.junit.jupiter.api.Test;

class CanonTimekeeperTest {
    @Test
    void currentReturnsZeroInitially() {
        CanonTimekeeper timekeeper = new CanonTimekeeper();
        assertEquals(CanonTime.ZERO, timekeeper.current());
    }

    @Test
    void advanceIncrementsTickAndAddsNanos() {
        CanonTimekeeper timekeeper = new CanonTimekeeper();

        CanonTime next = timekeeper.advance(50L);

        assertEquals(1L, next.tick());
        assertEquals(50L, next.simulationNanos());
    }

    @Test
    void advanceZeroThrows() {
        CanonTimekeeper timekeeper = new CanonTimekeeper();
        assertThrows(IllegalArgumentException.class, () -> timekeeper.advance(0L));
    }

    @Test
    void advanceNegativeThrows() {
        CanonTimekeeper timekeeper = new CanonTimekeeper();
        assertThrows(IllegalArgumentException.class, () -> timekeeper.advance(-1L));
    }

    @Test
    void advanceToTickSetsTickWithoutChangingNanos() {
        CanonTimekeeper timekeeper = new CanonTimekeeper();
        timekeeper.advance(50L);

        CanonTime advanced = timekeeper.advanceToTick(10L);

        assertEquals(10L, advanced.tick());
        assertEquals(50L, advanced.simulationNanos());
    }

    @Test
    void advanceToCurrentOrPastTickThrows() {
        CanonTimekeeper timekeeper = new CanonTimekeeper();
        timekeeper.advance(10L);

        assertThrows(IllegalArgumentException.class, () -> timekeeper.advanceToTick(1L));
        assertThrows(IllegalArgumentException.class, () -> timekeeper.advanceToTick(0L));
    }

    @Test
    void resetReturnsToZero() {
        CanonTimekeeper timekeeper = new CanonTimekeeper();
        timekeeper.advance(10L);

        timekeeper.reset();

        assertEquals(CanonTime.ZERO, timekeeper.current());
    }

    @Test
    void canonTimekeeperDoesNotUseSystemWallTimeCalls() throws Exception {
        String content = Files.readString(Path.of("src/main/java/org/dynamisscripting/canon/CanonTimekeeper.java"));
        assertTrue(!content.contains("System.currentTimeMillis"));
        assertTrue(!content.contains("System.nanoTime"));
    }
}
