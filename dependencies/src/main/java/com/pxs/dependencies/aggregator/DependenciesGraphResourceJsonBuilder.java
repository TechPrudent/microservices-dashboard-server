package com.pxs.dependencies.aggregator;

import static com.google.common.collect.Maps.newHashMap;
import static com.pxs.dependencies.constants.Constants.DETAILS;
import static com.pxs.dependencies.constants.Constants.ID;
import static com.pxs.dependencies.constants.Constants.LANE;
import static com.pxs.dependencies.constants.Constants.MICROSERVICE;
import static com.pxs.dependencies.constants.Constants.OWN_HEALTH;
import static com.pxs.dependencies.constants.Constants.TYPE;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.stereotype.Component;

import com.pxs.dependencies.constants.Constants;
import com.pxs.dependencies.model.Node;
import com.pxs.dependencies.services.RedisService;

@Component
public class DependenciesGraphResourceJsonBuilder {

	private static final String DIRECTED = "directed";
	private static final String MULTIGRAPH = "multigraph";
	private static final String GRAPH = "graph";
	private static final String LANES = "lanes";
	private static final String NODES = "nodes";
	private static final String LINKS = "links";
	private static final String UI = "UI";
	private static final String ENDPOINT = "Endpoint";
	private static final String BACKEND = "Backend";
	private static final String DESCRIPTION = "description";
	@Autowired
	private HealthIndicatorsAggregator healthIndicatorsAggregator;

	@Autowired
	private RedisService redisService;

	@Autowired
	private VirtualDependenciesConverter virtualDependenciesConverter;

	public Map<String, Object> build() {
		Map<String, Map<String, Object>> dependencies = healthIndicatorsAggregator.fetchCombinedDependencies();
		Map<String, Node> virtualDependencies = redisService.getAllNodes();
		dependencies.putAll(virtualDependenciesConverter.convert(virtualDependencies));
		return createGraph(dependencies);

	}

	private Map<String, Object> createGraph(final Map<String, Map<String, Object>> dependencies) {
		Map<String, Object> graph = new HashMap<>();
		graph.put(DIRECTED, true);
		graph.put(MULTIGRAPH, false);
		graph.put(GRAPH, new String[0]);
		List<Map<String, Object>> nodes = new ArrayList<>();
		List<Map<String, Integer>> links = new ArrayList<>();

		for (String microserviceName : dependencies.keySet()) {
			Map<String, Object> microservice = dependencies.get(microserviceName);

			Map<String, Object> microserviceNode = createMicroserviceNode(microserviceName, microservice);
			if (!isNodeAlreadyThere(nodes, microserviceName)) {
				nodes.add(microserviceNode);
			}
			int microserviceNodeId = nodes.size() - 1;
			Set<Map.Entry<String, Object>> entries = microservice.entrySet();
			removeEurecaDescription(entries);
			for (Map.Entry<String, Object> dependencyEntrySet : entries) {
				int dependencyNodeId = findDependencyNode(dependencyEntrySet.getKey(), nodes);
				if (dependencyNodeId == -1) {
					Integer lane = 0;
					System.out.println("OOOOOOOOOOOOOOOOOOOOO  "+ dependencyEntrySet.getValue());
					if (dependencyEntrySet.getValue() instanceof Node) {
						lane = determineLane(((Node) dependencyEntrySet.getValue()).getDetails());
						nodes.add(createNode(dependencyEntrySet.getKey(), lane, ((Node) dependencyEntrySet.getValue()).getDetails()));
					}


					dependencyNodeId = nodes.size() - 1;
				}
				links.add(createLink(microserviceNodeId, dependencyNodeId));
			}
		}
		graph.put(LANES, constructLanes());
		graph.put(NODES, nodes);
		graph.put(LINKS, links);

		return graph;
	}

	private Integer determineLane(Map<String, Object> details) {
		if (Constants.MICROSERVICE.equals(details.get(TYPE))) {
			return new Integer("2");
		}
		return new Integer("3");
	}

	private int findDependencyNode(final String dependency, final List<Map<String, Object>> nodes) {
		for (Map<String, Object> map : nodes) {
			if (dependency.equals(map.get(ID))) {
				return nodes.indexOf(map);
			}
		}
		return -1;
	}

	private Map<String, Object> createMicroserviceNode(String microServicename, Map<String, Object> microservice) {
		Map<String, Object> details = new HashMap<>();
		for (Map.Entry<String, Object> detail : microservice.entrySet()) {
			if (!(detail.getValue() instanceof Node)) {
				details.put(detail.getKey(), detail.getValue());
				details.remove(detail.getKey());
			}
		}
		System.out.println("nnnnnnnnnnnnnnnnnnnnnnnnnnnnn  "+ details);
		Integer lane = determineLane(details);
		return createNode(microServicename, lane, details);
	}

	private Map<String, Object> createNode(final String id, final Integer lane, Map<String,Object> details) {
		Map<String, Object> node = new HashMap<>();
		node.put(ID, id);
		node.put(LANE, lane);
		node.put(DETAILS,details);
		return node;
	}

	private Map<String, Integer> createLink(final int source, final int target) {
		Map<String, Integer> link = new HashMap<>();
		link.put("source", source);
		link.put("target", target);
		return link;
	}

	private List<Map<Object, Object>> constructLanes() {
		List<Map<Object, Object>> lanes = new ArrayList<>();
		lanes.add(constructLane(0, UI));
		lanes.add(constructLane(1, ENDPOINT));
		lanes.add(constructLane(2, MICROSERVICE));
		lanes.add(constructLane(3, BACKEND));
		return lanes;
	}

	private Map<Object, Object> constructLane(final int lane, final String type) {
		Map<Object, Object> laneMap = newHashMap();
		laneMap.put(LANE, lane);
		laneMap.put(TYPE, type);
		return laneMap;
	}

	private void removeEurecaDescription(final Set<Map.Entry<String, Object>> entrySet) {
		for (Map.Entry<String, Object> entry : entrySet) {
			if (DESCRIPTION.equals(entry.getKey())) {
				entrySet.remove(entry);
				break;
			}
		}
	}

	private boolean isNodeAlreadyThere(final List<Map<String, Object>> nodes, final String microserviceId) {
		for (Map<String, Object> node : nodes) {
			if (microserviceId.equals(node.get(ID))) {
				return true;
			}
		}
		return false;
	}
}
