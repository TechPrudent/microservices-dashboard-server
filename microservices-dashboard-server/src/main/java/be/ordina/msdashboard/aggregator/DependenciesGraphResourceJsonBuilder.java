package be.ordina.msdashboard.aggregator;

import static com.google.common.collect.Maps.newHashMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import be.ordina.msdashboard.constants.Constants;
import be.ordina.msdashboard.model.Node;
import be.ordina.msdashboard.services.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.annotations.VisibleForTesting;

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

	private HealthIndicatorsAggregator healthIndicatorsAggregator;

	private RedisService redisService;

	private VirtualAndRealDependencyIntegrator virtualAndRealDependencyIntegrator;

	private final Map<String, Object> graph;

	@Autowired
	public DependenciesGraphResourceJsonBuilder(final HealthIndicatorsAggregator healthIndicatorsAggregator, final RedisService redisService,
			final VirtualAndRealDependencyIntegrator virtualAndRealDependencyIntegrator) {
		this.healthIndicatorsAggregator = healthIndicatorsAggregator;
		this.redisService = redisService;
		this.virtualAndRealDependencyIntegrator = virtualAndRealDependencyIntegrator;
		graph = new HashMap<>();
		initGraph(graph);
	}

	public Map<String, Object> build() {
		Node node = healthIndicatorsAggregator.fetchCombinedDependencies();
		List<Node> dependencies = node.getLinkedNodes();
		List<Node> virtualDependencies = redisService.getAllNodes();
		if (!virtualDependencies.isEmpty()) {
			virtualAndRealDependencyIntegrator.integrateVirtualNodesToReal(dependencies, virtualDependencies);
		}
		return createGraph(dependencies);
	}

	private Map<String, Object> createGraph(final List<Node> dependencies) {
		List<Map<String, Object>> nodes = new ArrayList<>();
		List<Map<String, Integer>> links = new ArrayList<>();

		for (Node microservice : dependencies) {
			String microserviceName = microservice.getId();
			Map<String, Object> microserviceNode = createMicroserviceNode(microserviceName, microservice);
			if (!isNodeAlreadyThere(nodes, microserviceName)) {
				nodes.add(microserviceNode);
			}
			int microserviceNodeId = nodes.size() - 1;
			List<Node> dependencyNodes = microservice.getLinkedNodes();
			removeEurekaDescription(dependencyNodes);
			for (Node dependencyNode : dependencyNodes) {
				int dependencyNodeId = findDependencyNode(dependencyNode.getId(), nodes);
				if (dependencyNodeId == -1) {
					Integer lane;
					lane = determineLane(dependencyNode.getDetails());
					nodes.add(createNode(dependencyNode.getId(), lane, dependencyNode.getDetails()));
					dependencyNodeId = nodes.size() - 1;
				}
				links.add(createLink(microserviceNodeId, dependencyNodeId));
			}
		}
		graph.put(NODES, nodes);
		graph.put(LINKS, links);
		return graph;
	}

	@VisibleForTesting
	Integer determineLane(Map<String, Object> details) {
		if (Constants.MICROSERVICE.equals(details.get(Constants.TYPE))) {
			return new Integer("2");
		}
		return new Integer("3");
	}

	private int findDependencyNode(final String dependency, final List<Map<String, Object>> nodes) {
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

	private Map<String, Object> createNode(final String id, final Integer lane, Map<String, Object> details) {
		Map<String, Object> node = new HashMap<>();
		node.put(Constants.ID, id);
		node.put(Constants.LANE, lane);
		node.put(Constants.DETAILS, details);
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
		lanes.add(constructLane(2, Constants.MICROSERVICE));
		lanes.add(constructLane(3, BACKEND));
		return lanes;
	}

	private Map<Object, Object> constructLane(final int lane, final String type) {
		Map<Object, Object> laneMap = newHashMap();
		laneMap.put(Constants.LANE, lane);
		laneMap.put(Constants.TYPE, type);
		return laneMap;
	}

	private void removeEurekaDescription(final List<Node> dependencyNodes) {
		for (Node dependencyNode : dependencyNodes) {
			if (DESCRIPTION.equals(dependencyNode.getId())) {
				dependencyNodes.remove(dependencyNode);
				break;
			}
		}
	}

	private boolean isNodeAlreadyThere(final List<Map<String, Object>> nodes, final String microserviceId) {
		for (Map<String, Object> node : nodes) {
			if (microserviceId.equals(node.get(Constants.ID))) {
				return true;
			}
		}
		return false;
	}

	private void initGraph(final Map<String, Object> graph) {
		graph.put(DIRECTED, true);
		graph.put(MULTIGRAPH, false);
		graph.put(GRAPH, new String[0]);
		graph.put(LANES, constructLanes());
	}
}
