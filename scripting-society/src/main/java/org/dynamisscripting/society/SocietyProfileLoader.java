package org.dynamisscripting.society;

import java.util.Map;

public final class SocietyProfileLoader {
    public SocietyProfile loadFromYaml(String yamlContent) {
        throw new SocietyException(
                "loadFromYaml",
                "YAML loading not yet implemented — use SocietyProfileLoader.build()");
    }

    public static SocietyProfile build(
            String societyId,
            String societyName,
            Map<String, DimensionWeight> dimensions,
            Map<String, Double> goapWeights) {
        return SocietyProfile.of(societyId, societyName, dimensions, goapWeights);
    }
}
