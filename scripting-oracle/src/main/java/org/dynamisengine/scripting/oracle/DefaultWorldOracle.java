package org.dynamisengine.scripting.oracle;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.dynamisengine.core.logging.DynamisLogger;
import org.dynamisengine.scripting.api.CanonLog;
import org.dynamisengine.scripting.api.WorldOracle;
import org.dynamisengine.scripting.api.value.Intent;
import org.dynamisengine.scripting.api.value.WorldEvent;
import org.dynamisengine.scripting.spi.IntentInterceptor;

/**
 * Oracle is the sole canonical mutation authority. It validates, shapes, and commits world state changes,
 * but never mutates agent beliefs, emotions, cognition, or authored NPC intent.
 */
public final class DefaultWorldOracle implements WorldOracle {
    private static final DynamisLogger LOGGER = DynamisLogger.get(DefaultWorldOracle.class);

    private final ValidatePhase validate;
    private final ShapePhase shape;
    private final CommitPhase commit;
    private final List<IntentInterceptor> interceptors;
    private final CanonLog canonLog;

    public DefaultWorldOracle(
            ValidatePhase validate,
            ShapePhase shape,
            CommitPhase commit,
            List<IntentInterceptor> interceptors,
            CanonLog canonLog) {
        this.validate = requireNonNull(validate, "validate");
        this.shape = requireNonNull(shape, "shape");
        this.commit = requireNonNull(commit, "commit");
        this.canonLog = requireNonNull(canonLog, "canonLog");
        this.interceptors = sortInterceptors(interceptors == null ? List.of() : interceptors);
    }

    @Override
    public ValidationResult validate(Intent intent) {
        try {
            Intent processedIntent = applyBeforeValidate(intent);
            ValidatePhase.ValidationPhaseResult result = validate.run(processedIntent, canonLog);
            return new ValidationResult(result.passed(), result.reasonCode(), result.explanation());
        } catch (OracleException exception) {
            return new ValidationResult(false, "VALIDATE_ERROR", exception.getMessage());
        }
    }

    @Override
    public ShapeResult shape(Intent intent) {
        try {
            ShapePhase.ShapePhaseResult result = shape.run(intent, canonLog);
            return new ShapeResult(
                    result.shaped(),
                    intent,
                    result.shapedIntent().orElse(intent),
                    result.reasonCode());
        } catch (OracleException exception) {
            return new ShapeResult(false, intent, intent, "SHAPE_ERROR");
        }
    }

    @Override
    public CommitResult commit(Intent intent) {
        Intent originalIntent = requireNonNull(intent, "intent");
        Intent processedIntent = applyBeforeValidate(originalIntent);

        ValidatePhase.ValidationPhaseResult validation = validate.run(processedIntent, canonLog);
        if (validation.passed()) {
            return commitIntent(processedIntent, originalIntent);
        }

        ShapePhase.ShapePhaseResult shaped = shape.run(processedIntent, canonLog);
        if (shaped.shaped() && shaped.shapedIntent().isPresent()) {
            return commitIntent(shaped.shapedIntent().get(), originalIntent);
        }

        ValidationResult rejectResult = new ValidationResult(false, validation.reasonCode(), validation.explanation());
        runAfterReject(processedIntent, rejectResult);
        return new CommitResult(false, 0L, validation.reasonCode());
    }

    @Override
    public CommitResult commitWorldEvent(WorldEvent worldEvent) {
        requireNonNull(worldEvent, "worldEvent");
        try {
            // Chronicler proposals are accepted as pre-validated by design; they bypass Validate/Shape.
            CommitPhase.CommitPhaseResult result =
                    commit.runWorldEvent(worldEvent, "worldevent:" + worldEvent.nodeId());
            return new CommitResult(result.committed(), result.commitId(), "WORLD_EVENT_COMMITTED");
        } catch (OracleException exception) {
            return new CommitResult(false, 0L, "WORLD_EVENT_REJECTED");
        }
    }

    public OracleExplainReport generateExplainReport(Intent intent) {
        Intent processedIntent = applyBeforeValidate(intent);
        ValidatePhase.ValidationPhaseResult validation = validate.run(processedIntent, canonLog);

        if (validation.passed()) {
            return OracleExplainReport.of(
                    processedIntent.intentType(),
                    processedIntent.agentId(),
                    "VALIDATE",
                    "ACCEPTED",
                    validation.reasonCode(),
                    validation.explanation(),
                    validation.appliedRuleIds(),
                    canonLog.latestCanonTime());
        }

        ShapePhase.ShapePhaseResult shaped = shape.run(processedIntent, canonLog);
        if (shaped.shaped()) {
            return OracleExplainReport.of(
                    processedIntent.intentType(),
                    processedIntent.agentId(),
                    "SHAPE",
                    "SHAPED",
                    shaped.reasonCode(),
                    "Intent was transformed by shape rules",
                    shaped.appliedRuleIds(),
                    canonLog.latestCanonTime());
        }

        return OracleExplainReport.of(
                processedIntent.intentType(),
                processedIntent.agentId(),
                "SHAPE",
                "REJECTED",
                validation.reasonCode(),
                validation.explanation(),
                combine(validation.appliedRuleIds(), shaped.appliedRuleIds()),
                canonLog.latestCanonTime());
    }

    private CommitResult commitIntent(Intent effectiveIntent, Intent originalIntent) {
        try {
            String causalLink = "intent:" + effectiveIntent.agentId() + ":" + effectiveIntent.intentType();
            CommitPhase.CommitPhaseResult result = commit.run(effectiveIntent, causalLink);
            CommitResult commitResult = new CommitResult(result.committed(), result.commitId(), "COMMITTED");
            runAfterCommit(originalIntent, commitResult);
            return commitResult;
        } catch (OracleException exception) {
            return new CommitResult(false, 0L, "COMMIT_ERROR");
        }
    }

    private Intent applyBeforeValidate(Intent intent) {
        Intent current = requireNonNull(intent, "intent");
        for (IntentInterceptor interceptor : interceptors) {
            current = requireNonNull(interceptor.beforeValidate(current), "beforeValidate result");
        }
        return current;
    }

    private void runAfterCommit(Intent originalIntent, CommitResult result) {
        for (IntentInterceptor interceptor : interceptors) {
            try {
                interceptor.afterCommit(originalIntent, result, canonLog);
            } catch (RuntimeException exception) {
                LOGGER.warn("IntentInterceptor afterCommit threw for interceptor " + interceptor.interceptorId(), exception);
            }
        }
    }

    private void runAfterReject(Intent intent, ValidationResult result) {
        for (IntentInterceptor interceptor : interceptors) {
            try {
                interceptor.afterReject(intent, result);
            } catch (RuntimeException exception) {
                LOGGER.warn("IntentInterceptor afterReject threw for interceptor " + interceptor.interceptorId(), exception);
            }
        }
    }

    private static List<IntentInterceptor> sortInterceptors(List<IntentInterceptor> values) {
        List<IntentInterceptor> sorted = new ArrayList<>(values);
        sorted.sort(Comparator.comparingInt(IntentInterceptor::priority).thenComparing(IntentInterceptor::interceptorId));
        return List.copyOf(sorted);
    }

    private static List<String> combine(List<String> first, List<String> second) {
        List<String> all = new ArrayList<>();
        all.addAll(first);
        all.addAll(second);
        return List.copyOf(all);
    }

    private static <T> T requireNonNull(T value, String field) {
        if (value == null) {
            throw new OracleException("ORACLE", field + " must not be null");
        }
        return value;
    }
}
