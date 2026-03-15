package org.dynamisengine.scripting.spi;

public interface SocietyVectorDimension {
    String dimensionId();

    String dimensionName();

    double minValue();

    double maxValue();

    String describeValue(double value);

    double computeInteractionContribution(double valueA, double valueB);
}
