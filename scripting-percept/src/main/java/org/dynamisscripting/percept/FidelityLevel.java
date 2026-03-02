package org.dynamisscripting.percept;

public enum FidelityLevel {
    FULL(1.0),
    HIGH(0.75),
    MEDIUM(0.5),
    LOW(0.25),
    TRACE(0.1);

    private final double value;

    FidelityLevel(double value) {
        this.value = value;
    }

    public double value() {
        return value;
    }

    public static FidelityLevel fromDouble(double fidelity) {
        FidelityLevel selected = TRACE;
        for (FidelityLevel level : values()) {
            if (fidelity >= level.value) {
                return level;
            }
            selected = level;
        }
        return selected;
    }
}
