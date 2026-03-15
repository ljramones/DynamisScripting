package org.dynamisengine.scripting.society;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class DimensionWeightTest {
    @Test
    void weightOutsideRangeThrows() {
        assertThrows(IllegalArgumentException.class, () -> DimensionWeight.of("honor", -0.1D, Map.of()));
        assertThrows(IllegalArgumentException.class, () -> DimensionWeight.of("honor", 1.1D, Map.of()));
    }

    @Test
    void parametersMapImmutableAfterConstruction() {
        Map<String, Double> params = new HashMap<>();
        params.put("publicShameMultiplier", 3.0D);
        DimensionWeight weight = DimensionWeight.of("honor", 0.8D, params);

        params.put("publicShameMultiplier", 9.0D);
        assertEquals(3.0D, weight.parameters().get("publicShameMultiplier"));
        assertThrows(UnsupportedOperationException.class, () -> weight.parameters().put("x", 1.0D));
    }
}
