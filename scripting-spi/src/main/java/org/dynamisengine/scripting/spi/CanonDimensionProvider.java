package org.dynamisengine.scripting.spi;

import java.util.List;
import org.dynamisengine.scripting.api.value.WorldPatch;

public interface CanonDimensionProvider {
    String dimensionId();

    String dimensionName();

    List<String> canonicalObjectTypes();

    void onWorldPatchApplied(WorldPatch patch);
}
