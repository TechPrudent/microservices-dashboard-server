package be.ordina.msdashboard.aggregator;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import be.ordina.msdashboard.model.Node;

@Component
public class VirtualAndRealDependencyIntegrator {

	public List<Node> integrateVirtualNodesWithReal(List<Node> microservicesAndBackends, final List<Node> indexes, final Collection<Node> virtualNodes) {
		Set<Node> agregatedNodes = new HashSet<>();
		for (Node microserviceOrBackend : microservicesAndBackends) {
			for (Node virtualNode : virtualNodes) {
				if (microserviceOrBackend.getId().equals(virtualNode.getId())) {
					aggregateNodes(microserviceOrBackend, virtualNode);
					agregatedNodes.add(virtualNode);
				}
			}
		}
		virtualNodes.removeAll(agregatedNodes);
		microservicesAndBackends.addAll(virtualNodes);
		return microservicesAndBackends;
	}

	private void aggregateNodes(final Node realNode, final Node virtualNode) {
		List<Node> realLinkedNodes = realNode.getLinkedNodes();
		List<Node> virtualLinkedNodes = virtualNode.getLinkedNodes();
		if (virtualLinkedNodes.size() > realLinkedNodes.size()) {
			realLinkedNodes.addAll(CollectionUtils.subtract(virtualLinkedNodes, realLinkedNodes));
		}
	}
}
