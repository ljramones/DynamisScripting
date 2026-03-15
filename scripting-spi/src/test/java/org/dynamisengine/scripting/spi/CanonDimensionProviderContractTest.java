package org.dynamisengine.scripting.spi;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;
import org.dynamisengine.scripting.api.value.WorldPatch;
import org.junit.jupiter.api.Test;

public abstract class CanonDimensionProviderContractTest {
    protected abstract CanonDimensionProvider provider();

    @Test
    void dimensionIdReturnsNonNullNonEmpty() {
        String dimensionId = provider().dimensionId();
        assertNotNull(dimensionId);
        assertFalse(dimensionId.isBlank());
    }

    @Test
    void dimensionNameReturnsNonNullNonEmpty() {
        String dimensionName = provider().dimensionName();
        assertNotNull(dimensionName);
        assertFalse(dimensionName.isBlank());
    }

    @Test
    void canonicalObjectTypesReturnsNonNullList() {
        List<String> objectTypes = provider().canonicalObjectTypes();
        assertNotNull(objectTypes);
    }

    @Test
    void onWorldPatchAppliedDoesNotThrowForValidPatch() {
        WorldPatch patch = WorldPatch.of("1.0.0", List.of("ruleA"), List.of("scheduleA"), List.of("assetA"));
        assertDoesNotThrow(() -> provider().onWorldPatchApplied(patch));
    }
}
