package be.ordina.msdashboard.model;

import java.util.*;

public class NodeBuilder {

	private String id;
	private Map<String, Object> details;
	private Integer lane;
	@Deprecated
	private Set<Node> linkedToNodes;
	private Set<String> linkedToNodeIds;
	private Set<String> linkedFromNodeIds;

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

	public NodeBuilder havingLinkedToNodes(final Set<Node> linkedToNodes) {
		this.linkedToNodes = linkedToNodes;
		return this;
	}

	public NodeBuilder havingLinkedToNodeIds(final Set<String> linkedToNodeIds) {
		this.linkedToNodeIds = linkedToNodeIds;
		return this;
	}

	public NodeBuilder havingLinkedFromNodeIds(final Set<String> linkedFromNodeIds) {
		this.linkedFromNodeIds = linkedFromNodeIds;
		return this;
	}

	public NodeBuilder withDetail(final String key, final String value) {
		if (details == null) {
			details = new HashMap<>();
		}
		details.put(key, value);
		return this;
	}

	public NodeBuilder withLinkedToNode(final Node node) {
		if (linkedToNodes == null) {
			linkedToNodes = new HashSet<>();
		}
		linkedToNodes.add(node);
		return this;
	}

	public NodeBuilder withLinkedToNodeId(final String nodeId) {
		if (linkedToNodeIds == null) {
			linkedToNodeIds = new HashSet<>();
		}
		linkedToNodeIds.add(nodeId);
		return this;
	}

	public NodeBuilder withLinkedFromNodeId(final String nodeId) {
		if (linkedFromNodeIds == null) {
			linkedFromNodeIds = new HashSet<>();
		}
		linkedFromNodeIds.add(nodeId);
		return this;
	}

	public NodeBuilder withLinkedNodes(final Collection<Node> nodes) {
		nodes.stream().forEach(this::withLinkedToNode);
		return this;
	}

	public Node build() {
		Node node = new Node(id);
		node.setLane(lane);
		node.setLinkedToNodes(linkedToNodes);
		node.setLinkedToNodeIds(linkedToNodeIds);
		node.setLinkedFromNodeIds(linkedFromNodeIds);
		node.setDetails(details);
		return node;
	}

	public static NodeBuilder node() {
		return new NodeBuilder();
	}
}