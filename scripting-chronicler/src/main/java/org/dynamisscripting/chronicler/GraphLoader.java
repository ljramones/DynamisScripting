package org.dynamisscripting.chronicler;

import java.util.List;

public final class GraphLoader {
    public GraphLoader() {
    }

    public QuestGraph loadFromYaml(String yamlContent) {
        throw new ChroniclerException(
                "loadFromYaml",
                "YAML loading not yet implemented — use GraphLoader.buildManually()");
    }

    public static QuestGraph buildManually(List<StoryNode> nodes) {
        if (nodes == null) {
            throw new ChroniclerException("buildManually", "nodes must not be null");
        }
        QuestGraph graph = new QuestGraph();
        for (StoryNode node : nodes) {
            graph.addNode(node);
        }
        return graph;
    }
}
