package com.pxs.dependencies.aggregator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
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
		List<Map<String, String>> nodes = new ArrayList<>();
		List<Map<String, Integer>> links = new ArrayList<>();
		for (String microserviceName : dependencies.keySet()) {
			Map<String, Object> microservice = dependencies.get(microserviceName);
			nodes.add(createNode(microserviceName));
			int microserviceNodeId = nodes.size() - 1;
			for (String dependency : microservice.keySet()) {
				int dependencyNodeId = findDependencyNode(dependency, nodes);
				if (dependencyNodeId == -1) {
					nodes.add(createNode(dependency));
					dependencyNodeId = nodes.size() - 1;
				}
				links.add(createLink(microserviceNodeId, dependencyNodeId));
			}
		}
		graph.put("nodes", nodes);
		graph.put("links", links);
		return graph;
	}

	private int findDependencyNode(final String dependency, final List<Map<String, String>> nodes) {
		for (Map<String, String> map : nodes) {
			if (dependency.equals(map.get("id"))) {
				return nodes.indexOf(map);
			}
		}
		return -1;
	}

	private Map<String, String> createNode(final String id) {
		Map<String, String> node = new HashMap<>();
		node.put("id", id);
		return node;
	}

	private Map<String, Integer> createLink(final int source, final int target) {
		Map<String, Integer> link = new HashMap<>();
		link.put("source", source);
		link.put("target", target);
		return link;
	}
}
