package org.dynamisengine.scripting.economy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.dynamisengine.core.entity.EntityId;
import org.dynamisengine.scripting.api.value.CanonEvent;
import org.dynamisengine.scripting.api.value.CanonTime;
import org.dynamisengine.scripting.api.value.Intent;
import org.dynamisengine.scripting.canon.DefaultCanonLog;
import org.dynamisengine.scripting.spi.result.ValidationOutcome;
import org.junit.jupiter.api.Test;

class EconomicsBudgetRuleTest {
    @Test
    void nonEconomicsIntentAlwaysPasses() {
        DefaultCanonLog log = new DefaultCanonLog();
        EconomicsBudgetRule rule = new EconomicsBudgetRule(log);

        ValidationOutcome outcome = rule.evaluate(intent("MOVE", "value=100"), log);

        assertTrue(outcome.passed());
    }

    @Test
    void contractActivatePassesWithSufficientFunds() {
        DefaultCanonLog log = new DefaultCanonLog();
        log.append(CanonEvent.of(1L, CanonTime.of(1L, 1L), "setup:funds", FactionFunds.of(EntityId.of(1L), 1000.0D, 0.0D)));
        EconomicsBudgetRule rule = new EconomicsBudgetRule(log);

        ValidationOutcome outcome = rule.evaluate(intent("CONTRACT_ACTIVATE", "value=250"), log);

        assertTrue(outcome.passed());
    }

    @Test
    void contractActivateFailsWithInsufficientFunds() {
        DefaultCanonLog log = new DefaultCanonLog();
        log.append(CanonEvent.of(1L, CanonTime.of(1L, 1L), "setup:funds", FactionFunds.of(EntityId.of(1L), 100.0D, 0.0D)));
        EconomicsBudgetRule rule = new EconomicsBudgetRule(log);

        ValidationOutcome outcome = rule.evaluate(intent("CONTRACT_ACTIVATE", "value=250"), log);

        assertFalse(outcome.passed());
        assertEquals("INSUFFICIENT_FUNDS", outcome.reasonCode());
    }

    @Test
    void contractActivateFailsWithNoFundRecord() {
        DefaultCanonLog log = new DefaultCanonLog();
        EconomicsBudgetRule rule = new EconomicsBudgetRule(log);

        ValidationOutcome outcome = rule.evaluate(intent("CONTRACT_ACTIVATE", "value=250"), log);

        assertFalse(outcome.passed());
        assertEquals("NO_FUND_RECORD", outcome.reasonCode());
    }

    @Test
    void appliesToRecognizesEconomicsIntentsOnly() {
        EconomicsBudgetRule rule = new EconomicsBudgetRule(new DefaultCanonLog());
        assertTrue(rule.appliesTo(intent("CONTRACT_ACTIVATE", "value=50")));
        assertTrue(rule.appliesTo(intent("BOUNTY_PLACE", "value=50")));
        assertFalse(rule.appliesTo(intent("MOVE", "value=50")));
    }

    private static Intent intent(String type, String rationale) {
        return Intent.of(
                EntityId.of(1L),
                type,
                List.of(),
                rationale,
                0.9D,
                CanonTime.ZERO,
                Intent.RequestedScope.PUBLIC);
    }
}
