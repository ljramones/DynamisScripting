package org.dynamisscripting.dsl;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class DslValidator {
    private static final Pattern LOOP_PATTERN = Pattern.compile("\\b(for|while|do)\\b");
    private static final Pattern ASSIGNMENT_PATTERN = Pattern.compile("(?<![=!<>])=(?!=)");
    private static final Pattern IDENTIFIER_PATTERN = Pattern.compile("\\b[A-Za-z_][A-Za-z0-9_]*\\b");
    private static final Pattern STRING_LITERAL_PATTERN = Pattern.compile("\"(?:\\\\.|[^\"\\\\])*\"");

    private static final Set<String> JAVA_KEYWORDS = Set.of(
            "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char", "class", "const",
            "continue", "default", "do", "double", "else", "enum", "extends", "final", "finally", "float",
            "for", "goto", "if", "implements", "import", "instanceof", "int", "interface", "long", "native",
            "new", "package", "private", "protected", "public", "return", "short", "static", "strictfp",
            "super", "switch", "synchronized", "this", "throw", "throws", "transient", "try", "void",
            "volatile", "while", "true", "false", "null", "var");

    private DslValidator() {
    }

    public static void validate(String expression) {
        String safeExpression = requireExpression(expression);
        String scrubbed = scrubStringLiterals(safeExpression);

        if (LOOP_PATTERN.matcher(scrubbed).find()) {
            throw new DslValidationException(safeExpression, "LOOP", "Loop constructs are not allowed in DSL");
        }

        if (containsMutation(scrubbed)) {
            throw new DslValidationException(safeExpression, "MUTATION", "Mutation constructs are not allowed in DSL");
        }

        if (containsAny(scrubbed,
                "System.out",
                "System.err",
                "System.in",
                "File",
                "Files",
                "FileInputStream",
                "Socket")) {
            throw new DslValidationException(safeExpression, "IO", "IO constructs are not allowed in DSL");
        }

        if (containsAny(scrubbed,
                "System.currentTimeMillis",
                "System.nanoTime",
                "Instant",
                "LocalDateTime",
                "new Date")) {
            throw new DslValidationException(
                    safeExpression,
                    "WALL_TIME",
                    "Wall-time constructs are not allowed; use CanonTime variables instead");
        }
    }

    public static List<String> extractVariables(String expression) {
        String safeExpression = requireExpression(expression);
        String scrubbed = scrubStringLiterals(safeExpression);
        Matcher matcher = IDENTIFIER_PATTERN.matcher(scrubbed);
        LinkedHashSet<String> variables = new LinkedHashSet<>();

        while (matcher.find()) {
            String token = matcher.group();
            if (JAVA_KEYWORDS.contains(token)) {
                continue;
            }
            if (isNumeric(token)) {
                continue;
            }
            variables.add(token);
        }

        return List.copyOf(variables);
    }

    private static boolean containsMutation(String expression) {
        if (expression.contains("++")
                || expression.contains("--")
                || expression.contains("+=")
                || expression.contains("-=")
                || expression.contains("*=")
                || expression.contains("/=")) {
            return true;
        }
        return ASSIGNMENT_PATTERN.matcher(expression).find();
    }

    private static boolean containsAny(String expression, String... tokens) {
        for (String token : tokens) {
            if (expression.contains(token)) {
                return true;
            }
        }
        return false;
    }

    private static String scrubStringLiterals(String expression) {
        return STRING_LITERAL_PATTERN.matcher(expression).replaceAll("\"\"");
    }

    private static boolean isNumeric(String token) {
        return token.chars().allMatch(Character::isDigit);
    }

    private static String requireExpression(String expression) {
        if (expression == null || expression.isBlank()) {
            throw new DslValidationException(expression, "UNKNOWN_VARIABLE", "Expression must not be null or blank");
        }
        return expression;
    }
}
