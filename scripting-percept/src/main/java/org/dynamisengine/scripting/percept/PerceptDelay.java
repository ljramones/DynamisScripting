package org.dynamisengine.scripting.percept;

import org.dynamisengine.core.logging.DynamisLogger;

public final class PerceptDelay {
    private static final DynamisLogger LOGGER = DynamisLogger.get(PerceptDelay.class);

    public long computeDelayTicks(double distanceUnits, String medium) {
        if (medium == null) {
            LOGGER.warn("Unknown percept medium 'null'; returning zero delay");
            return 0L;
        }

        return switch (medium) {
            case "sound" -> (long) (distanceUnits / 340.0D);
            case "light" -> 0L;
            case "courier" -> (long) (distanceUnits / 10.0D);
            default -> {
                LOGGER.warn("Unknown percept medium '" + medium + "'; returning zero delay");
                yield 0L;
            }
        };
    }
}
