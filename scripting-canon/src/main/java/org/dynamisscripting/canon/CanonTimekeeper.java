package org.dynamisscripting.canon;

import java.util.concurrent.atomic.AtomicLong;
import org.dynamisscripting.api.value.CanonTime;

public final class CanonTimekeeper {
    private final AtomicLong currentTick;
    private final AtomicLong currentSimulationNanos;

    public CanonTimekeeper() {
        this.currentTick = new AtomicLong(0L);
        this.currentSimulationNanos = new AtomicLong(0L);
    }

    public CanonTime current() {
        return CanonTime.of(currentTick.get(), currentSimulationNanos.get());
    }

    public CanonTime advance(long deltaNanos) {
        if (deltaNanos <= 0L) {
            throw new IllegalArgumentException("deltaNanos must be > 0");
        }
        long tick = currentTick.incrementAndGet();
        long nanos = currentSimulationNanos.addAndGet(deltaNanos);
        return CanonTime.of(tick, nanos);
    }

    public CanonTime advanceToTick(long targetTick) {
        long currentTickValue = currentTick.get();
        if (targetTick <= currentTickValue) {
            throw new IllegalArgumentException(
                    "targetTick must be greater than current tick " + currentTickValue);
        }
        currentTick.set(targetTick);
        return CanonTime.of(targetTick, currentSimulationNanos.get());
    }

    /**
     * Test-only reset utility.
     */
    public void reset() {
        currentTick.set(0L);
        currentSimulationNanos.set(0L);
    }
}
