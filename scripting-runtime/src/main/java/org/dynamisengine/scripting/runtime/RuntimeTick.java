package org.dynamisengine.scripting.runtime;

import java.util.concurrent.atomic.AtomicInteger;
import org.dynamisengine.scripting.api.Chronicler;
import org.dynamisengine.scripting.api.value.CanonTime;
import org.dynamisengine.scripting.canon.CanonTimekeeper;
import org.dynamisengine.scripting.oracle.DefaultWorldOracle;
import org.dynamisengine.scripting.percept.DefaultPerceptBus;

public final class RuntimeTick {
    private final CanonTimekeeper timekeeper;
    private final Chronicler chronicler;
    private final DefaultWorldOracle oracle;
    private final DegradationMonitor degradationMonitor;
    private final RuntimeConfiguration config;

    public RuntimeTick(
            CanonTimekeeper timekeeper,
            Chronicler chronicler,
            DefaultWorldOracle oracle,
            DefaultPerceptBus perceptBus,
            DegradationMonitor degradationMonitor,
            RuntimeConfiguration config) {
        this.timekeeper = requireNonNull(timekeeper, "timekeeper");
        this.chronicler = requireNonNull(chronicler, "chronicler");
        this.oracle = requireNonNull(oracle, "oracle");
        requireNonNull(perceptBus, "perceptBus");
        this.degradationMonitor = requireNonNull(degradationMonitor, "degradationMonitor");
        this.config = requireNonNull(config, "config");
    }

    public RuntimeTickResult execute() {
        // Canonical tick sequence (ADR-001):
        // 1. Advance CanonTime
        // 2. Chronicler evaluates → proposes WorldEvents to Oracle
        // 3. Oracle commits WorldEvents to CanonLog
        // 4. DegradationMonitor updated
        // PerceptBus delivery is demand-driven, not tick-driven

        // Telemetry only: wall-clock duration for diagnostics, never simulation logic.
        long startNanos = System.nanoTime();

        CanonTime currentTime = timekeeper.advance(config.tickRateNanos());
        AtomicInteger proposedCount = new AtomicInteger(0);
        AtomicInteger committedCount = new AtomicInteger(0);

        chronicler.registerWorldEventListener(worldEvent -> {
            proposedCount.incrementAndGet();
            if (oracle.commitWorldEvent(worldEvent).committed()) {
                committedCount.incrementAndGet();
            }
        });

        chronicler.tick(currentTime);
        degradationMonitor.allTiers(currentTime.tick());

        long durationNanos = System.nanoTime() - startNanos;
        return new RuntimeTickResult(currentTime, proposedCount.get(), committedCount.get(), durationNanos);
    }

    private static <T> T requireNonNull(T value, String field) {
        if (value == null) {
            throw new RuntimeException("RuntimeTick", field + " must not be null");
        }
        return value;
    }
}
