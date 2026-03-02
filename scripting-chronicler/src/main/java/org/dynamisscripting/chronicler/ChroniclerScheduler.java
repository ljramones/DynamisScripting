package org.dynamisscripting.chronicler;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.ArrayList;
import java.util.List;
import org.dynamisscripting.api.value.CanonTime;

public final class ChroniclerScheduler {
    private final QuestGraph graph;
    private final TriggerEvaluator evaluator;
    private final int maxNodeActivationsPerTick;

    @SuppressFBWarnings(
            value = {"EI_EXPOSE_REP2"},
            justification = "Scheduler intentionally operates on shared QuestGraph state")
    public ChroniclerScheduler(QuestGraph graph, TriggerEvaluator evaluator, int maxNodeActivationsPerTick) {
        if (graph == null || evaluator == null) {
            throw new ChroniclerException("ChroniclerScheduler", "graph and evaluator must not be null");
        }
        if (maxNodeActivationsPerTick <= 0) {
            throw new ChroniclerException("ChroniclerScheduler", "maxNodeActivationsPerTick must be > 0");
        }
        this.graph = graph;
        this.evaluator = evaluator;
        this.maxNodeActivationsPerTick = maxNodeActivationsPerTick;
    }

    public List<StoryNode> evaluateTick(CanonTime currentTime) {
        List<StoryNode> fired = new ArrayList<>();
        for (StoryNode node : graph.eligibleNodes(currentTime)) {
            if (fired.size() >= maxNodeActivationsPerTick) {
                break;
            }
            if (evaluator.evaluate(node, currentTime)) {
                graph.setState(node.nodeId(), NodeState.FIRED, currentTime);
                fired.add(node);
            }
        }
        return List.copyOf(fired);
    }

    public int getMaxNodeActivationsPerTick() {
        return maxNodeActivationsPerTick;
    }
}
