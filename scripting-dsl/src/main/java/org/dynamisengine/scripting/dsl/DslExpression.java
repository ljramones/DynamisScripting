package org.dynamisengine.scripting.dsl;

import java.util.List;

public sealed interface DslExpression permits PredicateExpression, RewriteExpression {
    String sourceText();

    Object compiledForm();

    List<String> referencedVariables();
}
