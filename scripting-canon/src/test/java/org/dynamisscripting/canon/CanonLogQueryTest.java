package org.dynamisscripting.canon;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import org.dynamisscripting.api.value.CanonEvent;
import org.dynamisscripting.api.value.CanonTime;
import org.junit.jupiter.api.Test;

class CanonLogQueryTest {
    @Test
    void countReturnsCorrectCountWithFilter() {
        DefaultCanonLog log = seedLog();

        int count = CanonLogQuery.count(
                log,
                CanonTime.of(1L, 0L),
                CanonTime.of(3L, 0L),
                event -> event.causalLink().startsWith("intent:alpha"));

        assertEquals(2, count);
    }

    @Test
    void filterByCausalLinkPrefixMatchesCorrectly() {
        DefaultCanonLog log = seedLog();

        var result = CanonLogQuery.filterByCausalLink(log, "intent:alp");

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(event -> event.causalLink().startsWith("intent:alp")));
    }

    @Test
    void latestMatchingReturnsMostRecentOrEmpty() {
        DefaultCanonLog log = seedLog();

        Optional<CanonEvent> latestAlpha = CanonLogQuery.latestMatching(
                log,
                event -> event.causalLink().startsWith("intent:alpha"));
        Optional<CanonEvent> none = CanonLogQuery.latestMatching(log, event -> event.causalLink().startsWith("missing"));

        assertTrue(latestAlpha.isPresent());
        assertEquals(3L, latestAlpha.get().commitId());
        assertTrue(none.isEmpty());
    }

    private static DefaultCanonLog seedLog() {
        DefaultCanonLog log = new DefaultCanonLog();
        log.append(CanonEvent.of(1L, CanonTime.of(1L, 0L), "intent:alpha", "delta1"));
        log.append(CanonEvent.of(2L, CanonTime.of(2L, 0L), "intent:beta", "delta2"));
        log.append(CanonEvent.of(3L, CanonTime.of(3L, 0L), "intent:alpha", "delta3"));
        return log;
    }
}
