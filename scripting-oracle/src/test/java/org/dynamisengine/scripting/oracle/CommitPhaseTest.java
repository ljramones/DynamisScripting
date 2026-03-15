package org.dynamisengine.scripting.oracle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import org.dynamisengine.core.entity.EntityId;
import org.dynamisengine.scripting.api.value.CanonTime;
import org.dynamisengine.scripting.api.value.Intent;
import org.dynamisengine.scripting.canon.CanonTimekeeper;
import org.dynamisengine.scripting.canon.DefaultCanonLog;
import org.junit.jupiter.api.Test;

class CommitPhaseTest {
    static Intent testIntent(String type, double confidence) {
        return Intent.of(
                EntityId.of(1L),
                type,
                List.of(),
                "test rationale",
                confidence,
                CanonTime.ZERO,
                Intent.RequestedScope.PUBLIC);
    }

    @Test
    void commitAdvancesCanonTimeByOneTick() {
        DefaultCanonLog log = new DefaultCanonLog();
        CanonTimekeeper timekeeper = new CanonTimekeeper();
        CommitPhase phase = new CommitPhase(log, timekeeper, new AtomicLong(1L));

        phase.run(testIntent("x", 1.0), "intent:1:x");

        assertEquals(1L, timekeeper.current().tick());
    }

    @Test
    void commitAssignsMonotonicallyIncreasingCommitId() {
        DefaultCanonLog log = new DefaultCanonLog();
        CommitPhase phase = new CommitPhase(log, new CanonTimekeeper(), new AtomicLong(1L));

        CommitPhase.CommitPhaseResult first = phase.run(testIntent("x", 1.0), "intent:1:x");
        CommitPhase.CommitPhaseResult second = phase.run(testIntent("x", 1.0), "intent:1:x");

        assertEquals(1L, first.commitId());
        assertEquals(2L, second.commitId());
    }

    @Test
    void committedEventAppearsInCanonLog() {
        DefaultCanonLog log = new DefaultCanonLog();
        CommitPhase phase = new CommitPhase(log, new CanonTimekeeper(), new AtomicLong(1L));

        CommitPhase.CommitPhaseResult result = phase.run(testIntent("x", 1.0), "intent:1:x");

        assertTrue(log.findByCommitId(result.commitId()).isPresent());
    }

    @Test
    void causalLinkFormatMatchesIntentAgentAndType() {
        DefaultCanonLog log = new DefaultCanonLog();
        CommitPhase phase = new CommitPhase(log, new CanonTimekeeper(), new AtomicLong(1L));
        Intent intent = testIntent("accuse", 1.0);
        String causalLink = "intent:" + intent.agentId() + ":" + intent.intentType();

        CommitPhase.CommitPhaseResult result = phase.run(intent, causalLink);

        assertEquals(causalLink, log.findByCommitId(result.commitId()).orElseThrow().causalLink());
    }
}
