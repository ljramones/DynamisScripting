package org.dynamisengine.scripting.dsl;

import java.util.Optional;
import java.util.Set;
import org.dynamisengine.scripting.api.value.Intent;
import org.mvel3.MVEL;

public final class RewriteDsl {
    private final DslCompiler compiler;
    private final MVEL mvel;

    public RewriteDsl(DslCompiler compiler) {
        this.compiler = requireNonNull(compiler, "compiler");
        System.setProperty("mvel3.compiler.lambda.persistence", "false");
        this.mvel = new MVEL();
    }

    public Optional<Intent> evaluate(String expression, Intent originalIntent, CanonEvaluationContext context) {
        RewriteExpression compiled = compiler.compileRewriteExpression(expression);

        try {
            String resolvedExpression = resolveExpression(compiled, context);
            Object result = mvel.executeExpression(resolvedExpression, Set.of(), java.util.Map.of(), Object.class);

            if (result == null) {
                return Optional.empty();
            }
            if (result instanceof Intent intentResult) {
                return Optional.of(intentResult);
            }
            if (result instanceof Boolean booleanResult) {
                return booleanResult ? Optional.of(originalIntent) : Optional.empty();
            }

            throw new DslEvaluationException(expression, "", "Rewrite expression must return Intent, Boolean, or null");
        } catch (DslEvaluationException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw new DslEvaluationException(expression, "", exception.getMessage(), exception);
        }
    }

    private static String resolveExpression(RewriteExpression expression, CanonEvaluationContext context) {
        String resolved = expression.sourceText();
        for (String variable : expression.referencedVariables()) {
            Object value = context.resolve(variable);
            resolved = resolved.replaceAll("\\b" + java.util.regex.Pattern.quote(variable) + "\\b", toLiteral(value));
        }
        return resolved;
    }

    private static String toLiteral(Object value) {
        if (value instanceof Number || value instanceof Boolean) {
            return String.valueOf(value);
        }
        if (value instanceof String stringValue) {
            return "\"" + stringValue.replace("\"", "\\\"") + "\"";
        }
        return "0";
    }

    private static <T> T requireNonNull(T value, String field) {
        if (value == null) {
            throw new DslEvaluationException("", field, "must not be null");
        }
        return value;
    }
}
