package org.dynamisscripting.dsl;

import java.util.List;
import org.dynamis.core.exception.DynamisException;

public record DslExplainTrace(String expression, boolean result, List<ClauseTrace> clauses) {
    public DslExplainTrace {
        if (expression == null) {
            throw new DynamisException("expression must not be null");
        }
        if (clauses == null) {
            throw new DynamisException("clauses must not be null");
        }
        clauses = List.copyOf(clauses);
    }
}
