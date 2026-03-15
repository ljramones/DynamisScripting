package org.dynamisengine.scripting.api.value;

import java.util.List;
import org.dynamisengine.core.entity.EntityId;
import org.dynamisengine.core.exception.DynamisException;

public record Intent(
        EntityId agentId,
        String intentType,
        List<EntityId> targets,
        String rationale,
        double confidence,
        CanonTime canonTimeSnapshot,
        RequestedScope requestedScope) {

    public Intent {
        requireNonNull(agentId, "agentId");
        requireNonBlank(intentType, "intentType");
        requireNonNull(targets, "targets");
        targets = List.copyOf(targets);
        requireNonNull(rationale, "rationale");
        if (confidence < 0.0D || confidence > 1.0D) {
            throw new DynamisException("confidence must be in range [0.0, 1.0], got: " + confidence);
        }
        requireNonNull(canonTimeSnapshot, "canonTimeSnapshot");
        requireNonNull(requestedScope, "requestedScope");
    }

    public static Intent of(
            EntityId agentId,
            String intentType,
            List<EntityId> targets,
            String rationale,
            double confidence,
            CanonTime canonTimeSnapshot,
            RequestedScope requestedScope) {
        return new Intent(
                agentId,
                intentType,
                targets,
                rationale,
                confidence,
                canonTimeSnapshot,
                requestedScope);
    }

    public enum RequestedScope {
        PUBLIC,
        PRIVATE,
        STEALTH
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
