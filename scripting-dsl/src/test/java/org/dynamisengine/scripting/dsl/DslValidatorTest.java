package org.dynamisengine.scripting.dsl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class DslValidatorTest {
    @Test
    void validSimpleExpressionPasses() {
        DslValidator.validate("funds >= 100");
    }

    @Test
    void validComparisonPasses() {
        DslValidator.validate("reputation > 0.5 && canonTime > 10");
    }

    @Test
    void loopRejected() {
        DslValidationException exception = assertThrows(
                DslValidationException.class,
                () -> DslValidator.validate("for(int i=0; i<10; i++) {}"));
        assertEquals("LOOP", exception.violationType());
    }

    @Test
    void whileRejected() {
        assertThrows(DslValidationException.class, () -> DslValidator.validate("while(true) {}"));
    }

    @Test
    void mutationRejected() {
        DslValidationException exception = assertThrows(
                DslValidationException.class,
                () -> DslValidator.validate("funds = 50"));
        assertEquals("MUTATION", exception.violationType());
    }

    @Test
    void compoundAssignmentRejected() {
        assertThrows(DslValidationException.class, () -> DslValidator.validate("funds += 50"));
    }

    @Test
    void ioRejected() {
        DslValidationException exception = assertThrows(
                DslValidationException.class,
                () -> DslValidator.validate("System.out.println()"));
        assertEquals("IO", exception.violationType());
    }

    @Test
    void wallTimeRejected() {
        DslValidationException exception = assertThrows(
                DslValidationException.class,
                () -> DslValidator.validate("System.currentTimeMillis() > 0"));
        assertEquals("WALL_TIME", exception.violationType());
    }

    @Test
    void extractVariablesReturnsExpectedIdentifiers() {
        List<String> variables = DslValidator.extractVariables("funds >= cost && reputation > threshold");
        assertTrue(variables.contains("funds"));
        assertTrue(variables.contains("cost"));
        assertTrue(variables.contains("reputation"));
        assertTrue(variables.contains("threshold"));
    }
}
