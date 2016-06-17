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
package be.ordina.msdashboard.aggregator;

import be.ordina.msdashboard.aggregator.health.HealthIndicatorsAggregator;
import be.ordina.msdashboard.aggregator.index.IndexesAggregator;
import be.ordina.msdashboard.aggregator.pact.PactsAggregator;
import be.ordina.msdashboard.constants.Constants;
import be.ordina.msdashboard.model.Node;
import be.ordina.msdashboard.store.NodeStore;
import com.google.common.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

import static com.google.common.base.CaseFormat.LOWER_CAMEL;
import static com.google.common.base.CaseFormat.LOWER_HYPHEN;
import static com.google.common.collect.Maps.newHashMap;

/**
 * @author Andreas Evers
 */
public class DependenciesGraphResourceJsonBuilder {

	private static final Logger LOG = LoggerFactory.getLogger(DependenciesGraphResourceJsonBuilder.class);

	private HealthIndicatorsAggregator healthIndicatorsAggregator;
	private IndexesAggregator indexesAggregator;
	private PactsAggregator pactsAggregator;

	private NodeStore redisService;

	private VirtualAndRealDependencyIntegrator virtualAndRealDependencyIntegrator;

	private final Map<String, Object> graph;

	@Autowired
	public DependenciesGraphResourceJsonBuilder(final HealthIndicatorsAggregator healthIndicatorsAggregator, final IndexesAggregator indexesAggregator,
												final PactsAggregator pactsAggregator, final NodeStore redisService,
												final VirtualAndRealDependencyIntegrator virtualAndRealDependencyIntegrator) {
		this.healthIndicatorsAggregator = healthIndicatorsAggregator;
		this.indexesAggregator = indexesAggregator;
		this.pactsAggregator = pactsAggregator;
		this.redisService = redisService;
		this.virtualAndRealDependencyIntegrator = virtualAndRealDependencyIntegrator;
		graph = new HashMap<>();
		initGraph(graph);
	}

	public Map<String, Object> build() {
		Node healthNode = /*healthIndicatorsAggregator.fetchCombinedDependencies()*/new Node("");
		Node indexNode = /*indexesAggregator.fetchIndexes()*/new Node("");
		Node pactNode = /*pactsAggregator.fetchPactNodes()*/new Node("");
		Set<Node> microservicesAndBackends = healthNode.getLinkedToNodes();
		Set<Node> resources = indexNode.getLinkedToNodes();
		Set<Node> pactComponents = pactNode.getLinkedToNodes();
		Collection<Node> virtualNodes = redisService.getAllNodes();
		if (!virtualNodes.isEmpty()) {
			virtualAndRealDependencyIntegrator.integrateVirtualNodesWithReal(microservicesAndBackends, resources, virtualNodes);
		}
		return createGraph(microservicesAndBackends, resources, pactComponents);
	}

	private Map<String, Object> createGraph(final Set<Node> microservicesWithTheirBackends, Set<Node> resources, Set<Node> pactComponents) {
		List<Map<String, Object>> nodes = new ArrayList<>();
		Set<Map<String, Integer>> links = new HashSet<>();

		for (Node microservice : microservicesWithTheirBackends) {
			String microserviceName = convertMicroserviceName(microservice.getId());
			Map<String, Object> microserviceNode = createMicroserviceNode(microserviceName, microservice);
			Optional<Integer> nodeIndex = getNodeIndex(nodes, microserviceName);
			if (!nodeIndex.isPresent()) {
				nodes.add(microserviceNode);
			}
			int microserviceNodeId = nodeIndex.orElse(nodes.size() - 1);
			Set<Node> dependencyNodes = microservice.getLinkedToNodes();
			removeEurekaDescription(dependencyNodes);
			for (Node dependencyNode : dependencyNodes) {
				if (Constants.MICROSERVICE.equals(dependencyNode.getDetails().get(Constants.TYPE))) {
					dependencyNode.setId(convertMicroserviceName(dependencyNode.getId()));
				}
				int dependencyNodeId = findNode(dependencyNode.getId(), nodes);
				if (dependencyNodeId == -1) {
					Integer lane = determineLane(dependencyNode.getDetails());
					nodes.add(createNode(dependencyNode.getId(), lane, dependencyNode.getDetails()));
					dependencyNodeId = nodes.size() - 1;
				}
				links.add(createLink(microserviceNodeId, dependencyNodeId));
			}
		}
		for (Node resource : resources) {
			Map<String, Object> resourceNode = createResourceNode(resource);
			nodes.add(resourceNode);
			String linkedMicroservice = resource.getLinkedToNodes().iterator().next().getId();
			int resourceNodeId = nodes.size() - 1;
			int linkedMicroserviceNodeId = findNode(linkedMicroservice, nodes);
			if (linkedMicroserviceNodeId != -1) {
				links.add(createLink(resourceNodeId, linkedMicroserviceNodeId));
			} else {
				LOG.warn("Unable to resolve a link from {} to {} (destination not found)", resource.getId(), linkedMicroservice);
			}
		}
		for (Node pactComponent : pactComponents) {
			Map<String, Object> pactComponentNode = createPactComponentNode(pactComponent);
			Optional<Integer> nodeIndex = getNodeIndex(nodes, pactComponent.getId());
			if (!nodeIndex.isPresent()) {
				nodes.add(pactComponentNode);
			}
			int pactNodeId = nodeIndex.orElse(nodes.size() - 1);
			pactComponent.getLinkedToNodes().stream()
					.map(linkedNode -> linkedNode.getId())
					.forEach(linkedNodeId -> {
						int resolvedLinkedNodeId = findNode(linkedNodeId, nodes);
						if (resolvedLinkedNodeId != -1) {
							links.add(createLink(pactNodeId, resolvedLinkedNodeId));
						} else {
							LOG.warn("Unable to resolve a link from {} to {} (destination not found)", pactComponent.getId(), linkedNodeId);
						}
					});
		}
		graph.put(Constants.NODES, nodes);
		graph.put(Constants.LINKS, links);
		return graph;
	}

