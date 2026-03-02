package org.dynamisscripting.oracle;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.dynamisscripting.spi.ArbitrationRule;
import org.dynamisscripting.spi.ArbitrationRule.RulePhase;

public final class RuleRegistry {
    private static final Comparator<ArbitrationRule> PRIORITY_ORDER =
            Comparator.comparingInt(ArbitrationRule::priority).thenComparing(ArbitrationRule::ruleId);

    private final ReadWriteLock lock;
    private final Map<String, ArbitrationRule> rulesById;

    public RuleRegistry() {
        this.lock = new ReentrantReadWriteLock();
        this.rulesById = new HashMap<>();
    }

    public void register(ArbitrationRule rule) {
        if (rule == null) {
            throw new OracleException("register", "rule must not be null");
        }

        lock.writeLock().lock();
        try {
            if (rulesById.containsKey(rule.ruleId())) {
                throw new OracleException("register", "duplicate ruleId: " + rule.ruleId());
            }
            rulesById.put(rule.ruleId(), rule);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void unregister(String ruleId) {
        if (ruleId == null || ruleId.isBlank()) {
            throw new OracleException("unregister", "ruleId must not be null or empty");
        }

        lock.writeLock().lock();
        try {
            rulesById.remove(ruleId);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public List<ArbitrationRule> validateRules() {
        return rulesByPhase(RulePhase.VALIDATE);
    }

    public List<ArbitrationRule> shapeRules() {
        return rulesByPhase(RulePhase.SHAPE);
    }

    public int size() {
        lock.readLock().lock();
        try {
            return rulesById.size();
        } finally {
            lock.readLock().unlock();
        }
    }

    private List<ArbitrationRule> rulesByPhase(RulePhase phase) {
        lock.readLock().lock();
        try {
            List<ArbitrationRule> rules = new ArrayList<>();
            for (ArbitrationRule rule : rulesById.values()) {
                if (rule.phase() == phase) {
                    rules.add(rule);
                }
            }
            rules.sort(PRIORITY_ORDER);
            return List.copyOf(rules);
        } finally {
            lock.readLock().unlock();
        }
    }
}
