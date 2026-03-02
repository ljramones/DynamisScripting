package org.dynamisscripting.chronicler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.dynamisscripting.api.value.CanonTime;
import org.junit.jupiter.api.Test;

class QuestGraphTest {
    private static StoryNode node(String id, List<String> preconditions, int priority, boolean repeatable, long cooldown) {
        return StoryNode.of(id, "authored", "canonTime > 0", preconditions, priority, repeatable, cooldown);
    }

    @Test
    void addNodeSetsDormantAndIncrementsSize() {
        QuestGraph graph = new QuestGraph();
        StoryNode node = node("n1", List.of(), 1, false, 0);

        graph.addNode(node);

        assertEquals(1, graph.size());
        assertEquals(NodeState.DORMANT, graph.getState("n1"));
    }

    @Test
    void duplicateNodeIdThrows() {
        QuestGraph graph = new QuestGraph();
        graph.addNode(node("n1", List.of(), 1, false, 0));
        assertThrows(ChroniclerException.class, () -> graph.addNode(node("n1", List.of(), 1, false, 0)));
    }

    @Test
    void getNodeUnknownThrows() {
        QuestGraph graph = new QuestGraph();
        assertThrows(ChroniclerException.class, () -> graph.getNode("missing"));
    }

    @Test
    void nodeWithNoPreconditionsEligibleWhenDormant() {
        QuestGraph graph = new QuestGraph();
        graph.addNode(node("n1", List.of(), 1, false, 0));

        List<StoryNode> eligible = graph.eligibleNodes(CanonTime.of(1, 0));

        assertEquals(1, eligible.size());
        assertEquals("n1", eligible.get(0).nodeId());
    }

    @Test
    void nodeWithUnfiredPreconditionNotEligible() {
        QuestGraph graph = new QuestGraph();
        graph.addNode(node("parent", List.of(), 1, false, 0));
        graph.addNode(node("child", List.of("parent"), 1, false, 0));

        List<StoryNode> eligible = graph.eligibleNodes(CanonTime.of(1, 0));

        assertEquals(1, eligible.size());
        assertEquals("parent", eligible.get(0).nodeId());
    }

    @Test
    void nodeWithAllFiredPreconditionsEligible() {
        QuestGraph graph = new QuestGraph();
        graph.addNode(node("parent", List.of(), 1, false, 0));
        graph.addNode(node("child", List.of("parent"), 1, false, 0));
        graph.setState("parent", NodeState.FIRED, CanonTime.of(1, 0));

        List<StoryNode> eligible = graph.eligibleNodes(CanonTime.of(2, 0));

        assertTrue(eligible.stream().anyMatch(node -> "child".equals(node.nodeId())));
    }

    @Test
    void higherPriorityReturnedFirst() {
        QuestGraph graph = new QuestGraph();
        graph.addNode(node("low", List.of(), 1, false, 0));
        graph.addNode(node("high", List.of(), 100, false, 0));

        List<StoryNode> eligible = graph.eligibleNodes(CanonTime.of(1, 0));

        assertEquals("high", eligible.get(0).nodeId());
    }

    @Test
    void repeatableNodeEligibleAgainAfterCooldown() {
        QuestGraph graph = new QuestGraph();
        graph.addNode(node("repeat", List.of(), 1, true, 2));
        graph.setState("repeat", NodeState.FIRED, CanonTime.of(1, 0));

        List<StoryNode> tooEarly = graph.eligibleNodes(CanonTime.of(2, 0));
        List<StoryNode> afterCooldown = graph.eligibleNodes(CanonTime.of(3, 0));

        assertFalse(tooEarly.stream().anyMatch(node -> "repeat".equals(node.nodeId())));
        assertTrue(afterCooldown.stream().anyMatch(node -> "repeat".equals(node.nodeId())));
    }

    @Test
    void nonRepeatableNodeNotEligibleAfterFired() {
        QuestGraph graph = new QuestGraph();
        graph.addNode(node("once", List.of(), 1, false, 0));
        graph.setState("once", NodeState.FIRED, CanonTime.of(1, 0));

        List<StoryNode> eligible = graph.eligibleNodes(CanonTime.of(10, 0));

        assertFalse(eligible.stream().anyMatch(node -> "once".equals(node.nodeId())));
    }
}
