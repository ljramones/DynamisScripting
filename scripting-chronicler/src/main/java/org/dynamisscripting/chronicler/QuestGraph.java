package org.dynamisscripting.chronicler;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.dynamisscripting.api.value.CanonTime;

public final class QuestGraph {
    private final Map<String, StoryNode> nodes;
    private final Map<String, NodeState> nodeStates;
    private final Map<String, CanonTime> lastFiredAt;
    private CanonTime lastEvaluationTime;

    public QuestGraph() {
        this.nodes = new HashMap<>();
        this.nodeStates = new HashMap<>();
        this.lastFiredAt = new HashMap<>();
        this.lastEvaluationTime = CanonTime.ZERO;
    }

    public void addNode(StoryNode node) {
        if (node == null) {
            throw new ChroniclerException("addNode", "node must not be null");
        }
        if (nodes.containsKey(node.nodeId())) {
            throw new ChroniclerException("addNode", "duplicate nodeId: " + node.nodeId());
        }
        nodes.put(node.nodeId(), node);
        nodeStates.put(node.nodeId(), NodeState.DORMANT);
    }

    public void removeNode(String nodeId) {
        if (nodeId == null || nodeId.isBlank()) {
            throw new ChroniclerException("removeNode", "nodeId must not be null or empty");
        }
        nodes.remove(nodeId);
        nodeStates.remove(nodeId);
        lastFiredAt.remove(nodeId);
    }

    public StoryNode getNode(String nodeId) {
        StoryNode node = nodes.get(nodeId);
        if (node == null) {
            throw new ChroniclerException("getNode", "unknown nodeId: " + nodeId);
        }
        return node;
    }

    public NodeState getState(String nodeId) {
        NodeState state = nodeStates.get(nodeId);
        if (state == null) {
            throw new ChroniclerException("getState", "unknown nodeId: " + nodeId);
        }
        return state;
    }

    public void setState(String nodeId, NodeState state) {
        setState(nodeId, state, lastEvaluationTime);
    }

    public void setState(String nodeId, NodeState state, CanonTime currentTime) {
        if (!nodes.containsKey(nodeId)) {
            throw new ChroniclerException("setState", "unknown nodeId: " + nodeId);
        }
        if (state == null) {
            throw new ChroniclerException("setState", "state must not be null");
        }

        nodeStates.put(nodeId, state);
        if (state == NodeState.FIRED) {
            lastFiredAt.put(nodeId, currentTime == null ? CanonTime.ZERO : currentTime);
        }
    }

    public List<StoryNode> eligibleNodes(CanonTime currentTime) {
        if (currentTime == null) {
            throw new ChroniclerException("eligibleNodes", "currentTime must not be null");
        }
        this.lastEvaluationTime = currentTime;

        List<StoryNode> eligible = new ArrayList<>();
        for (StoryNode node : nodes.values()) {
            NodeState state = nodeStates.getOrDefault(node.nodeId(), NodeState.DORMANT);
            if (!stateEligible(node, state, currentTime)) {
                continue;
            }
            if (!preconditionsSatisfied(node)) {
                continue;
            }

            nodeStates.put(node.nodeId(), NodeState.ELIGIBLE);
            eligible.add(node);
        }

        eligible.sort(Comparator.comparingInt(StoryNode::priority).reversed().thenComparing(StoryNode::nodeId));
        return List.copyOf(eligible);
    }

    public int size() {
        return nodes.size();
    }

    public boolean isEmpty() {
        return nodes.isEmpty();
    }

    private boolean stateEligible(StoryNode node, NodeState state, CanonTime currentTime) {
        if (state == NodeState.DORMANT || state == NodeState.ELIGIBLE) {
            return true;
        }
        if (state != NodeState.FIRED) {
            return false;
        }
        if (!node.repeatable()) {
            return false;
        }

        CanonTime lastFired = lastFiredAt.get(node.nodeId());
        if (lastFired == null) {
            return true;
        }
        long elapsedTicks = currentTime.tick() - lastFired.tick();
        return elapsedTicks >= node.cooldownTicks();
    }

    private boolean preconditionsSatisfied(StoryNode node) {
        for (String prerequisite : node.preconditionNodeIds()) {
            if (nodeStates.getOrDefault(prerequisite, NodeState.DORMANT) != NodeState.FIRED) {
                return false;
            }
        }
        return true;
    }
}
