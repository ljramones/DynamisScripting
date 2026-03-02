package org.dynamisscripting.society;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import org.dynamisscripting.api.value.CanonTime;
import org.dynamisscripting.canon.DefaultCanonLog;
import org.junit.jupiter.api.Test;

class CulturalDriftTrackerTest {
    @Test
    void applyDriftUpdatesWeightInRegistry() {
        SocietyRegistry registry = new SocietyRegistry();
        registry.register(profile("empire", 0.5D));
        CulturalDriftTracker tracker = new CulturalDriftTracker(registry, new DefaultCanonLog());

        tracker.applyDrift("empire", "honor", 0.01D, CanonTime.of(1L, 1L));

        assertEquals(0.51D, registry.get("empire").weightFor("honor"), 0.000001D);
    }

    @Test
    void driftClampedToZeroWhenNegativeBeyondLowerBound() {
        SocietyRegistry registry = new SocietyRegistry();
        registry.register(profile("empire", 0.0D));
        CulturalDriftTracker tracker = new CulturalDriftTracker(registry, new DefaultCanonLog());

        tracker.applyDrift("empire", "honor", -0.01D, CanonTime.of(1L, 1L));

        assertEquals(0.0D, registry.get("empire").weightFor("honor"));
    }

    @Test
    void driftExceedingMaxThrows() {
        SocietyRegistry registry = new SocietyRegistry();
        registry.register(profile("empire", 0.5D));
        CulturalDriftTracker tracker = new CulturalDriftTracker(registry, new DefaultCanonLog());

        assertThrows(
                SocietyException.class,
                () -> tracker.applyDrift(
                        "empire",
                        "honor",
                        CulturalDriftTracker.MAX_DRIFT_PER_TICK + 0.0001D,
                        CanonTime.of(1L, 1L)));
    }

    @Test
    void driftIsRecordedInCanonLog() {
        SocietyRegistry registry = new SocietyRegistry();
        registry.register(profile("empire", 0.5D));
        DefaultCanonLog canonLog = new DefaultCanonLog();
        CulturalDriftTracker tracker = new CulturalDriftTracker(registry, canonLog);

        tracker.applyDrift("empire", "honor", 0.01D, CanonTime.of(1L, 1L));

        assertEquals(1, canonLog.queryByCausalLink("drift:empire:honor").size());
    }

    @Test
    void getDriftHistoryReturnsEventsInRange() {
        SocietyRegistry registry = new SocietyRegistry();
        registry.register(profile("empire", 0.5D));
        DefaultCanonLog canonLog = new DefaultCanonLog();
        CulturalDriftTracker tracker = new CulturalDriftTracker(registry, canonLog);

        tracker.applyDrift("empire", "honor", 0.01D, CanonTime.of(1L, 1L));
        tracker.applyDrift("empire", "honor", 0.01D, CanonTime.of(2L, 2L));

        assertEquals(
                1,
                tracker.getDriftHistory("empire", CanonTime.of(2L, 2L), CanonTime.of(2L, 2L)).size());
    }

    @Test
    void unknownSocietyThrows() {
        CulturalDriftTracker tracker = new CulturalDriftTracker(new SocietyRegistry(), new DefaultCanonLog());

        assertThrows(
                SocietyException.class,
                () -> tracker.applyDrift("unknown", "honor", 0.01D, CanonTime.of(1L, 1L)));
    }

    private static SocietyProfile profile(String id, double honorWeight) {
        return SocietyProfile.of(
                id,
                id,
                Map.of("honor", DimensionWeight.of("honor", honorWeight, Map.of())),
                Map.of("survival", 0.4D));
    }
}
