package org.dynamisengine.scripting.oracle;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.dynamisengine.scripting.api.CanonLog;
import org.dynamisengine.scripting.api.value.Intent;
import org.dynamisengine.scripting.spi.ArbitrationRule;
import org.dynamisengine.scripting.spi.result.ShapeOutcome;

public final class ShapePhase {
    private final RuleRegistry registry;

    public ShapePhase(RuleRegistry registry) {
        if (registry == null) {
            throw new OracleException("SHAPE", "registry must not be null");
        }
        this.registry = registry;
    }

    public ShapePhaseResult run(Intent intent, CanonLog canonLog) {
        if (intent == null || canonLog == null) {
            throw new OracleException("SHAPE", "intent and canonLog must not be null");
        }

        List<String> applied = new ArrayList<>();
        for (ArbitrationRule rule : registry.shapeRules()) {
            if (!rule.appliesTo(intent)) {
                continue;
            }

            applied.add(rule.ruleId());
            ShapeOutcome outcome;
            try {
                outcome = rule.shape(intent, canonLog);
            } catch (UnsupportedOperationException exception) {
                continue;
            }

            if (outcome != null && outcome.shaped() && outcome.result() != null) {
                return new ShapePhaseResult(
                        true,
                        Optional.of(outcome.result()),
                        outcome.reasonCode(),
                        List.copyOf(applied));
            }
        }

        return new ShapePhaseResult(false, Optional.empty(), "NO_SHAPE_RULE", List.copyOf(applied));
    }

    public record ShapePhaseResult(
            boolean shaped,
            Optional<Intent> shapedIntent,
            String reasonCode,
            List<String> appliedRuleIds) {

        public ShapePhaseResult {
            shapedIntent = shapedIntent == null ? Optional.empty() : shapedIntent;
            appliedRuleIds = List.copyOf(appliedRuleIds);
        }
    }
}
