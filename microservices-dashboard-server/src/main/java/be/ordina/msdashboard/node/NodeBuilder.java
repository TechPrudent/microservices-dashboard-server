package be.ordina.msdashboard.node;

import java.util.*;

public class NodeBuilder {

    private String id;
    private Map<String, Object> details;
    private Integer lane;
    private List<Node> linkedNodes;

    public NodeBuilder withId(final String id) {
        this.id = id;
        return this;
    }

    public NodeBuilder withLane(final Integer lane) {
        this.lane = lane;
        return this;
    }

    public NodeBuilder withDetail(final String key, final String value) {
        if (details == null) {
            details = new HashMap<>();
        }
        details.put(key, value);
        return this;
    }

    public NodeBuilder withDetails(final Map<String, String> details) {
        details.entrySet().stream().forEach(e -> withDetail(e.getKey(), e.getValue()));
        return this;
    }

    public NodeBuilder withLinkedNode(final Node node) {
        if (linkedNodes == null) {
            linkedNodes = new ArrayList<>();
        }
        linkedNodes.add(node);
        return this;
    }

    public NodeBuilder withLinkedNodes(final Collection<Node> nodes) {
        nodes.stream().forEach(this::withLinkedNode);
        return this;
    }

    public Node build() {
        Node node = new Node();
        node.setId(id);
        node.setLane(lane);
        node.setLinkedNodes(linkedNodes);
        node.setDetails(details);
        return node;
    }

    public static NodeBuilder node() {
        return new NodeBuilder();
    }
}