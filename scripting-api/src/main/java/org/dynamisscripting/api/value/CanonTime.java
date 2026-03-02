package org.dynamisscripting.api.value;

import org.dynamis.core.exception.DynamisException;

public record CanonTime(long tick, long simulationNanos) implements Comparable<CanonTime> {
    public static final CanonTime ZERO = CanonTime.of(0L, 0L);

    public CanonTime {
        if (tick < 0L) {
            throw new DynamisException("tick must be non-negative: " + tick);
        }
        if (simulationNanos < 0L) {
            throw new DynamisException("simulationNanos must be non-negative: " + simulationNanos);
        }
    }

    public static CanonTime of(long tick, long simulationNanos) {
        return new CanonTime(tick, simulationNanos);
    }

    public boolean isAfter(CanonTime other) {
        return compareTo(requireNonNull(other, "other")) > 0;
    }

    public boolean isBefore(CanonTime other) {
        return compareTo(requireNonNull(other, "other")) < 0;
    }

    @Override
    public int compareTo(CanonTime other) {
        CanonTime safeOther = requireNonNull(other, "other");
        int tickComparison = Long.compare(tick, safeOther.tick);
        if (tickComparison != 0) {
            return tickComparison;
        }
        return Long.compare(simulationNanos, safeOther.simulationNanos);
    }

    private static <T> T requireNonNull(T value, String field) {
        if (value == null) {
            throw new DynamisException(field + " must not be null");
        }
        return value;
    }
}
