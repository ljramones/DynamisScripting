package org.dynamisengine.scripting.society;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import org.dynamisengine.scripting.spi.SocietyVectorDimension;
import org.junit.jupiter.api.Test;

class InteractionFunctionTest {
    @Test
    void twoHighHonorSocietiesProduceAlignment() {
        InteractionFunction function = new InteractionFunction(List.of(honorDimension()));

        InteractionOutcome outcome = function.compute(profile("a", 1.0D), profile("b", 1.0D));

        assertEquals(InteractionMode.ALIGNMENT, outcome.mode());
        assertStrengthRange(outcome.strength());
        assertDominantDimension(outcome.dominantDimensionId());
    }

    @Test
    void highHonorVsZeroHonorProducesOrthogonality() {
        InteractionFunction function = new InteractionFunction(List.of(honorDimension()));

        InteractionOutcome outcome = function.compute(profile("a", 1.0D), profile("b", 0.0D));

        assertEquals(InteractionMode.ORTHOGONALITY, outcome.mode());
        assertStrengthRange(outcome.strength());
        assertDominantDimension(outcome.dominantDimensionId());
    }

    @Test
    void lowHonorProfilesProduceOppositionContribution() {
        InteractionFunction function = new InteractionFunction(List.of(honorDimension()));

        InteractionOutcome outcome = function.compute(profile("a", 0.0D), profile("b", 0.0D));

        assertEquals(InteractionMode.OPPOSITION, outcome.mode());
        assertStrengthRange(outcome.strength());
        assertDominantDimension(outcome.dominantDimensionId());
    }

    @Test
    void sameInputProducesSameOutcome() {
        InteractionFunction function = new InteractionFunction(List.of(honorDimension()));
        SocietyProfile a = profile("a", 0.8D);
        SocietyProfile b = profile("b", 0.2D);

        InteractionOutcome first = function.compute(a, b);
        InteractionOutcome second = function.compute(a, b);

        assertEquals(first, second);
    }

    @Test
    void emptyProfileProducesOrthogonalityWithZeroStrength() {
        InteractionFunction function = new InteractionFunction(List.of(honorDimension()));
        SocietyProfile a = SocietyProfile.of("a", "A", Map.of(), Map.of());
        SocietyProfile b = SocietyProfile.of("b", "B", Map.of(), Map.of());

        InteractionOutcome outcome = function.compute(a, b);

        assertEquals(InteractionMode.ORTHOGONALITY, outcome.mode());
        assertEquals(0.0D, outcome.strength());
        assertDominantDimension(outcome.dominantDimensionId());
    }

    private static SocietyProfile profile(String id, double honorWeight) {
        return SocietyProfile.of(
                id,
                id,
                Map.of("honor", DimensionWeight.of("honor", honorWeight, Map.of())),
                Map.of("survival", 0.5D));
    }

    static SocietyVectorDimension honorDimension() {
        return new SocietyVectorDimension() {
            @Override
            public String dimensionId() {
                return "honor";
            }

            @Override
            public String dimensionName() {
                return "Honor-Shame";
            }

            @Override
            public double minValue() {
                return 0.0D;
            }

            @Override
            public double maxValue() {
                return 1.0D;
            }

            @Override
            public String describeValue(double value) {
                return "honor=" + value;
            }

            @Override
            public double computeInteractionContribution(double a, double b) {
                return (a + b) / 2.0D - 0.5D;
            }
        };
    }

    private static void assertStrengthRange(double strength) {
        assertTrue(strength >= 0.0D);
        assertTrue(strength <= 1.0D);
    }

    private static void assertDominantDimension(String dimensionId) {
        assertNotNull(dimensionId);
        assertFalse(dimensionId.isBlank());
    }
}
