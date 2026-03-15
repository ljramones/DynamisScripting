package org.dynamisengine.scripting.dsl;

import java.util.List;
import org.dynamisengine.core.exception.DynamisException;

public record PredicateExpression(String sourceText, Object compiledForm, List<String> referencedVariables)
        implements DslExpression {

    public PredicateExpression {
        requireNonNull(sourceText, "sourceText");
        requireNonNull(compiledForm, "compiledForm");
        requireNonNull(referencedVariables, "referencedVariables");
        referencedVariables = List.copyOf(referencedVariables);
    }

    private static <T> T requireNonNull(T value, String field) {
        if (value == null) {
            throw new DynamisException(field + " must not be null");
        }
        return value;
    }
}
