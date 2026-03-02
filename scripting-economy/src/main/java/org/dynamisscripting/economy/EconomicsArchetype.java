package org.dynamisscripting.economy;

import java.util.Map;
import org.dynamisscripting.api.CanonLog;
import org.dynamisscripting.api.value.CanonEvent;
import org.dynamisscripting.api.value.CanonTime;
import org.dynamisscripting.api.value.Intent;
import org.dynamisscripting.api.value.WorldEvent;
import org.dynamisscripting.spi.ChroniclerNodeArchetype;

public final class EconomicsArchetype implements ChroniclerNodeArchetype {
    @Override
    public String archetypeId() {
        return "economics.contract_escalation";
    }

    @Override
    public String archetypeName() {
        return "Contract Escalation Arc";
    }

    @Override
    public boolean canInstantiate(CanonLog canonLog, CanonTime currentTime) {
        return activeContractCount(canonLog) >= 2;
    }

    @Override
    public WorldEvent instantiate(CanonLog canonLog, CanonTime currentTime) {
        int activeCount = activeContractCount(canonLog);
        return WorldEvent.of(
                "economics.market_tension",
                archetypeId(),
                Map.of("activeContractCount", activeCount),
                5,
                currentTime);
    }

    @Override
    public int maxConcurrentInstances() {
        return 3;
    }

    private static int activeContractCount(CanonLog canonLog) {
        if (canonLog == null || (canonLog.latestCommitId() == 0L && CanonTime.ZERO.equals(canonLog.latestCanonTime()))) {
            return 0;
        }

        int active = 0;
        for (CanonEvent event : canonLog.query(CanonTime.ZERO, canonLog.latestCanonTime())) {
            if (!(event.delta() instanceof Intent intent)) {
                continue;
            }
            if ("CONTRACT_ACTIVATE".equals(intent.intentType())) {
                active++;
            } else if ("CONTRACT_DISSOLVE".equals(intent.intentType())) {
                active = Math.max(0, active - 1);
            }
        }
        return active;
    }
}
