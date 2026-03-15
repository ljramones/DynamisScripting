package org.dynamisengine.scripting.spi;

import org.dynamisengine.scripting.api.CanonLog;
import org.dynamisengine.scripting.api.value.Intent;
import org.dynamisengine.scripting.spi.result.ShapeOutcome;
import org.dynamisengine.scripting.spi.result.ValidationOutcome;

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
