package be.ordina.msdashboard.aggregator;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;

import be.ordina.msdashboard.model.Node;

/**
 * @author Andreas Evers
 */
public class VirtualAndRealDependencyIntegrator {

	public Set<Node> integrateVirtualNodesWithReal(Set<Node> microservicesAndBackends, final Set<Node> indexes, final Collection<Node> virtualNodes) {
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
		Set<Node> realLinkedNodes = realNode.getLinkedToNodes();
		Set<Node> virtualLinkedNodes = virtualNode.getLinkedToNodes();
		if (virtualLinkedNodes.size() > realLinkedNodes.size()) {
			realLinkedNodes.addAll(CollectionUtils.subtract(virtualLinkedNodes, realLinkedNodes));
		}
	}
}
