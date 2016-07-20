/*
 * Copyright 2012-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package be.ordina.msdashboard.aggregators;

import be.ordina.msdashboard.model.Node;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * TODO: To be reviewed + refactored
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
		/*Set<Node> realLinkedNodes = realNode.getLinkedToNodes();
		Set<Node> virtualLinkedNodes = virtualNode.getLinkedToNodes();
		if (virtualLinkedNodes.size() > realLinkedNodes.size()) {
			realLinkedNodes.addAll(CollectionUtils.subtract(virtualLinkedNodes, realLinkedNodes));
		}*/
	}
}
