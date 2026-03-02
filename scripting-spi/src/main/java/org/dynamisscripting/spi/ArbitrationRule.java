package org.dynamisscripting.spi;

import org.dynamisscripting.api.CanonLog;
import org.dynamisscripting.api.value.Intent;
import org.dynamisscripting.spi.result.ShapeOutcome;
import org.dynamisscripting.spi.result.ValidationOutcome;

public interface ArbitrationRule {
    String ruleId();

    int priority();

    RulePhase phase();

    ValidationOutcome evaluate(Intent intent, CanonLog canonLog);

    default ShapeOutcome shape(Intent intent, CanonLog canonLog) {
        throw new UnsupportedOperationException("This rule is a VALIDATE rule, not a SHAPE rule");
    }

    default boolean appliesTo(Intent intent) {
        return true;
    }

    enum RulePhase {
        VALIDATE,
        SHAPE
    }
}
