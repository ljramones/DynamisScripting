package org.dynamisscripting.dsl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class DslCompilerTest {
    @Test
    void compileValidPredicateExpressionReturnsNonNull() {
        DslCompiler compiler = new DslCompiler();
        PredicateExpression expression = compiler.compilePredicateExpression("canonTime > 0");
        assertNotNull(expression);
    }

    @Test
    void compiledSourceTextMatchesInput() {
        DslCompiler compiler = new DslCompiler();
        String source = "canonTime > 0";

        PredicateExpression expression = compiler.compilePredicateExpression(source);
        assertEquals(source, expression.sourceText());
    }

    @Test
    void cacheHitKeepsCacheSizeAtOne() {
        DslCompiler compiler = new DslCompiler();

        PredicateExpression first = compiler.compilePredicateExpression("canonTime > 0");
        PredicateExpression second = compiler.compilePredicateExpression("canonTime > 0");

        assertSame(first, second);
        assertEquals(1, compiler.cacheSize());
    }

    @Test
    void invalidateCacheClearsCache() {
        DslCompiler compiler = new DslCompiler();
        compiler.compilePredicateExpression("canonTime > 0");

        compiler.invalidateCache();

        assertEquals(0, compiler.cacheSize());
    }

    @Test
    void invalidExpressionThrowsBeforeCompilation() {
        DslCompiler compiler = new DslCompiler();
        assertThrows(DslValidationException.class, () -> compiler.compilePredicateExpression("while(true) {}"));
    }
}
