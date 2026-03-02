package org.dynamisscripting.oracle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import org.dynamis.core.entity.EntityId;
import org.dynamisscripting.api.CanonLog;
import org.dynamisscripting.api.WorldOracle;
import org.dynamisscripting.api.value.CanonTime;
import org.dynamisscripting.api.value.Intent;
import org.dynamisscripting.api.value.WorldEvent;
import org.dynamisscripting.canon.CanonTimekeeper;
import org.dynamisscripting.canon.DefaultCanonLog;
import org.dynamisscripting.spi.ArbitrationRule;
import org.dynamisscripting.spi.ArbitrationRule.RulePhase;
import org.dynamisscripting.spi.IntentInterceptor;
import org.dynamisscripting.spi.result.ShapeOutcome;
import org.dynamisscripting.spi.result.ValidationOutcome;
import org.junit.jupiter.api.Test;

class DefaultWorldOracleTest {
    static Intent testIntent(String type, double confidence) {
        return Intent.of(
                EntityId.of(1L),
                type,
                List.of(),
                "test rationale",
                confidence,
                CanonTime.ZERO,
                Intent.RequestedScope.PUBLIC);
    }

    static ArbitrationRule alwaysPassRule() {
        return new ArbitrationRule() {
            @Override
            public String ruleId() {
                return "alwaysPass";
            }

            @Override
            public int priority() {
                return 1;
            }

            @Override
            public RulePhase phase() {
                return RulePhase.VALIDATE;
            }

            @Override
            public ValidationOutcome evaluate(Intent intent, CanonLog canonLog) {
                return new ValidationOutcome(true, "PASS", "ok");
            }
        };
    }

    static ArbitrationRule alwaysFailRule(String reasonCode) {
        return new ArbitrationRule() {
            @Override
            public String ruleId() {
                return "alwaysFail";
            }

            @Override
            public int priority() {
                return 1;
            }

            @Override
            public RulePhase phase() {
                return RulePhase.VALIDATE;
            }

            @Override
            public ValidationOutcome evaluate(Intent intent, CanonLog canonLog) {
                return new ValidationOutcome(false, reasonCode, "denied");
            }
        };
    }

    static ArbitrationRule alwaysShapeRule(Intent replacement) {
        return new ArbitrationRule() {
            @Override
            public String ruleId() {
                return "alwaysShape";
            }

            @Override
            public int priority() {
                return 1;
            }

            @Override
            public RulePhase phase() {
                return RulePhase.SHAPE;
            }

            @Override
            public ValidationOutcome evaluate(Intent intent, CanonLog canonLog) {
                return new ValidationOutcome(true, "PASS", "ok");
            }

            @Override
            public ShapeOutcome shape(Intent intent, CanonLog canonLog) {
                return new ShapeOutcome(true, replacement, "SHAPED");
            }
        };
    }

    @Test
    void validIntentCommitsAndAppendsEvent() {
        DefaultCanonLog canonLog = new DefaultCanonLog();
        DefaultWorldOracle oracle = oracle(canonLog, List.of(alwaysPassRule()), List.of(), List.of());

        WorldOracle.CommitResult result = oracle.commit(testIntent("accuse", 1.0));

        assertTrue(result.committed());
        assertTrue(canonLog.findByCommitId(result.commitId()).isPresent());
    }

    @Test
    void invalidIntentShapeableCommitsShapedIntent() {
        DefaultCanonLog canonLog = new DefaultCanonLog();
        Intent shaped = testIntent("shaped.intent", 1.0);
        DefaultWorldOracle oracle = oracle(canonLog, List.of(alwaysFailRule("NO")), List.of(alwaysShapeRule(shaped)), List.of());

        WorldOracle.CommitResult result = oracle.commit(testIntent("original.intent", 1.0));

        assertTrue(result.committed());
        assertEquals(shaped.intentType(), canonLog.findByCommitId(result.commitId()).orElseThrow().delta() instanceof Intent i ? i.intentType() : "");
    }

    @Test
    void invalidIntentNotShapeableDoesNotCommit() {
        DefaultCanonLog canonLog = new DefaultCanonLog();
        DefaultWorldOracle oracle = oracle(canonLog, List.of(alwaysFailRule("NO")), List.of(), List.of());

        WorldOracle.CommitResult result = oracle.commit(testIntent("x", 1.0));

        assertFalse(result.committed());
        assertEquals(0, canonLog.size());
    }

