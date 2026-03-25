package org.dynamisengine.scripting.dsl;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public final class DslCompiler {
    private final ConcurrentHashMap<String, DslExpression> expressionCache;

    // Telemetry counters (atomic for thread safety, telemetry-only)
    private final AtomicLong cacheHitCount = new AtomicLong();
    private final AtomicLong cacheMissCount = new AtomicLong();

    public DslCompiler() {
        this.expressionCache = new ConcurrentHashMap<>();
    }

    public PredicateExpression compilePredicateExpression(String expression) {
        DslValidator.validate(expression);
        String key = predicateCacheKey(expression);
        DslExpression existing = expressionCache.get(key);
        if (existing != null) {
            cacheHitCount.incrementAndGet();
            return (PredicateExpression) existing;
        }
        cacheMissCount.incrementAndGet();
        DslExpression compiled = expressionCache.computeIfAbsent(key,
                ignored -> compilePredicateInternal(expression));
        return (PredicateExpression) compiled;
    }

    public RewriteExpression compileRewriteExpression(String expression) {
        DslValidator.validate(expression);
        String key = rewriteCacheKey(expression);
        DslExpression existing = expressionCache.get(key);
        if (existing != null) {
            cacheHitCount.incrementAndGet();
            return (RewriteExpression) existing;
        }
        cacheMissCount.incrementAndGet();
        DslExpression compiled = expressionCache.computeIfAbsent(key,
                ignored -> compileRewriteInternal(expression));
        return (RewriteExpression) compiled;
    }

    public void invalidateCache() {
        expressionCache.clear();
    }

    public int cacheSize() {
        return expressionCache.size();
    }

    public long cacheHits() { return cacheHitCount.get(); }
    public long cacheMisses() { return cacheMissCount.get(); }

    private PredicateExpression compilePredicateInternal(String expression) {
        return new PredicateExpression(expression, expression, DslValidator.extractVariables(expression));
    }

    private RewriteExpression compileRewriteInternal(String expression) {
        return new RewriteExpression(expression, expression, DslValidator.extractVariables(expression));
    }

    private static String predicateCacheKey(String expression) {
        return "predicate::" + expression;
    }

    private static String rewriteCacheKey(String expression) {
        return "rewrite::" + expression;
    }
}
