package org.dynamisengine.scripting.oracle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.dynamisengine.core.entity.EntityId;
import org.dynamisengine.scripting.api.CanonLog;
import org.dynamisengine.scripting.api.value.CanonTime;
import org.dynamisengine.scripting.api.value.Intent;
import org.dynamisengine.scripting.spi.ArbitrationRule;
import org.dynamisengine.scripting.spi.ArbitrationRule.RulePhase;
import org.dynamisengine.scripting.spi.result.ShapeOutcome;
import org.dynamisengine.scripting.spi.result.ValidationOutcome;
import org.junit.jupiter.api.Test;

class RuleRegistryTest {
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

    static ArbitrationRule alwaysPassRule(String ruleId, int priority) {
        return new ArbitrationRule() {
            @Override
            public String ruleId() {
                return ruleId;
            }

            @Override
            public int priority() {
                return priority;
            }

            @Override
            public RulePhase phase() {
                return RulePhase.VALIDATE;
            }

            @Override
            public ValidationOutcome evaluate(Intent intent, CanonLog canonLog) {
                return new ValidationOutcome(true, "PASS", "ok");
            }
        };
    }

    static ArbitrationRule alwaysShapeRule(String ruleId, int priority) {
        return new ArbitrationRule() {
            @Override
            public String ruleId() {
                return ruleId;
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
                return new ShapeOutcome(true, intent, "SHAPED");
            }
        };
    }

    @Test
    void registerAndRetrieveValidateRulesInPriorityOrder() {
        RuleRegistry registry = new RuleRegistry();
        registry.register(alwaysPassRule("r2", 20));
        registry.register(alwaysPassRule("r1", 10));

        List<ArbitrationRule> rules = registry.validateRules();

        assertEquals(2, rules.size());
        assertEquals("r1", rules.get(0).ruleId());
        assertEquals("r2", rules.get(1).ruleId());
    }

    @Test
    void shapeRulesSeparatedFromValidateRules() {
        RuleRegistry registry = new RuleRegistry();
        registry.register(alwaysPassRule("validate", 10));
        registry.register(alwaysShapeRule("shape", 5));

        assertEquals(1, registry.validateRules().size());
        assertEquals(1, registry.shapeRules().size());
        assertEquals("shape", registry.shapeRules().get(0).ruleId());
    }

    @Test
    void unregisterRemovesRule() {
        RuleRegistry registry = new RuleRegistry();
        registry.register(alwaysPassRule("validate", 10));

        registry.unregister("validate");

        assertEquals(0, registry.size());
    }

    @Test
    void duplicateRuleIdThrows() {
        RuleRegistry registry = new RuleRegistry();
        registry.register(alwaysPassRule("same", 10));

        assertThrows(OracleException.class, () -> registry.register(alwaysPassRule("same", 11)));
    }

    @Test
    void lowerPriorityNumberComesFirst() {
        RuleRegistry registry = new RuleRegistry();
        registry.register(alwaysPassRule("late", 100));
        registry.register(alwaysPassRule("early", 1));

        List<ArbitrationRule> rules = registry.validateRules();

        assertEquals("early", rules.get(0).ruleId());
        assertEquals("late", rules.get(1).ruleId());
    }
}
