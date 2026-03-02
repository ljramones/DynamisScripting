package org.dynamisscripting.spi;

import java.util.List;
import org.dynamisscripting.api.value.WorldPatch;

public interface CanonDimensionProvider {
    String dimensionId();

    String dimensionName();

    List<String> canonicalObjectTypes();

    void onWorldPatchApplied(WorldPatch patch);
}
