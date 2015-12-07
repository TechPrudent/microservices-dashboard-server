package com.pxs.dependencies.aggregator;

import static com.google.common.collect.Maps.newHashMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DependenciesGraphResourceJsonBuilder {

	@Autowired
	private HealthIndicatorsAggregator healthIndicatorsAggregator;

	public Map<String, Object> build() {
		Map<String, Map<String, Object>> dependencies = healthIndicatorsAggregator.fetchCombinedDependencies();
		return createGraph(dependencies);

	}

	private Map<String, Object> createGraph(final Map<String, Map<String, Object>> dependencies) {
		Map<String, Object> graph = new HashMap<>();
		graph.put("directed", true);
		graph.put("multigraph", false);
		graph.put("graph", new String[0]);
		List<Map<String, Object>> nodes = new ArrayList<>();
		List<Map<String, Integer>> links = new ArrayList<>();
		for (String microserviceName : dependencies.keySet()) {
			Map<String, Object> microservice = dependencies.get(microserviceName);
			nodes.add(createNode(microserviceName, new Integer("2"), null));
			int microserviceNodeId = nodes.size() - 1;
			for (Map.Entry<String, Object> dependencyEntrySet : microservice.entrySet()) {

				int dependencyNodeId = findDependencyNode(dependencyEntrySet.getKey(), nodes);
				if (dependencyNodeId == -1) {
					nodes.add(createNode(dependencyEntrySet.getKey(), new Integer("3"), dependencyEntrySet.getValue()));
					dependencyNodeId = nodes.size() - 1;
				}
				links.add(createLink(microserviceNodeId, dependencyNodeId));
			}
		}
		graph.put("lanes", constructLanes());
		graph.put("nodes", nodes);
		graph.put("links", links);

		return graph;
	}

	private int findDependencyNode(final String dependency, final List<Map<String, Object>> nodes) {
		for (Map<String, Object> map : nodes) {
			if (dependency.equals(map.get("id"))) {
				return nodes.indexOf(map);
			}
		}
		return -1;
	}

	private Map<String, Object> createNode(final String id, final Integer lane, Object details) {
		Map<String, Object> node = new HashMap<>();
		node.put("id", id);
		node.put("lane", lane);
		node.put("details", details);
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
		lanes.add(constructLane(0, "UI"));
		lanes.add(constructLane(1, "Endpoint"));
		lanes.add(constructLane(2, "Microservice"));
		lanes.add(constructLane(3, "Backend"));
		return lanes;
	}

	private Map<Object, Object> constructLane(final int lane, final String type) {
		Map<Object, Object> laneMap = newHashMap();
		laneMap.put("lane", lane);
		laneMap.put("type", type);
		return laneMap;
	}
}
