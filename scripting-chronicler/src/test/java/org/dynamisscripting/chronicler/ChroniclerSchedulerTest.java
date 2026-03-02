package org.dynamisscripting.chronicler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.dynamisscripting.api.value.CanonTime;
import org.dynamisscripting.canon.DefaultCanonLog;
import org.dynamisscripting.dsl.DslCompiler;
import org.dynamisscripting.dsl.PredicateDsl;
import org.junit.jupiter.api.Test;

class ChroniclerSchedulerTest {
    @Test
    void evaluateTickTrueTriggerReturnsFiredNodeAndUpdatesState() {
        QuestGraph graph = GraphLoader.buildManually(List.of(StoryNode.of("n1", "authored", "canonTime > 0", List.of(), 1, false, 0)));
        TriggerEvaluator evaluator = new TriggerEvaluator(new PredicateDsl(new DslCompiler()), new DefaultCanonLog());
        ChroniclerScheduler scheduler = new ChroniclerScheduler(graph, evaluator, 10);

        List<StoryNode> fired = scheduler.evaluateTick(CanonTime.of(5, 0));

        assertEquals(1, fired.size());
        assertEquals(NodeState.FIRED, graph.getState("n1"));
    }

    @Test
    void evaluateTickFalseTriggerKeepsNodeDormant() {
        QuestGraph graph = GraphLoader.buildManually(List.of(StoryNode.of("n1", "authored", "canonTime > 100", List.of(), 1, false, 0)));
        TriggerEvaluator evaluator = new TriggerEvaluator(new PredicateDsl(new DslCompiler()), new DefaultCanonLog());
        ChroniclerScheduler scheduler = new ChroniclerScheduler(graph, evaluator, 10);

        List<StoryNode> fired = scheduler.evaluateTick(CanonTime.of(5, 0));

        assertTrue(fired.isEmpty());
        assertTrue(graph.getState("n1") == NodeState.ELIGIBLE || graph.getState("n1") == NodeState.DORMANT);
    }

    @Test
    void maxNodeActivationsPerTickEnforced() {
        QuestGraph graph = GraphLoader.buildManually(List.of(
                StoryNode.of("n1", "authored", "canonTime > 0", List.of(), 1, false, 0),
                StoryNode.of("n2", "authored", "canonTime > 0", List.of(), 2, false, 0),
                StoryNode.of("n3", "authored", "canonTime > 0", List.of(), 3, false, 0)));
        TriggerEvaluator evaluator = new TriggerEvaluator(new PredicateDsl(new DslCompiler()), new DefaultCanonLog());
        ChroniclerScheduler scheduler = new ChroniclerScheduler(graph, evaluator, 2);

        List<StoryNode> fired = scheduler.evaluateTick(CanonTime.of(5, 0));

        assertEquals(2, fired.size());
    }
}
