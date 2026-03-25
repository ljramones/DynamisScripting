package org.dynamisengine.scripting.runtime;

import java.util.concurrent.atomic.AtomicInteger;
import org.dynamisengine.scripting.api.Chronicler;
import org.dynamisengine.scripting.api.value.CanonTime;
import org.dynamisengine.scripting.canon.CanonTimekeeper;
import org.dynamisengine.scripting.chronicler.DefaultChronicler;
import org.dynamisengine.scripting.dsl.DslCompiler;
import org.dynamisengine.scripting.oracle.DefaultWorldOracle;
import org.dynamisengine.scripting.percept.DefaultPerceptBus;

public final class RuntimeTick {
    private final CanonTimekeeper timekeeper;
    private final Chronicler chronicler;
    private final DefaultWorldOracle oracle;
    private final DegradationMonitor degradationMonitor;
    private final RuntimeConfiguration config;
    private final DslCompiler dslCompiler;

    public RuntimeTick(
            CanonTimekeeper timekeeper,
            Chronicler chronicler,
            DefaultWorldOracle oracle,
            DefaultPerceptBus perceptBus,
            DegradationMonitor degradationMonitor,
            RuntimeConfiguration config) {
        this(timekeeper, chronicler, oracle, perceptBus, degradationMonitor, config, null);
    }

    public RuntimeTick(
            CanonTimekeeper timekeeper,
            Chronicler chronicler,
            DefaultWorldOracle oracle,
            DefaultPerceptBus perceptBus,
            DegradationMonitor degradationMonitor,
            RuntimeConfiguration config,
            DslCompiler dslCompiler) {
        this.timekeeper = requireNonNull(timekeeper, "timekeeper");
        this.chronicler = requireNonNull(chronicler, "chronicler");
        this.oracle = requireNonNull(oracle, "oracle");
        requireNonNull(perceptBus, "perceptBus");
        this.degradationMonitor = requireNonNull(degradationMonitor, "degradationMonitor");
        this.config = requireNonNull(config, "config");
        this.dslCompiler = dslCompiler;
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

        long chroniclerStart = System.nanoTime();
        chronicler.tick(currentTime);
        long chroniclerNanos = System.nanoTime() - chroniclerStart;

        degradationMonitor.allTiers(currentTime.tick());

        long durationNanos = System.nanoTime() - startNanos;

        // Collect telemetry from instrumented components
        long errors = (chronicler instanceof DefaultChronicler dc) ? dc.evaluationErrors() : 0;
        long cacheHits = dslCompiler != null ? dslCompiler.cacheHits() : 0;
        long cacheMisses = dslCompiler != null ? dslCompiler.cacheMisses() : 0;
        int cacheSize = dslCompiler != null ? dslCompiler.cacheSize() : 0;

        return new RuntimeTickResult(currentTime, proposedCount.get(), committedCount.get(),
                durationNanos, chroniclerNanos, errors, cacheHits, cacheMisses, cacheSize);
    }

    private static <T> T requireNonNull(T value, String field) {
        if (value == null) {
            throw new RuntimeException("RuntimeTick", field + " must not be null");
        }
        return value;
    }
}
