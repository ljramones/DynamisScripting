package org.dynamisscripting.dsl;

import java.util.concurrent.ConcurrentHashMap;

public final class DslCompiler {
    private final ConcurrentHashMap<String, DslExpression> expressionCache;

    public DslCompiler() {
        this.expressionCache = new ConcurrentHashMap<>();
    }

    public PredicateExpression compilePredicateExpression(String expression) {
        DslValidator.validate(expression);
        DslExpression cached = expressionCache.computeIfAbsent(predicateCacheKey(expression),
                ignored -> compilePredicateInternal(expression));
        return (PredicateExpression) cached;
    }

    public RewriteExpression compileRewriteExpression(String expression) {
        DslValidator.validate(expression);
        DslExpression cached = expressionCache.computeIfAbsent(rewriteCacheKey(expression),
                ignored -> compileRewriteInternal(expression));
        return (RewriteExpression) cached;
    }

    public void invalidateCache() {
        expressionCache.clear();
    }

    public int cacheSize() {
        return expressionCache.size();
    }

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
