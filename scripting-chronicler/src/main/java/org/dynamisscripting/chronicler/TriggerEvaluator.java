package org.dynamisscripting.chronicler;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.dynamis.core.logging.DynamisLogger;
import org.dynamisscripting.api.CanonLog;
import org.dynamisscripting.api.value.CanonTime;
import org.dynamisscripting.dsl.CanonEvaluationContext;
import org.dynamisscripting.dsl.ClauseTrace;
import org.dynamisscripting.dsl.DslExplainTrace;
import org.dynamisscripting.dsl.PredicateDsl;

public final class TriggerEvaluator {
    private static final DynamisLogger LOGGER = DynamisLogger.get(TriggerEvaluator.class);

    private final PredicateDsl predicateDsl;
    private final CanonLog canonLog;

    @SuppressFBWarnings(
            value = {"EI_EXPOSE_REP2"},
            justification = "TriggerEvaluator intentionally holds shared CanonLog reference for deterministic queries")
    public TriggerEvaluator(PredicateDsl predicateDsl, CanonLog canonLog) {
        if (predicateDsl == null || canonLog == null) {
            throw new ChroniclerException("TriggerEvaluator", "predicateDsl and canonLog must not be null");
        }
        this.predicateDsl = predicateDsl;
        this.canonLog = canonLog;
    }

    public boolean evaluate(StoryNode node, CanonTime currentTime) {
        try {
            CanonEvaluationContext context = new CanonEvaluationContext(canonLog, currentTime);
            return predicateDsl.evaluate(node.triggerPredicate(), context);
        } catch (RuntimeException exception) {
            LOGGER.warn(
                    "Trigger evaluation failed for node " + node.nodeId() + " expression '" + node.triggerPredicate() + "'",
                    exception);
            return false;
        }
    }

    public DslExplainTrace explain(StoryNode node, CanonTime currentTime) {
        try {
            CanonEvaluationContext context = new CanonEvaluationContext(canonLog, currentTime);
            return predicateDsl.evaluateWithTrace(node.triggerPredicate(), context);
        } catch (RuntimeException exception) {
            LOGGER.warn(
                    "Trigger explain failed for node " + node.nodeId() + " expression '" + node.triggerPredicate() + "'",
                    exception);
            return new DslExplainTrace(
                    node.triggerPredicate(),
                    false,
                    java.util.List.of(new ClauseTrace(
                            node.triggerPredicate(),
                            null,
                            false,
                            "Evaluation failed: " + exception.getMessage())));
        }
    }

    @SuppressFBWarnings(
            value = {"EI_EXPOSE_REP"},
            justification = "DefaultChronicler needs read-only CanonLog access for archetype evaluation")
    public CanonLog canonLog() {
        return canonLog;
    }
}
