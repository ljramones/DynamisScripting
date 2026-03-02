package org.dynamisscripting.economy;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.dynamisscripting.api.CanonLog;
import org.dynamisscripting.api.value.CanonEvent;
import org.dynamisscripting.api.value.CanonTime;
import org.dynamisscripting.api.value.Intent;
import org.dynamisscripting.spi.ArbitrationRule;
import org.dynamisscripting.spi.result.ValidationOutcome;

public final class EconomicsBudgetRule implements ArbitrationRule {
    private static final Pattern VALUE_PATTERN = Pattern.compile("(?:value|amount)\\s*=\\s*([0-9]+(?:\\.[0-9]+)?)");

    private final CanonLog defaultCanonLog;

    @SuppressFBWarnings(
            value = {"EI_EXPOSE_REP2"},
            justification = "Rule intentionally uses shared CanonLog for deterministic canonical fund lookup")
    public EconomicsBudgetRule(CanonLog canonLog) {
        this.defaultCanonLog = canonLog;
    }

    @Override
    public String ruleId() {
        return "economics.budget";
    }

    @Override
    public int priority() {
        return 10;
    }

    @Override
    public RulePhase phase() {
        return RulePhase.VALIDATE;
    }

    @Override
    public ValidationOutcome evaluate(Intent intent, CanonLog canonLog) {
        if (!appliesTo(intent)) {
            return new ValidationOutcome(true, "NOT_APPLICABLE", "Intent is outside economics scope");
        }

        Optional<FactionFunds> funds = latestFundsFor(intent.agentId(), canonLog == null ? defaultCanonLog : canonLog);
        if (funds.isEmpty()) {
            return new ValidationOutcome(false, "NO_FUND_RECORD", "No faction funds found in CanonLog");
        }

        double requiredAmount = extractAmount(intent);
        if ("CONTRACT_ACTIVATE".equals(intent.intentType()) && !funds.get().canAfford(requiredAmount)) {
            return new ValidationOutcome(false, "INSUFFICIENT_FUNDS", "Insufficient funds for contract activation");
        }
        if ("BOUNTY_PLACE".equals(intent.intentType()) && !funds.get().canAfford(requiredAmount)) {
            return new ValidationOutcome(
                    false,
                    "INSUFFICIENT_FUNDS_FOR_BOUNTY",
                    "Insufficient funds for bounty placement");
        }

        return new ValidationOutcome(true, "FUNDS_SUFFICIENT", "Economics budget check passed");
    }

    @Override
    public boolean appliesTo(Intent intent) {
        if (intent == null) {
            return false;
        }
        return "CONTRACT_ACTIVATE".equals(intent.intentType()) || "BOUNTY_PLACE".equals(intent.intentType());
    }

    private Optional<FactionFunds> latestFundsFor(org.dynamis.core.entity.EntityId factionId, CanonLog canonLog) {
        if (canonLog == null) {
            return Optional.empty();
        }
        CanonTime latest = canonLog.latestCanonTime();
        if (CanonTime.ZERO.equals(latest) && canonLog.latestCommitId() == 0L) {
            return Optional.empty();
        }

        Optional<FactionFunds> latestFunds = Optional.empty();
        for (CanonEvent event : canonLog.query(CanonTime.ZERO, latest)) {
            if (event.delta() instanceof FactionFunds funds && funds.factionId().equals(factionId)) {
                latestFunds = Optional.of(funds);
            }
        }
        return latestFunds;
    }

    private static double extractAmount(Intent intent) {
        if (intent.rationale() == null) {
            return 0.0D;
        }
        Matcher matcher = VALUE_PATTERN.matcher(intent.rationale());
        if (matcher.find()) {
            return Double.parseDouble(matcher.group(1));
        }
        return 0.0D;
    }
}
