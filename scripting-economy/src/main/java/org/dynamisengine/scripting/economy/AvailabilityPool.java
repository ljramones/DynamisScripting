package org.dynamisengine.scripting.economy;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public final class AvailabilityPool {
    private final ConcurrentHashMap<String, AtomicLong> pools;

    public AvailabilityPool() {
        this.pools = new ConcurrentHashMap<>();
    }

    public void register(String resourceType, long initialCount) {
        if (resourceType == null || resourceType.isBlank()) {
            throw new EconomyException("register", "resourceType must not be null or blank");
        }
        if (initialCount < 0L) {
            throw new EconomyException("register", "initialCount must be >= 0");
        }
        AtomicLong previous = pools.putIfAbsent(resourceType, new AtomicLong(initialCount));
        if (previous != null) {
            throw new EconomyException("register", "resourceType already registered: " + resourceType);
        }
    }

    public boolean consume(String resourceType, long amount) {
        if (amount < 0L) {
            throw new EconomyException("consume", "amount must be >= 0");
        }
        AtomicLong pool = poolFor(resourceType, "consume");
        while (true) {
            long current = pool.get();
            if (amount > current) {
                return false;
            }
            if (pool.compareAndSet(current, current - amount)) {
                return true;
            }
        }
    }

    public long available(String resourceType) {
        return poolFor(resourceType, "available").get();
    }

    public void restore(String resourceType, long amount) {
        if (amount < 0L) {
            throw new EconomyException("restore", "amount must be >= 0");
        }
        poolFor(resourceType, "restore").addAndGet(amount);
    }

    private AtomicLong poolFor(String resourceType, String operation) {
        if (resourceType == null || resourceType.isBlank()) {
            throw new EconomyException(operation, "resourceType must not be null or blank");
        }
        AtomicLong pool = pools.get(resourceType);
        if (pool == null) {
            throw new EconomyException(operation, "unknown resourceType: " + resourceType);
        }
        return pool;
    }
}
