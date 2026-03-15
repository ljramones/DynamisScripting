package org.dynamisengine.scripting.dsl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.dynamisengine.scripting.api.value.CanonTime;
import org.junit.jupiter.api.Test;

class CanonEvaluationContextTest {
    @Test
    void resolveCanonTimeReturnsCurrentTick() {
        CanonEvaluationContext context = new CanonEvaluationContext(new TestCanonLog(), CanonTime.of(5L, 900L));
        assertEquals(5L, context.resolve("canonTime"));
    }

    @Test
    void resolveSimulationNanosReturnsCurrentSimulationNanos() {
        CanonEvaluationContext context = new CanonEvaluationContext(new TestCanonLog(), CanonTime.of(5L, 900L));
        assertEquals(900L, context.resolve("simulationNanos"));
    }

    @Test
    void resolveUnknownVariableReturnsStubZero() {
        CanonEvaluationContext context = new CanonEvaluationContext(new TestCanonLog(), CanonTime.of(5L, 900L));
        assertEquals(0L, context.resolve("unknownVariable"));
    }
}
