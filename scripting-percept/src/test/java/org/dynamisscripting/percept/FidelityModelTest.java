package org.dynamisscripting.percept;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class FidelityModelTest {
    @Test
    void fullFidelityAtZeroDistanceWithNoOcclusionAndLos() {
        FidelityModel model = new FidelityModel();
        assertEquals(1.0D, model.computeFidelity(0, false, false, true));
    }

    @Test
    void distanceDegradationAtFiveHundredIsHalf() {
        FidelityModel model = new FidelityModel();
        assertEquals(0.5D, model.computeFidelity(500, false, false, true));
    }

    @Test
    void distanceAtMaxDistanceIsZero() {
        FidelityModel model = new FidelityModel();
        assertEquals(0.0D, model.computeFidelity(FidelityModel.MAX_DISTANCE, false, false, true));
    }

    @Test
    void occlusionMultiplierApplied() {
        FidelityModel model = new FidelityModel();
        assertEquals(0.5D, model.computeFidelity(0, true, false, true));
    }

    @Test
    void acousticOcclusionMultiplierApplied() {
        FidelityModel model = new FidelityModel();
        assertEquals(0.7D, model.computeFidelity(0, false, true, true));
    }

    @Test
    void noLineOfSightMultiplierApplied() {
        FidelityModel model = new FidelityModel();
        assertEquals(0.6D, model.computeFidelity(0, false, false, false));
    }

    @Test
    void allFactorsCombinedNeverBelowZero() {
        FidelityModel model = new FidelityModel();
        assertTrue(model.computeFidelity(10_000, true, true, false) >= 0.0D);
    }

    @Test
    void pureFunctionSameInputSameOutput() {
        FidelityModel model = new FidelityModel();
        double first = model.computeFidelity(123, true, false, false);
        double second = model.computeFidelity(123, true, false, false);
        assertEquals(first, second);
    }
}
