package org.dynamisscripting.percept;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.dynamisscripting.api.value.Percept;

/**
 * Downsampling keeps all critical percepts (fidelity >= 0.75) first, then fills remaining slots by highest
 * fidelity among non-critical percepts.
 */
public final class PerceptDownsampler {
    public List<Percept> downsample(List<Percept> percepts, int maxToDeliver) {
        if (percepts == null) {
            throw new PerceptException("downsample", "percepts must not be null");
        }
        if (maxToDeliver < 0) {
            throw new PerceptException("downsample", "maxToDeliver must be >= 0");
        }
        if (percepts.size() <= maxToDeliver) {
            return List.copyOf(percepts);
        }

        List<Percept> critical = new ArrayList<>();
        List<Percept> nonCritical = new ArrayList<>();
        for (Percept percept : percepts) {
            if (percept.fidelity() >= 0.75D) {
                critical.add(percept);
            } else {
                nonCritical.add(percept);
            }
        }

        nonCritical.sort(Comparator.comparingDouble(Percept::fidelity).reversed());

        List<Percept> selected = new ArrayList<>();
        for (Percept percept : critical) {
            if (selected.size() >= maxToDeliver) {
                break;
            }
            selected.add(percept);
        }
        for (Percept percept : nonCritical) {
            if (selected.size() >= maxToDeliver) {
                break;
            }
            selected.add(percept);
        }
        return List.copyOf(selected);
    }

    public boolean isEventStorm(int pendingCount, int stormThreshold) {
        return pendingCount > stormThreshold;
    }
}
