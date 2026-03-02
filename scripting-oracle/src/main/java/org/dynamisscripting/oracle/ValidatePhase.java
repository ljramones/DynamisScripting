package org.dynamisscripting.oracle;

import java.util.ArrayList;
import java.util.List;
import org.dynamisscripting.api.CanonLog;
import org.dynamisscripting.api.value.Intent;
import org.dynamisscripting.spi.ArbitrationRule;
import org.dynamisscripting.spi.result.ValidationOutcome;

public final class ValidatePhase {
    private final RuleRegistry registry;
    private final BudgetLedger budgetLedger;

    public ValidatePhase(RuleRegistry registry, BudgetLedger budgetLedger) {
        if (registry == null || budgetLedger == null) {
            throw new OracleException("VALIDATE", "registry and budgetLedger must not be null");
        }
        this.registry = registry;
        this.budgetLedger = budgetLedger;
    }

    public ValidationPhaseResult run(Intent intent, CanonLog canonLog) {
        if (intent == null || canonLog == null) {
            throw new OracleException("VALIDATE", "intent and canonLog must not be null");
        }

        if (budgetLedger.registeredBudgets().contains(intent.intentType())) {
            boolean consumed = budgetLedger.consume(intent.intentType(), 1L);
            if (!consumed) {
                return new ValidationPhaseResult(
                        false,
                        "BUDGET_EXHAUSTED",
                        "Budget exhausted for intent type " + intent.intentType(),
                        List.of());
            }
        }

        List<String> applied = new ArrayList<>();
        for (ArbitrationRule rule : registry.validateRules()) {
            if (!rule.appliesTo(intent)) {
                continue;
            }

            applied.add(rule.ruleId());
            ValidationOutcome outcome = rule.evaluate(intent, canonLog);
            if (outcome == null) {
                throw new OracleException("VALIDATE", "rule " + rule.ruleId() + " returned null outcome");
            }
            if (!outcome.passed()) {
                return new ValidationPhaseResult(
                        false,
                        outcome.reasonCode(),
                        outcome.explanation(),
                        List.copyOf(applied));
            }
        }

        return new ValidationPhaseResult(true, "VALID", "Intent passed all validation rules", List.copyOf(applied));
    }

    public record ValidationPhaseResult(
            boolean passed,
            String reasonCode,
            String explanation,
            List<String> appliedRuleIds) {

        public ValidationPhaseResult {
            appliedRuleIds = List.copyOf(appliedRuleIds);
        }
    }
}
