package org.dynamisengine.scripting.canon;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;
import org.dynamisengine.scripting.api.CanonLog;
import org.dynamisengine.scripting.api.value.CanonEvent;
import org.dynamisengine.scripting.api.value.CanonTime;

public final class DefaultCanonLog implements CanonLog {
    private final CopyOnWriteArrayList<CanonLogEntry> entries;
    private final AtomicLong sequenceCounter;
    private final AtomicLong latestCommitId;
    private final ReadWriteLock appendLock;

    public DefaultCanonLog() {
        this.entries = new CopyOnWriteArrayList<>();
        this.sequenceCounter = new AtomicLong(1L);
        this.latestCommitId = new AtomicLong(0L);
        this.appendLock = new ReentrantReadWriteLock();
    }

    @Override
    public void append(CanonEvent event) {
        if (event == null) {
            throw new CanonLogException("append", "event must not be null");
        }
        if (event.commitId() <= 0L) {
            throw new CanonLogException("append", "commitId must be greater than 0");
        }
        if (event.causalLink() == null) {
            throw new CanonLogException("append", "causalLink must not be null");
        }
        if (event.causalLink().isBlank()) {
            throw new CanonLogException("append", "causalLink must not be empty");
        }

        appendLock.writeLock().lock();
        try {
            long latest = latestCommitId.get();
            if (event.commitId() <= latest) {
                throw new CanonLogException(
                        "append",
                        "commitId " + event.commitId() + " is not greater than latest " + latest);
            }

            long sequence = sequenceCounter.getAndIncrement();
            long wallNanosAtInsert = System.nanoTime();
            entries.add(CanonLogEntry.of(event, sequence, wallNanosAtInsert));
            latestCommitId.set(event.commitId());
        } finally {
            appendLock.writeLock().unlock();
        }
    }

    @Override
    public List<CanonEvent> query(CanonTime from, CanonTime to) {
        if (from == null || to == null) {
            throw new CanonLogException("query", "from and to must not be null");
        }
        if (from.isAfter(to)) {
            throw new CanonLogException("query", "from must not be after to");
        }

        appendLock.readLock().lock();
        try {
            List<CanonEvent> results = new ArrayList<>();
            for (CanonLogEntry entry : entries) {
                CanonTime eventTime = entry.event().canonTime();
                if (!eventTime.isBefore(from) && !eventTime.isAfter(to)) {
                    results.add(entry.event());
                }
            }
            return List.copyOf(results);
        } finally {
            appendLock.readLock().unlock();
        }
    }

    @Override
    public List<CanonEvent> queryByCausalLink(String causalLink) {
        if (causalLink == null || causalLink.isBlank()) {
            throw new CanonLogException("queryByCausalLink", "causalLink must not be null or empty");
        }

        appendLock.readLock().lock();
        try {
            List<CanonEvent> results = new ArrayList<>();
            for (CanonLogEntry entry : entries) {
                if (causalLink.equals(entry.event().causalLink())) {
                    results.add(entry.event());
                }
            }
            return List.copyOf(results);
        } finally {
            appendLock.readLock().unlock();
        }
    }

    @Override
    public Optional<CanonEvent> findByCommitId(long commitId) {
        appendLock.readLock().lock();
        try {
            for (CanonLogEntry entry : entries) {
                if (entry.event().commitId() == commitId) {
                    return Optional.of(entry.event());
                }
            }
            return Optional.empty();
        } finally {
            appendLock.readLock().unlock();
        }
    }

    @Override
    public CanonLog fork(long atCommitId) {
        if (atCommitId < 0L) {
            throw new CanonLogException("fork", "atCommitId must be >= 0");
        }

        List<CanonEvent> snapshot;
        appendLock.readLock().lock();
        try {
            List<CanonEvent> selected = new ArrayList<>();
            for (CanonLogEntry entry : entries) {
                if (entry.event().commitId() <= atCommitId) {
                    selected.add(entry.event());
                }
            }
            snapshot = List.copyOf(selected);
        } finally {
            appendLock.readLock().unlock();
        }

        DefaultCanonLog forkedLog = new DefaultCanonLog();
        for (CanonEvent event : snapshot) {
            forkedLog.append(event);
        }
        return forkedLog;
    }

    @Override
    public void replay(long fromCommitId, Consumer<CanonEvent> handler) {
        if (fromCommitId < 0L) {
            throw new CanonLogException("replay", "fromCommitId must be >= 0");
        }
        if (handler == null) {
            throw new CanonLogException("replay", "handler must not be null");
        }

        List<CanonEvent> stream;
        appendLock.readLock().lock();
        try {
            List<CanonEvent> selected = new ArrayList<>();
            for (CanonLogEntry entry : entries) {
                if (entry.event().commitId() >= fromCommitId) {
                    selected.add(entry.event());
                }
            }
            stream = List.copyOf(selected);
        } finally {
            appendLock.readLock().unlock();
        }

        for (CanonEvent event : stream) {
            handler.accept(event);
        }
    }

    @Override
    public long latestCommitId() {
        return latestCommitId.get();
    }

    @Override
    public CanonTime latestCanonTime() {
        appendLock.readLock().lock();
        try {
            if (entries.isEmpty()) {
                return CanonTime.ZERO;
            }
            return entries.get(entries.size() - 1).event().canonTime();
        } finally {
            appendLock.readLock().unlock();
        }
    }

    public int size() {
        return entries.size();
    }
}
