package org.dynamisscripting.chronicler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.dynamisscripting.api.value.CanonTime;
import org.dynamisscripting.canon.DefaultCanonLog;
import org.dynamisscripting.dsl.DslCompiler;
import org.dynamisscripting.dsl.DslExplainTrace;
import org.dynamisscripting.dsl.PredicateDsl;
import org.junit.jupiter.api.Test;

class TriggerEvaluatorTest {
    @Test
    void evaluateTrueForCanonTimeGreaterThanZeroAtTickFive() {
        TriggerEvaluator evaluator = evaluator();
        StoryNode node = StoryNode.of("n1", "authored", "canonTime > 0", List.of(), 1, false, 0);

        assertTrue(evaluator.evaluate(node, CanonTime.of(5, 0)));
    }

    @Test
    void evaluateFalseForCanonTimeGreaterThanHundredAtTickFive() {
        TriggerEvaluator evaluator = evaluator();
        StoryNode node = StoryNode.of("n1", "authored", "canonTime > 100", List.of(), 1, false, 0);

        assertFalse(evaluator.evaluate(node, CanonTime.of(5, 0)));
    }

    @Test
    void evaluateMalformedExpressionReturnsFalseAndLogsWarn() {
        TriggerEvaluator evaluator = evaluator();
        StoryNode node = StoryNode.of("n1", "authored", "while(true) {}", List.of(), 1, false, 0);

        Logger logger = Logger.getLogger(TriggerEvaluator.class.getName());
        logger.setLevel(Level.ALL);
        CapturingHandler handler = new CapturingHandler();
        logger.addHandler(handler);
        try {
            assertFalse(evaluator.evaluate(node, CanonTime.of(5, 0)));
            assertTrue(handler.warningCount() > 0);
        } finally {
            logger.removeHandler(handler);
        }
    }

    @Test
    void explainReturnsTraceMatchingEvaluate() {
        TriggerEvaluator evaluator = evaluator();
        StoryNode node = StoryNode.of("n1", "authored", "canonTime > 0", List.of(), 1, false, 0);

        boolean result = evaluator.evaluate(node, CanonTime.of(5, 0));
        DslExplainTrace trace = evaluator.explain(node, CanonTime.of(5, 0));

        assertNotNull(trace);
        assertEquals(result, trace.result());
    }

    private static TriggerEvaluator evaluator() {
        return new TriggerEvaluator(new PredicateDsl(new DslCompiler()), new DefaultCanonLog());
    }

    private static final class CapturingHandler extends Handler {
        private final List<LogRecord> records = new ArrayList<>();

        @Override
        public void publish(LogRecord record) {
            records.add(record);
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() {
        }

        int warningCount() {
            int count = 0;
            for (LogRecord record : records) {
                if (record.getLevel().intValue() >= Level.WARNING.intValue()) {
                    count++;
                }
            }
            return count;
        }
    }
}
