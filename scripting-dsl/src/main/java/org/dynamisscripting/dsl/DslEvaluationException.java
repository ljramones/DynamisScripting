package org.dynamisscripting.dsl;

import org.dynamis.core.exception.DynamisException;

public final class DslEvaluationException extends DynamisException {
    private final String expression;
    private final String canonVariable;
    private final String reason;

    public DslEvaluationException(String expression, String canonVariable, String reason) {
        super("DSL evaluation failed: " + reason + " | variable=" + canonVariable + " | expression=" + expression);
        this.expression = expression;
        this.canonVariable = canonVariable;
        this.reason = reason;
    }

    public DslEvaluationException(String expression, String canonVariable, String reason, Throwable cause) {
        super(
                "DSL evaluation failed: " + reason + " | variable=" + canonVariable + " | expression=" + expression,
                cause);
        this.expression = expression;
        this.canonVariable = canonVariable;
        this.reason = reason;
    }

    public String expression() {
        return expression;
    }

    public String canonVariable() {
        return canonVariable;
    }

    public String reason() {
        return reason;
    }
}
