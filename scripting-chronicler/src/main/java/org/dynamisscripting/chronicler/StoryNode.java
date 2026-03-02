package org.dynamisscripting.chronicler;

import java.util.List;
import org.dynamis.core.exception.DynamisException;

public record StoryNode(
        String nodeId,
        String archetypeId,
        String triggerPredicate,
        List<String> preconditionNodeIds,
        int priority,
        boolean repeatable,
        long cooldownTicks) {

    public StoryNode {
        requireNonBlank(nodeId, "nodeId");
        requireNonBlank(archetypeId, "archetypeId");
        requireNonBlank(triggerPredicate, "triggerPredicate");
        requireNonNull(preconditionNodeIds, "preconditionNodeIds");
        preconditionNodeIds = List.copyOf(preconditionNodeIds);
        if (cooldownTicks < 0L) {
            throw new DynamisException("cooldownTicks must be >= 0");
        }
    }

    public static StoryNode of(
            String nodeId,
            String archetypeId,
            String triggerPredicate,
            List<String> preconditionNodeIds,
            int priority,
            boolean repeatable,
            long cooldownTicks) {
        return new StoryNode(
                nodeId,
                archetypeId,
                triggerPredicate,
                preconditionNodeIds,
                priority,
                repeatable,
                cooldownTicks);
    }

    private static <T> T requireNonNull(T value, String field) {
        if (value == null) {
            throw new DynamisException(field + " must not be null");
        }
        return value;
    }

    private static String requireNonBlank(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new DynamisException(field + " must not be null or blank");
        }
        return value;
    }
}
