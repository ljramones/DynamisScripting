package org.dynamisscripting.oracle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.dynamis.core.entity.EntityId;
import org.dynamisscripting.api.CanonLog;
import org.dynamisscripting.api.value.CanonTime;
import org.dynamisscripting.api.value.Intent;
import org.dynamisscripting.canon.DefaultCanonLog;
import org.dynamisscripting.spi.ArbitrationRule;
import org.dynamisscripting.spi.ArbitrationRule.RulePhase;
import org.dynamisscripting.spi.result.ShapeOutcome;
import org.dynamisscripting.spi.result.ValidationOutcome;
import org.junit.jupiter.api.Test;

class ShapePhaseTest {
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

    static ArbitrationRule alwaysShapeRule(String id, int priority, Intent replacement) {
        return new ArbitrationRule() {
            @Override
            public String ruleId() {
                return id;
            }

            @Override
            public int priority() {
                return priority;
            }

            @Override
            public RulePhase phase() {
                return RulePhase.SHAPE;
            }

            @Override
            public ValidationOutcome evaluate(Intent intent, CanonLog canonLog) {
                return new ValidationOutcome(true, "PASS", "ok");
            }

            @Override
            public ShapeOutcome shape(Intent intent, CanonLog canonLog) {
                return new ShapeOutcome(true, replacement, "SHAPED");
            }
        };
    }

    @Test
    void shapeRuleApplies() {
        RuleRegistry registry = new RuleRegistry();
        Intent replacement = testIntent("replacement", 1.0);
        registry.register(alwaysShapeRule("shape", 1, replacement));
        ShapePhase phase = new ShapePhase(registry);

        ShapePhase.ShapePhaseResult result = phase.run(testIntent("original", 1.0), new DefaultCanonLog());

        assertTrue(result.shaped());
        assertTrue(result.shapedIntent().isPresent());
        assertEquals("replacement", result.shapedIntent().get().intentType());
    }

    @Test
    void noApplicableShapeRuleReturnsNotShaped() {
        RuleRegistry registry = new RuleRegistry();
        ShapePhase phase = new ShapePhase(registry);

        ShapePhase.ShapePhaseResult result = phase.run(testIntent("original", 1.0), new DefaultCanonLog());

        assertFalse(result.shaped());
        assertTrue(result.shapedIntent().isEmpty());
    }

    @Test
    void firstApplicableShapeRuleWins() {
        RuleRegistry registry = new RuleRegistry();
        registry.register(alwaysShapeRule("first", 1, testIntent("firstResult", 1.0)));
        registry.register(alwaysShapeRule("second", 2, testIntent("secondResult", 1.0)));
        ShapePhase phase = new ShapePhase(registry);

        ShapePhase.ShapePhaseResult result = phase.run(testIntent("original", 1.0), new DefaultCanonLog());

        assertTrue(result.shaped());
        assertEquals("firstResult", result.shapedIntent().orElseThrow().intentType());
        assertEquals(List.of("first"), result.appliedRuleIds());
    }
}
