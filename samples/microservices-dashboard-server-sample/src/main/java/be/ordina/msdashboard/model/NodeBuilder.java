package be.ordina.msdashboard.model;

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

	public NodeBuilder havingDetails(final Map<String, Object> details) {
		this.details = details;
		return this;
	}

	public NodeBuilder havingLinkedNodes(final List<Node> linkedNodes) {
		this.linkedNodes = linkedNodes;
		return this;
	}

	public NodeBuilder withDetail(final String key, final String value) {
		if (details == null) {
			details = new HashMap<>();
		}
		details.put(key, value);
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