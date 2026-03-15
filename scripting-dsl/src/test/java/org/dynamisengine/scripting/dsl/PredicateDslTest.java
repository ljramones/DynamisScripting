package org.dynamisengine.scripting.dsl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.dynamisengine.scripting.api.value.CanonTime;
import org.junit.jupiter.api.Test;

class PredicateDslTest {
    @Test
    void evaluateCanonTimeGreaterThanZeroAtTickFiveReturnsTrue() {
        PredicateDsl predicateDsl = new PredicateDsl(new DslCompiler());
        CanonEvaluationContext context = new CanonEvaluationContext(new TestCanonLog(), CanonTime.of(5L, 500L));

        boolean result = predicateDsl.evaluate("canonTime > 0", context);
        assertTrue(result);
    }

    @Test
    void evaluateCanonTimeGreaterThanHundredAtTickFiveReturnsFalse() {
        PredicateDsl predicateDsl = new PredicateDsl(new DslCompiler());
        CanonEvaluationContext context = new CanonEvaluationContext(new TestCanonLog(), CanonTime.of(5L, 500L));

        boolean result = predicateDsl.evaluate("canonTime > 100", context);
        assertFalse(result);
    }

    @Test
    void evaluateWithTraceResultMatchesEvaluate() {
        PredicateDsl predicateDsl = new PredicateDsl(new DslCompiler());
        CanonEvaluationContext context = new CanonEvaluationContext(new TestCanonLog(), CanonTime.of(5L, 500L));

        boolean result = predicateDsl.evaluate("canonTime > 0", context);
        DslExplainTrace trace = predicateDsl.evaluateWithTrace("canonTime > 0", context);

        assertEquals(result, trace.result());
    }

    @Test
    void traceClausesListIsNonNullAndNonEmpty() {
        PredicateDsl predicateDsl = new PredicateDsl(new DslCompiler());
        CanonEvaluationContext context = new CanonEvaluationContext(new TestCanonLog(), CanonTime.of(5L, 500L));

        DslExplainTrace trace = predicateDsl.evaluateWithTrace("canonTime > 0", context);

        assertNotNull(trace.clauses());
        assertFalse(trace.clauses().isEmpty());
    }

    @Test
    void eachClauseHasNonNullExplanation() {
        PredicateDsl predicateDsl = new PredicateDsl(new DslCompiler());
        CanonEvaluationContext context = new CanonEvaluationContext(new TestCanonLog(), CanonTime.of(5L, 500L));

        DslExplainTrace trace = predicateDsl.evaluateWithTrace("canonTime > 0", context);

        for (ClauseTrace clause : trace.clauses()) {
            assertNotNull(clause.explanation());
        }
    }
}
