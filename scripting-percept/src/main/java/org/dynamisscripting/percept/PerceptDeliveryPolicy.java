package org.dynamisscripting.percept;

import java.util.Set;
import org.dynamis.core.entity.EntityId;
import org.dynamis.core.exception.DynamisException;

public record PerceptDeliveryPolicy(
        EntityId agentId,
        Set<String> subscribedPerceptTypes,
        double minimumFidelity,
        boolean acceptDownsampled) {

    public PerceptDeliveryPolicy {
        requireNonNull(agentId, "agentId");
        requireNonNull(subscribedPerceptTypes, "subscribedPerceptTypes");
        subscribedPerceptTypes = Set.copyOf(subscribedPerceptTypes);
        if (minimumFidelity < 0.0D || minimumFidelity > 1.0D) {
            throw new DynamisException("minimumFidelity must be in range [0.0, 1.0]");
        }
    }

    public static PerceptDeliveryPolicy of(
            EntityId agentId,
            Set<String> subscribedPerceptTypes,
            double minimumFidelity,
            boolean acceptDownsampled) {
        return new PerceptDeliveryPolicy(agentId, subscribedPerceptTypes, minimumFidelity, acceptDownsampled);
    }

    public boolean acceptsType(String perceptType) {
        if (perceptType == null || perceptType.isBlank()) {
            return false;
        }
        return subscribedPerceptTypes.isEmpty() || subscribedPerceptTypes.contains(perceptType);
    }

    private static <T> T requireNonNull(T value, String field) {
        if (value == null) {
            throw new DynamisException(field + " must not be null");
        }
        return value;
    }
}