	private String convertMicroserviceName(String name) {
		return LOWER_CAMEL.to(LOWER_HYPHEN, name);
	}

	@VisibleForTesting
	Integer determineLane(Map<String, Object> details) {
		String type = (String) details.get(Constants.TYPE);
		if (type == null) {
			return new Integer("3");
		}
		switch (type) {
			case Constants.UI_COMPONENT: return new Integer("0");
			case Constants.RESOURCE: return new Integer("1");
			case Constants.MICROSERVICE: return new Integer("2");
			default: return new Integer("3");
		}
	}

	private int findNode(final String dependency, final List<Map<String, Object>> nodes) {
		for (Map<String, Object> map : nodes) {
			if (dependency.equals(map.get(Constants.ID))) {
				return nodes.indexOf(map);
			}
		}
		return -1;
	}

	@VisibleForTesting
	Map<String, Object> createMicroserviceNode(final String microServicename, final Node node) {
		Map<String, Object> details = new HashMap<>();
		for (Map.Entry<String, Object> detail : node.getDetails().entrySet()) {
			if (!(detail.getValue() instanceof Node)) {
				details.put(detail.getKey(), detail.getValue());
			}
		}
		Integer lane = determineLane(details);
		return createNode(microServicename, lane, details);
	}

	private Map<String, Object> createResourceNode(final Node node) {
		return createNode(node.getId(), node.getLane(), node.getDetails());
	}

	private Map<String, Object> createPactComponentNode(final Node node) {
		Integer lane = determineLane(node.getDetails());
		return createNode(node.getId(), lane, node.getDetails());
	}

	private Map<String, Object> createNode(final String id, final Integer lane, Map<String, Object> details) {
		Map<String, Object> node = new HashMap<>();
		node.put(Constants.ID, id);
		node.put(Constants.LANE, lane);
		node.put(Constants.DETAILS, details);
		return node;
	}

	@VisibleForTesting
	Map<String, Integer> createLink(final int source, final int target) {
		Map<String, Integer> link = new HashMap<>();
		link.put("source", source);
		link.put("target", target);
		return link;
	}

	private List<Map<Object, Object>> constructLanes() {
		List<Map<Object, Object>> lanes = new ArrayList<>();
		lanes.add(constructLane(0, Constants.UI));
		lanes.add(constructLane(1, Constants.RESOURCES));
		lanes.add(constructLane(2, Constants.MICROSERVICES));
		lanes.add(constructLane(3, Constants.BACKEND));
		return lanes;
	}

	private List<String> constructTypes() {
		List<String> types = new ArrayList<>();
		types.add(Constants.DB);
		types.add(Constants.MICROSERVICE);
		types.add(Constants.REST);
		types.add(Constants.SOAP);
		types.add(Constants.JMS);
		types.add(Constants.RESOURCE);
		return types;
	}

	private Map<Object, Object> constructLane(final int lane, final String type) {
		Map<Object, Object> laneMap = newHashMap();
		laneMap.put(Constants.LANE, lane);
		laneMap.put(Constants.TYPE, type);
		return laneMap;
	}

	private void removeEurekaDescription(final Set<Node> dependencyNodes) {
		for (Node dependencyNode : dependencyNodes) {
			if (Constants.DESCRIPTION.equals(dependencyNode.getId())) {
				dependencyNodes.remove(dependencyNode);
				break;
			}
		}
	}

	private Optional<Integer> getNodeIndex(final List<Map<String, Object>> nodes, final String nodeId) {
		for (int i = 0; i < nodes.size(); i++) {
			Map<String, Object> node = nodes.get(i);
			if (nodeId.equals(node.get(Constants.ID))) {
				return Optional.of(i);
			}
		}
		return Optional.empty();
	}

	private Optional<Integer> getNodeIndexByLane(final List<Map<String, Object>> nodes, final String nodeId, final int lane) {
		for (int i = 0; i < nodes.size(); i++) {
			Map<String, Object> node = nodes.get(i);
			if (lane == (Integer) node.get(Constants.LANE) && nodeId.equals(node.get(Constants.ID))) {
				return Optional.of(i);
			}
		}
		return Optional.empty();
	}

	private void initGraph(final Map<String, Object> graph) {
		graph.put(Constants.DIRECTED, true);
		graph.put(Constants.MULTIGRAPH, false);
		graph.put(Constants.GRAPH, new String[0]);
		graph.put(Constants.LANES, constructLanes());
		graph.put(Constants.TYPES, constructTypes());
	}
}
