package org.dynamisengine.scripting.oracle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.dynamisengine.core.entity.EntityId;
import org.dynamisengine.scripting.api.CanonLog;
import org.dynamisengine.scripting.api.value.CanonTime;
import org.dynamisengine.scripting.api.value.Intent;
import org.dynamisengine.scripting.canon.DefaultCanonLog;
import org.dynamisengine.scripting.spi.ArbitrationRule;
import org.dynamisengine.scripting.spi.ArbitrationRule.RulePhase;
import org.dynamisengine.scripting.spi.result.ValidationOutcome;
import org.junit.jupiter.api.Test;

class ValidatePhaseTest {
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

    static ArbitrationRule alwaysPassRule(String id, int priority) {
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
                return RulePhase.VALIDATE;
            }

            @Override
            public ValidationOutcome evaluate(Intent intent, CanonLog canonLog) {
                return new ValidationOutcome(true, "PASS", "ok");
            }
        };
    }

    static ArbitrationRule alwaysFailRule(String id, int priority, String reasonCode) {
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
                return RulePhase.VALIDATE;
            }

            @Override
            public ValidationOutcome evaluate(Intent intent, CanonLog canonLog) {
                return new ValidationOutcome(false, reasonCode, "failed");
            }
        };
    }

    @Test
    void allRulesPass() {
        RuleRegistry registry = new RuleRegistry();
        registry.register(alwaysPassRule("a", 1));
        registry.register(alwaysPassRule("b", 2));
        ValidatePhase phase = new ValidatePhase(registry, new BudgetLedger());

        ValidatePhase.ValidationPhaseResult result = phase.run(testIntent("x", 1.0), new DefaultCanonLog());

        assertTrue(result.passed());
    }

    @Test
    void oneRuleFails() {
        RuleRegistry registry = new RuleRegistry();
        registry.register(alwaysPassRule("a", 1));
        registry.register(alwaysFailRule("b", 2, "DENIED"));
        ValidatePhase phase = new ValidatePhase(registry, new BudgetLedger());

        ValidatePhase.ValidationPhaseResult result = phase.run(testIntent("x", 1.0), new DefaultCanonLog());

        assertFalse(result.passed());
        assertEquals("DENIED", result.reasonCode());
    }

    @Test
    void rulesAppliedInPriorityOrder() {
        RuleRegistry registry = new RuleRegistry();
        registry.register(alwaysPassRule("second", 2));
        registry.register(alwaysPassRule("first", 1));
        ValidatePhase phase = new ValidatePhase(registry, new BudgetLedger());

        ValidatePhase.ValidationPhaseResult result = phase.run(testIntent("x", 1.0), new DefaultCanonLog());

        assertEquals(List.of("first", "second"), result.appliedRuleIds());
    }

    @Test
    void budgetExhaustedFailsBeforeRules() {
        RuleRegistry registry = new RuleRegistry();
        registry.register(alwaysPassRule("rule", 1));
        BudgetLedger ledger = new BudgetLedger();
        ledger.registerBudget("accuse", 0L);
        ValidatePhase phase = new ValidatePhase(registry, ledger);

        ValidatePhase.ValidationPhaseResult result = phase.run(testIntent("accuse", 1.0), new DefaultCanonLog());

        assertFalse(result.passed());
        assertEquals("BUDGET_EXHAUSTED", result.reasonCode());
        assertTrue(result.appliedRuleIds().isEmpty());
    }

    @Test
    void appliesToFalseSkipsRule() {
        RuleRegistry registry = new RuleRegistry();
        registry.register(new ArbitrationRule() {
            @Override
            public String ruleId() {
                return "skipped";
            }

            @Override
            public int priority() {
                return 1;
            }

            @Override
            public RulePhase phase() {
                return RulePhase.VALIDATE;
            }

            @Override
            public ValidationOutcome evaluate(Intent intent, CanonLog canonLog) {
                return new ValidationOutcome(true, "PASS", "ok");
            }

            @Override
            public boolean appliesTo(Intent intent) {
                return false;
            }
        });
        ValidatePhase phase = new ValidatePhase(registry, new BudgetLedger());

        ValidatePhase.ValidationPhaseResult result = phase.run(testIntent("x", 1.0), new DefaultCanonLog());

        assertTrue(result.passed());
        assertTrue(result.appliedRuleIds().isEmpty());
    }
}
