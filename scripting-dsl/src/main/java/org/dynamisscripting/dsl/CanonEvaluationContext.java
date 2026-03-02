package org.dynamisscripting.dsl;

import org.dynamis.core.logging.DynamisLogger;
import org.dynamisscripting.api.CanonLog;
import org.dynamisscripting.api.value.CanonTime;

public final class CanonEvaluationContext {
    private static final DynamisLogger LOGGER = DynamisLogger.get(CanonEvaluationContext.class);

    private final CanonLog canonLog;
    private final CanonTime currentTime;

    public CanonEvaluationContext(CanonLog canonLog, CanonTime currentTime) {
        this.canonLog = requireNonNull(canonLog, "canonLog");
        this.currentTime = requireNonNull(currentTime, "currentTime");
    }

    public Object resolve(String variableName) {
        String safeName = requireNonBlank(variableName, "variableName");
        return switch (safeName) {
            case "canonTime" -> currentTime.tick();
            case "simulationNanos" -> currentTime.simulationNanos();
            default -> {
                // Placeholder until runtime wires canonical dimension providers into variable resolution.
                LOGGER.warn("Unknown canonical variable '" + safeName + "'; returning stub value 0");
                yield 0L;
            }
        };
    }

    private static <T> T requireNonNull(T value, String field) {
        if (value == null) {
            throw new DslEvaluationException("", field, "must not be null");
        }
        return value;
    }

    private static String requireNonBlank(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new DslEvaluationException("", field, "must not be null or blank");
        }
        return value;
    }
}
