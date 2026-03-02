package org.dynamisscripting.ashford;

import java.util.List;
import java.util.Map;
import org.dynamis.core.entity.EntityId;
import org.dynamisscripting.api.value.CanonEvent;
import org.dynamisscripting.api.value.CanonTime;
import org.dynamisscripting.runtime.AgentCognitiveTier;
import org.dynamisscripting.runtime.RuntimeTickResult;
import org.dynamisscripting.runtime.ScriptingRuntime;

public final class AshfordDebugOverlay {
    private final ScriptingRuntime runtime;

    public AshfordDebugOverlay(ScriptingRuntime runtime) {
        if (runtime == null) {
            throw new IllegalArgumentException("runtime must not be null");
        }
        this.runtime = runtime;
    }

    public void printTick(RuntimeTickResult result) {
        if (result == null) {
            throw new IllegalArgumentException("result must not be null");
        }

        long tick = result.canonTime().tick();
        double durationMillis = result.tickDurationNanos() / 1_000_000.0;
        System.out.printf(
                "[Tick %d] CanonTime=%d | WorldEvents proposed=%d committed=%d | Duration=%.3fms%n",
                tick,
                tick,
                result.worldEventsProposed(),
                result.worldEventsCommitted(),
                durationMillis);

        long latestCommitId = runtime.canonLog().latestCommitId();
        System.out.println("CanonLog latestCommitId=" + latestCommitId);

        Map<EntityId, AgentCognitiveTier> tiers = runtime.degradationMonitor().allTiers(tick);
        System.out.println("Agent tiers=" + tiers);

        CanonTime from = CanonTime.of(Math.max(0L, tick - 2L), 0L);
        CanonTime to = result.canonTime();
        List<CanonEvent> eventsInWindow = runtime.canonLog().query(from, to);
        int start = Math.max(0, eventsInWindow.size() - 3);
        List<CanonEvent> lastThree = eventsInWindow.subList(start, eventsInWindow.size());
        System.out.println("Last events=" + lastThree);
    }
}
