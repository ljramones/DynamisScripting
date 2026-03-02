package org.dynamisscripting.society;

import java.util.Map;
import java.util.Optional;

public record SocietyProfile(
        String societyId,
        String societyName,
        Map<String, DimensionWeight> dimensions,
        Map<String, Double> goapWeights) {

    public SocietyProfile {
        if (societyId == null || societyId.isBlank()) {
            throw new SocietyException("SocietyProfile", "societyId must not be null or blank");
        }
        if (societyName == null || societyName.isBlank()) {
            throw new SocietyException("SocietyProfile", "societyName must not be null or blank");
        }
        if (dimensions == null) {
            throw new SocietyException("SocietyProfile", "dimensions must not be null");
        }
        if (goapWeights == null) {
            throw new SocietyException("SocietyProfile", "goapWeights must not be null");
        }
        dimensions = Map.copyOf(dimensions);
        goapWeights = Map.copyOf(goapWeights);
    }

    public static SocietyProfile of(
            String societyId,
            String societyName,
            Map<String, DimensionWeight> dimensions,
            Map<String, Double> goapWeights) {
        return new SocietyProfile(societyId, societyName, dimensions, goapWeights);
    }

    public double weightFor(String dimensionId) {
        if (dimensionId == null || dimensionId.isBlank()) {
            return 0.0D;
        }
        DimensionWeight dimensionWeight = dimensions.get(dimensionId);
        return dimensionWeight == null ? 0.0D : dimensionWeight.weight();
    }

    public Optional<DimensionWeight> dimension(String dimensionId) {
        if (dimensionId == null || dimensionId.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(dimensions.get(dimensionId));
    }

    public boolean hasDimension(String dimensionId) {
        if (dimensionId == null || dimensionId.isBlank()) {
            return false;
        }
        return dimensions.containsKey(dimensionId);
    }
}
