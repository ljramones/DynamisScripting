package org.dynamisscripting.chronicler;

import java.util.Map;
import java.util.function.Consumer;
import org.dynamisscripting.api.Chronicler;
import org.dynamisscripting.api.value.CanonTime;
import org.dynamisscripting.api.value.WorldEvent;

public final class DefaultChronicler implements Chronicler {
    private final TriggerEvaluator evaluator;
    private final ChroniclerScheduler scheduler;
    private final ArchetypeInstantiator archetypeInstantiator;
    private final WorldEventEmitter emitter;

    public DefaultChronicler(
            QuestGraph graph,
            TriggerEvaluator evaluator,
            ChroniclerScheduler scheduler,
            ArchetypeInstantiator archetypeInstantiator,
            WorldEventEmitter emitter) {
        requireNonNull(graph, "graph");
        this.evaluator = requireNonNull(evaluator, "evaluator");
        this.scheduler = requireNonNull(scheduler, "scheduler");
        this.archetypeInstantiator = requireNonNull(archetypeInstantiator, "archetypeInstantiator");
        this.emitter = requireNonNull(emitter, "emitter");
    }

    @Override
    public void tick(CanonTime currentTime) {
        // Chronicler never commits to CanonLog directly — all world state changes go through Oracle via WorldEventEmitter
        for (StoryNode node : scheduler.evaluateTick(currentTime)) {
            WorldEvent worldEvent = WorldEvent.of(
                    node.nodeId(),
                    node.archetypeId(),
                    Map.of("triggerTime", currentTime.tick()),
                    node.priority(),
                    currentTime);
            emitter.emit(worldEvent);
        }

        for (WorldEvent worldEvent : archetypeInstantiator.evaluateAll(evaluator.canonLog(), currentTime)) {
            emitter.emit(worldEvent);
        }
    }

    @Override
    public void registerWorldEventListener(Consumer<WorldEvent> listener) {
        emitter.registerListener(listener);
    }

    private static <T> T requireNonNull(T value, String field) {
        if (value == null) {
            throw new ChroniclerException("DefaultChronicler", field + " must not be null");
        }
        return value;
    }
}
