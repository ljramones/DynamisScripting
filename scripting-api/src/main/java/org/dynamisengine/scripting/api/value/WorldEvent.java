package org.dynamisengine.scripting.api.value;

import java.util.Map;
import org.dynamisengine.core.exception.DynamisException;

public record WorldEvent(String nodeId, String archetype, Map<String, Object> parameters, int priority, CanonTime canonTime) {
    public WorldEvent {
        requireNonBlank(nodeId, "nodeId");
        requireNonNull(archetype, "archetype");
        requireNonNull(parameters, "parameters");
        parameters = Map.copyOf(parameters);
        requireNonNull(canonTime, "canonTime");
    }

    public static WorldEvent of(
            String nodeId,
            String archetype,
            Map<String, Object> parameters,
            int priority,
            CanonTime canonTime) {
        return new WorldEvent(nodeId, archetype, parameters, priority, canonTime);
    }

    private static <T> T requireNonNull(T value, String field) {
        if (value == null) {
            throw new DynamisException(field + " must not be null");
        }
        return value;
    }

    private static String requireNonBlank(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new DynamisException(field + " must not be null or blank");
        }
        return value;
    }
}
