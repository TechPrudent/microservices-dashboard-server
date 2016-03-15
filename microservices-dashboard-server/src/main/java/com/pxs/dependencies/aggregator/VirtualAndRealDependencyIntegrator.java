package com.pxs.dependencies.aggregator;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import com.pxs.dependencies.model.Node;

@Component
public class VirtualAndRealDependencyIntegrator {

	public List<Node> integrateVirtualNodesToReal(final List<Node> realNodes, final List<Node> virtualNodes) {
		Set<Node> agregatedNodes = new HashSet<>();
		for (Node realNode : realNodes) {
			for (Node virtualNode : virtualNodes) {
				if (realNode.getId().equals(virtualNode.getId())) {
					aggregateNodes(realNode, virtualNode);
					agregatedNodes.add(virtualNode);
				}
			}
		}
		virtualNodes.removeAll(agregatedNodes);
		realNodes.addAll(virtualNodes);
		return realNodes;
	}

	private void aggregateNodes(final Node realNode, final Node virtualNode) {
		List<Node> realLinkedNodes = realNode.getLinkedNodes();
		List<Node> virtualLinkedNodes = virtualNode.getLinkedNodes();
		if (virtualLinkedNodes.size() > realLinkedNodes.size()) {
			realLinkedNodes.addAll(CollectionUtils.subtract(virtualLinkedNodes, realLinkedNodes));
		}
	}
}
