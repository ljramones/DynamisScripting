package org.dynamisengine.scripting.spi;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public abstract class SocietyVectorDimensionContractTest {
    protected abstract SocietyVectorDimension dimension();

    @Test
    void dimensionIdReturnsNonNullNonEmpty() {
        String dimensionId = dimension().dimensionId();
        assertNotNull(dimensionId);
        assertFalse(dimensionId.isBlank());
    }

    @Test
    void minValueIsLessThanMaxValue() {
        assertTrue(dimension().minValue() < dimension().maxValue());
    }

    @Test
    void describeValueReturnsNonNullWithinRange() {
        double mid = (dimension().minValue() + dimension().maxValue()) / 2.0D;
        String description = dimension().describeValue(mid);
        assertNotNull(description);
    }

    @Test
    void interactionContributionStaysInRange() {
        double contribution = dimension().computeInteractionContribution(dimension().minValue(), dimension().maxValue());
        assertTrue(contribution >= -1.0D && contribution <= 1.0D);
    }

    @Test
    void identicalValuesYieldPositiveAlignment() {
        double mid = (dimension().minValue() + dimension().maxValue()) / 2.0D;
        double contribution = dimension().computeInteractionContribution(mid, mid);
        assertTrue(contribution > 0.0D);
    }
}