    @Test
    void commitWorldEventAppendsWithWorldeventPrefix() {
        DefaultCanonLog canonLog = new DefaultCanonLog();
        DefaultWorldOracle oracle = oracle(canonLog, List.of(alwaysPassRule()), List.of(), List.of());
        WorldEvent event = WorldEvent.of("node-1", "arc", Map.of(), 1, CanonTime.ZERO);

        WorldOracle.CommitResult result = oracle.commitWorldEvent(event);

        assertTrue(result.committed());
        assertTrue(canonLog.findByCommitId(result.commitId()).orElseThrow().causalLink().startsWith("worldevent:"));
    }

    @Test
    void interceptorBeforeValidateCalledFirst() {
        DefaultCanonLog canonLog = new DefaultCanonLog();
        AtomicBoolean called = new AtomicBoolean(false);
        IntentInterceptor interceptor = new IntentInterceptor() {
            @Override
            public String interceptorId() {
                return "i";
            }

            @Override
            public int priority() {
                return 1;
            }

            @Override
            public Intent beforeValidate(Intent intent) {
                called.set(true);
                return intent;
            }

            @Override
            public void afterCommit(Intent originalIntent, WorldOracle.CommitResult result, CanonLog canonLog) {
            }
        };

        DefaultWorldOracle oracle = oracle(canonLog, List.of(alwaysPassRule()), List.of(), List.of(interceptor));
        oracle.commit(testIntent("x", 1.0));

        assertTrue(called.get());
    }

    @Test
    void interceptorAfterCommitCalledOnSuccess() {
        DefaultCanonLog canonLog = new DefaultCanonLog();
        AtomicBoolean called = new AtomicBoolean(false);
        IntentInterceptor interceptor = new IntentInterceptor() {
            @Override
            public String interceptorId() {
                return "i";
            }

            @Override
            public int priority() {
                return 1;
            }

            @Override
            public Intent beforeValidate(Intent intent) {
                return intent;
            }

            @Override
            public void afterCommit(Intent originalIntent, WorldOracle.CommitResult result, CanonLog canonLog) {
                called.set(true);
            }
        };

        DefaultWorldOracle oracle = oracle(canonLog, List.of(alwaysPassRule()), List.of(), List.of(interceptor));
        oracle.commit(testIntent("x", 1.0));

        assertTrue(called.get());
    }

    @Test
    void interceptorAfterRejectCalledOnRejection() {
        DefaultCanonLog canonLog = new DefaultCanonLog();
        AtomicBoolean called = new AtomicBoolean(false);
        IntentInterceptor interceptor = new IntentInterceptor() {
            @Override
            public String interceptorId() {
                return "i";
            }

            @Override
            public int priority() {
                return 1;
            }

            @Override
            public Intent beforeValidate(Intent intent) {
                return intent;
            }

            @Override
            public void afterCommit(Intent originalIntent, WorldOracle.CommitResult result, CanonLog canonLog) {
            }

            @Override
            public void afterReject(Intent intent, WorldOracle.ValidationResult result) {
                called.set(true);
            }
        };

        DefaultWorldOracle oracle = oracle(canonLog, List.of(alwaysFailRule("NO")), List.of(), List.of(interceptor));
        oracle.commit(testIntent("x", 1.0));

        assertTrue(called.get());
    }

    @Test
    void generateExplainReportDoesNotAppend() {
        DefaultCanonLog canonLog = new DefaultCanonLog();
        DefaultWorldOracle oracle = oracle(canonLog, List.of(alwaysPassRule()), List.of(), List.of());
        int before = canonLog.size();

        oracle.generateExplainReport(testIntent("x", 1.0));

        assertEquals(before, canonLog.size());
    }

    @Test
    void businessRuleFailuresReturnRecordsNotThrows() {
        DefaultCanonLog canonLog = new DefaultCanonLog();
        DefaultWorldOracle oracle = oracle(canonLog, List.of(alwaysFailRule("NO")), List.of(), List.of());

        WorldOracle.CommitResult result = oracle.commit(testIntent("x", 1.0));

        assertFalse(result.committed());
        assertEquals("NO", result.reasonCode());
    }

    private static DefaultWorldOracle oracle(
            DefaultCanonLog canonLog,
            List<ArbitrationRule> validateRules,
            List<ArbitrationRule> shapeRules,
            List<IntentInterceptor> interceptors) {
        RuleRegistry registry = new RuleRegistry();
        for (ArbitrationRule rule : validateRules) {
            registry.register(rule);
        }
        for (ArbitrationRule rule : shapeRules) {
            registry.register(rule);
        }

        ValidatePhase validate = new ValidatePhase(registry, new BudgetLedger());
        ShapePhase shape = new ShapePhase(registry);
        CommitPhase commit = new CommitPhase(canonLog, new CanonTimekeeper(), new AtomicLong(1L));
        return new DefaultWorldOracle(validate, shape, commit, interceptors, canonLog);
    }
}
