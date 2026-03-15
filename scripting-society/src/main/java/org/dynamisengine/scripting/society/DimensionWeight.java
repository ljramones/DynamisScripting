package org.dynamisengine.scripting.society;

import java.util.Map;

public record DimensionWeight(String dimensionId, double weight, Map<String, Double> parameters) {
    public DimensionWeight {
        if (dimensionId == null || dimensionId.isBlank()) {
            throw new SocietyException("DimensionWeight", "dimensionId must not be null or blank");
        }
        if (weight < 0.0D || weight > 1.0D) {
            throw new IllegalArgumentException("weight must be in range [0.0, 1.0]");
        }
        if (parameters == null) {
            throw new SocietyException("DimensionWeight", "parameters must not be null");
        }
        parameters = Map.copyOf(parameters);
    }

    public static DimensionWeight of(String dimensionId, double weight, Map<String, Double> parameters) {
        return new DimensionWeight(dimensionId, weight, parameters);
    }
}
