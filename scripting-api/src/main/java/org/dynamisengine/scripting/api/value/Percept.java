package org.dynamisengine.scripting.api.value;

import org.dynamisengine.core.entity.EntityId;
import org.dynamisengine.core.exception.DynamisException;

public record Percept(EntityId agentId, String perceptType, Object payload, double fidelity, long sourceCommitId) {
    public Percept {
        requireNonNull(agentId, "agentId");
        requireNonBlank(perceptType, "perceptType");
        requireNonNull(payload, "payload");
        if (fidelity < 0.0D || fidelity > 1.0D) {
            throw new DynamisException("fidelity must be in range [0.0, 1.0], got: " + fidelity);
        }
        if (sourceCommitId <= 0L) {
            throw new DynamisException("sourceCommitId must be greater than 0: " + sourceCommitId);
        }
    }

    public static Percept of(
            EntityId agentId,
            String perceptType,
            Object payload,
            double fidelity,
            long sourceCommitId) {
        return new Percept(agentId, perceptType, payload, fidelity, sourceCommitId);
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
