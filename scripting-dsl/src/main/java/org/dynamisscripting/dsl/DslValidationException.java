package org.dynamisscripting.dsl;

import org.dynamis.core.exception.DynamisException;

public final class DslValidationException extends DynamisException {
    private final String expression;
    private final String violationType;
    private final String explanation;

    public DslValidationException(String expression, String violationType, String explanation) {
        super("DSL validation failed [" + violationType + "]: " + explanation + " | expression=" + expression);
        this.expression = expression;
        this.violationType = violationType;
        this.explanation = explanation;
    }

    public String expression() {
        return expression;
    }

    public String violationType() {
        return violationType;
    }

    public String explanation() {
        return explanation;
    }
}
