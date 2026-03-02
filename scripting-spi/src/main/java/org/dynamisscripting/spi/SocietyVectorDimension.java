package org.dynamisscripting.spi;

public interface SocietyVectorDimension {
    String dimensionId();

    String dimensionName();

    double minValue();

    double maxValue();

    String describeValue(double value);

    double computeInteractionContribution(double valueA, double valueB);
}
