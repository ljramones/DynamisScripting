package org.dynamisscripting.society;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.dynamisscripting.api.CanonLog;
import org.dynamisscripting.api.value.CanonEvent;
import org.dynamisscripting.api.value.CanonTime;

public final class CulturalDriftTracker {
    public static final double MAX_DRIFT_PER_TICK = 0.01D;

    private final SocietyRegistry registry;
    private final CanonLog canonLog;

    @SuppressFBWarnings(
            value = {"EI_EXPOSE_REP2"},
            justification = "Tracker intentionally coordinates shared registry and CanonLog instances")
    public CulturalDriftTracker(SocietyRegistry registry, CanonLog canonLog) {
        if (registry == null || canonLog == null) {
            throw new SocietyException("constructor", "registry and canonLog must not be null");
        }
        this.registry = registry;
        this.canonLog = canonLog;
    }

    /**
     * The only method in this module that appends to CanonLog.
     */
    public void applyDrift(String societyId, String dimensionId, double delta, CanonTime currentTime) {
        if (societyId == null || societyId.isBlank()) {
            throw new SocietyException("applyDrift", "societyId must not be null or blank");
        }
        if (dimensionId == null || dimensionId.isBlank()) {
            throw new SocietyException("applyDrift", "dimensionId must not be null or blank");
        }
        if (currentTime == null) {
            throw new SocietyException("applyDrift", "currentTime must not be null");
        }
        if (Math.abs(delta) > MAX_DRIFT_PER_TICK) {
            throw new SocietyException(
                    "applyDrift",
                    "delta exceeds MAX_DRIFT_PER_TICK: " + delta + " > " + MAX_DRIFT_PER_TICK);
        }

        SocietyProfile current = registry.find(societyId)
                .orElseThrow(() -> new SocietyException("applyDrift", "unknown societyId: " + societyId));

        double currentWeight = current.weightFor(dimensionId);
        double newWeight = Math.max(0.0D, Math.min(1.0D, currentWeight + delta));

        Map<String, DimensionWeight> updatedDimensions = new HashMap<>(current.dimensions());
        Map<String, Double> existingParameters = current.dimension(dimensionId)
                .map(DimensionWeight::parameters)
                .orElse(Map.of());
        updatedDimensions.put(dimensionId, DimensionWeight.of(dimensionId, newWeight, existingParameters));

        SocietyProfile updated = SocietyProfile.of(
                current.societyId(),
                current.societyName(),
                updatedDimensions,
                current.goapWeights());
        registry.replace(updated);

        long commitId = canonLog.latestCommitId() + 1L;
        String causalLink = "drift:" + societyId + ":" + dimensionId;
        Map<String, Object> driftDelta = Map.of(
                "societyId", societyId,
                "dimensionId", dimensionId,
                "previousWeight", currentWeight,
                "newWeight", newWeight,
                "delta", delta);
        canonLog.append(CanonEvent.of(commitId, currentTime, causalLink, driftDelta));
    }

    public List<CanonEvent> getDriftHistory(String societyId, CanonTime from, CanonTime to) {
        if (societyId == null || societyId.isBlank()) {
            throw new SocietyException("getDriftHistory", "societyId must not be null or blank");
        }
        if (from == null || to == null) {
            throw new SocietyException("getDriftHistory", "from and to must not be null");
        }
        List<CanonEvent> events = canonLog.query(from, to);
        List<CanonEvent> driftEvents = new ArrayList<>();
        String prefix = "drift:" + societyId + ":";
        for (CanonEvent event : events) {
            if (event.causalLink().startsWith(prefix)) {
                driftEvents.add(event);
            }
        }
        return List.copyOf(driftEvents);
    }
}
