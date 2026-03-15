package org.dynamisengine.scripting.canon;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;
import org.dynamisengine.scripting.api.CanonLog;
import org.dynamisengine.scripting.api.value.CanonEvent;
import org.dynamisengine.scripting.api.value.CanonTime;
import org.junit.jupiter.api.Test;

class DefaultCanonLogTest {
    @Test
    void appendSingleEventUpdatesLatestCommitId() {
        DefaultCanonLog log = new DefaultCanonLog();

        log.append(event(1L, 1L, "intent:1"));

        assertEquals(1L, log.latestCommitId());
    }

    @Test
    void appendMultipleEventsTracksSizeAndLatestCommitId() {
        DefaultCanonLog log = new DefaultCanonLog();
        log.append(event(1L, 1L, "intent:1"));
        log.append(event(2L, 2L, "intent:2"));
        log.append(event(3L, 3L, "intent:3"));

        assertEquals(3, log.size());
        assertEquals(3L, log.latestCommitId());
    }

    @Test
    void appendOrderingViolationThrowsCanonLogException() {
        DefaultCanonLog log = new DefaultCanonLog();
        log.append(event(2L, 1L, "intent:1"));

        CanonLogException exception = assertThrows(CanonLogException.class, () -> log.append(event(2L, 2L, "intent:2")));

        assertTrue(exception.getMessage().contains("append"));
    }

    @Test
    void appendNullEventThrows() {
        DefaultCanonLog log = new DefaultCanonLog();
        assertThrows(CanonLogException.class, () -> log.append(null));
    }

    @Test
    void appendZeroCommitIdThrows() {
        DefaultCanonLog log = new DefaultCanonLog();
        assertThrows(RuntimeException.class, () -> log.append(CanonEvent.of(0L, CanonTime.of(1L, 1L), "intent:1", "delta")));
    }

    @Test
    void appendNullCausalLinkThrows() {
        DefaultCanonLog log = new DefaultCanonLog();
        assertThrows(RuntimeException.class, () -> log.append(CanonEvent.of(1L, CanonTime.of(1L, 1L), null, "delta")));
    }

    @Test
    void appendEmptyCausalLinkThrows() {
        DefaultCanonLog log = new DefaultCanonLog();
        assertThrows(RuntimeException.class, () -> log.append(CanonEvent.of(1L, CanonTime.of(1L, 1L), "  ", "delta")));
    }

    @Test
    void queryByInclusiveTimeRangeReturnsEventsInRange() {
        DefaultCanonLog log = new DefaultCanonLog();
        log.append(event(1L, 1L, "intent:1"));
        log.append(event(2L, 2L, "intent:2"));
        log.append(event(3L, 3L, "intent:3"));

        List<CanonEvent> result = log.query(CanonTime.of(2L, 0L), CanonTime.of(3L, Long.MAX_VALUE));

        assertEquals(2, result.size());
        assertEquals(2L, result.get(0).commitId());
        assertEquals(3L, result.get(1).commitId());
    }

    @Test
    void queryFromAfterToThrows() {
        DefaultCanonLog log = new DefaultCanonLog();
        assertThrows(CanonLogException.class,
                () -> log.query(CanonTime.of(2L, 0L), CanonTime.of(1L, 0L)));
    }

    @Test
    void queryByCausalLinkReturnsMatchingEvents() {
        DefaultCanonLog log = new DefaultCanonLog();
        log.append(event(1L, 1L, "intent:alpha"));
        log.append(event(2L, 2L, "intent:beta"));
        log.append(event(3L, 3L, "intent:alpha"));

        List<CanonEvent> result = log.queryByCausalLink("intent:alpha");

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(event -> "intent:alpha".equals(event.causalLink())));
    }

    @Test
    void findByCommitIdReturnsExpectedOptional() {
        DefaultCanonLog log = new DefaultCanonLog();
        log.append(event(1L, 1L, "intent:1"));

        assertTrue(log.findByCommitId(1L).isPresent());
        assertTrue(log.findByCommitId(999L).isEmpty());
    }

    @Test
    void latestCanonTimeReturnsZeroForEmptyAndLatestAfterAppend() {
        DefaultCanonLog log = new DefaultCanonLog();
        assertEquals(CanonTime.ZERO, log.latestCanonTime());

        CanonTime expected = CanonTime.of(3L, 10L);
        log.append(CanonEvent.of(1L, expected, "intent:1", "delta"));

        assertEquals(expected, log.latestCanonTime());
    }

    @Test
    void forkIsIsolatedFromParentAndViceVersa() {
        DefaultCanonLog parent = new DefaultCanonLog();
        parent.append(event(1L, 1L, "intent:1"));
        parent.append(event(2L, 2L, "intent:2"));

        CanonLog fork = parent.fork(1L);
        ((DefaultCanonLog) fork).append(event(3L, 3L, "intent:fork"));
        parent.append(event(4L, 4L, "intent:parent"));

        assertEquals(2, ((DefaultCanonLog) fork).size());
        assertEquals(3, parent.size());
        assertFalse(parent.findByCommitId(3L).isPresent());
        assertFalse(fork.findByCommitId(4L).isPresent());
    }

    @Test
    void replayInvokesHandlerInSequenceOrder() {
        DefaultCanonLog log = new DefaultCanonLog();
        log.append(event(1L, 1L, "intent:1"));
        log.append(event(2L, 2L, "intent:2"));
        log.append(event(3L, 3L, "intent:3"));

        List<Long> replayed = new ArrayList<>();
        log.replay(2L, event -> replayed.add(event.commitId()));

        assertEquals(List.of(2L, 3L), replayed);
    }

    @Test
    void replayPropagatesHandlerExceptions() {
        DefaultCanonLog log = new DefaultCanonLog();
        log.append(event(1L, 1L, "intent:1"));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> log.replay(1L, event -> {
                    throw new RuntimeException("boom");
                }));

        assertEquals("boom", exception.getMessage());
    }

    @Test
    void concurrentAppendCompletesWithExpectedSize() throws Exception {
        DefaultCanonLog log = new DefaultCanonLog();
        AtomicLong commitIdSource = new AtomicLong(1L);
        AtomicLong nextCommitToAppend = new AtomicLong(1L);
        CountDownLatch startGate = new CountDownLatch(1);
        var executor = Executors.newFixedThreadPool(10);
        List<Callable<Void>> tasks = new ArrayList<>();

        for (int thread = 0; thread < 10; thread++) {
            tasks.add(() -> {
                startGate.await();
                for (int i = 0; i < 10; i++) {
                    long commitId = commitIdSource.getAndIncrement();
                    while (nextCommitToAppend.get() != commitId) {
                        Thread.onSpinWait();
                    }
                    CanonTime canonTime = CanonTime.of(commitId, commitId * 10);
                    log.append(CanonEvent.of(commitId, canonTime, "intent:" + commitId, "delta"));
                    nextCommitToAppend.incrementAndGet();
                }
                return null;
            });
        }

        List<Future<Void>> futures = new ArrayList<>();
        for (Callable<Void> task : tasks) {
            futures.add(executor.submit(task));
        }
        startGate.countDown();
        executor.shutdown();
        for (Future<Void> future : futures) {
            future.get();
        }

        assertEquals(100, log.size());
        assertEquals(100L, log.latestCommitId());
    }

    private static CanonEvent event(long commitId, long tick, String causalLink) {
        return CanonEvent.of(commitId, CanonTime.of(tick, tick * 100L), causalLink, "delta-" + commitId);
    }
}
