package org.dynamisengine.scripting.dsl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.mvel3.MVEL;

public final class PredicateDsl {
    private final DslCompiler compiler;
    private final MVEL mvel;

    public PredicateDsl(DslCompiler compiler) {
        this.compiler = requireNonNull(compiler, "compiler");
        System.setProperty("mvel3.compiler.lambda.persistence", "false");
        this.mvel = new MVEL();
    }

    public boolean evaluate(String expression, CanonEvaluationContext context) {
        PredicateExpression compiled = compiler.compilePredicateExpression(expression);
        try {
            String resolvedExpression = resolveExpression(compiled, context);
            Boolean result = mvel.executeExpression(resolvedExpression, Set.of(), java.util.Map.of(), Boolean.class);
            return Boolean.TRUE.equals(result);
        } catch (RuntimeException exception) {
            throw new DslEvaluationException(expression, "", exception.getMessage(), exception);
        }
    }

    public DslExplainTrace evaluateWithTrace(String expression, CanonEvaluationContext context) {
        PredicateExpression compiled = compiler.compilePredicateExpression(expression);
        boolean result = evaluate(expression, context);
        List<ClauseTrace> clauses = new ArrayList<>();

        for (String clauseText : topLevelClauses(compiled.sourceText())) {
            Object resolvedValue = safeResolveForClause(clauseText, context);
            boolean passed = result;
            String explanation = clauseText + " resolved to " + resolvedValue + " — "
                    + (passed ? "clause passed" : "clause failed");
            clauses.add(new ClauseTrace(clauseText, resolvedValue, passed, explanation));
        }

        return new DslExplainTrace(expression, result, clauses);
    }

    private static List<String> topLevelClauses(String expression) {
        String[] split = expression.split("&&|\\|\\|");
        List<String> clauses = new ArrayList<>();
        for (String clause : split) {
            String trimmed = clause.trim();
            if (!trimmed.isEmpty()) {
                clauses.add(trimmed);
            }
        }
        if (clauses.isEmpty()) {
            clauses.add(expression.trim());
        }
        return clauses;
    }

    private static Object safeResolveForClause(String clauseText, CanonEvaluationContext context) {
        List<String> variables = DslValidator.extractVariables(clauseText);
        if (variables.isEmpty()) {
            return "literal";
        }
        String variable = variables.get(0);
        return context.resolve(variable);
    }

    private static String resolveExpression(PredicateExpression expression, CanonEvaluationContext context) {
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
