package org.dynamisengine.scripting.society;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class SocietyProfileTest {
    @Test
    void weightForReturnsZeroForUnknownDimension() {
        SocietyProfile profile = profile();
        assertEquals(0.0D, profile.weightFor("unknown"));
    }

    @Test
    void hasDimensionChecksPresence() {
        SocietyProfile profile = profile();
        assertTrue(profile.hasDimension("honor"));
        assertFalse(profile.hasDimension("authority"));
    }

    @Test
    void dimensionReturnsOptionalPresence() {
        SocietyProfile profile = profile();
        assertTrue(profile.dimension("honor").isPresent());
        assertTrue(profile.dimension("authority").isEmpty());
    }

    @Test
    void mapsAreImmutableAndRecordEqualityWorks() {
        Map<String, DimensionWeight> dimensions = new HashMap<>();
        dimensions.put("honor", DimensionWeight.of("honor", 0.9D, Map.of()));
        Map<String, Double> goap = new HashMap<>();
        goap.put("survival", 0.4D);

        SocietyProfile a = SocietyProfile.of("empire", "Empire", dimensions, goap);
        SocietyProfile b = SocietyProfile.of("empire", "Empire", dimensions, goap);

        assertEquals(a, b);
        assertThrowsUnsupported(a);

        dimensions.put("authority", DimensionWeight.of("authority", 0.6D, Map.of()));
        goap.put("honor", 0.95D);

        assertFalse(a.hasDimension("authority"));
        assertFalse(a.goapWeights().containsKey("honor"));
    }

    private static SocietyProfile profile() {
        return SocietyProfile.of(
                "klingon_empire",
                "Klingon Empire",
                Map.of("honor", DimensionWeight.of("honor", 0.95D, Map.of("publicShameMultiplier", 3.0D))),
                Map.of("survival", 0.4D, "honor", 0.95D));
    }

    private static void assertThrowsUnsupported(SocietyProfile profile) {
        org.junit.jupiter.api.Assertions.assertThrows(
                UnsupportedOperationException.class,
                () -> profile.dimensions().put("x", DimensionWeight.of("x", 0.1D, Map.of())));
        org.junit.jupiter.api.Assertions.assertThrows(
                UnsupportedOperationException.class,
                () -> profile.goapWeights().put("x", 0.1D));
    }
}
