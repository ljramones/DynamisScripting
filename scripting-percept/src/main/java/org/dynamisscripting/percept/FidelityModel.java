package org.dynamisscripting.percept;

public final class FidelityModel {
    // Configurable constant; hardcoded for now until runtime configuration wiring is added.
    public static final double MAX_DISTANCE = 1000.0D;

    public double computeFidelity(
            double distanceUnits,
            boolean occluded,
            boolean acousticOcclusion,
            boolean lineOfSight) {
        double fidelity = 1.0D;
        fidelity *= Math.max(0.0D, 1.0D - (distanceUnits / MAX_DISTANCE));
        if (occluded) {
            fidelity *= 0.5D;
        }
        if (acousticOcclusion) {
            fidelity *= 0.7D;
        }
        if (!lineOfSight) {
            fidelity *= 0.6D;
        }
        return Math.max(0.0D, fidelity);
    }
}
