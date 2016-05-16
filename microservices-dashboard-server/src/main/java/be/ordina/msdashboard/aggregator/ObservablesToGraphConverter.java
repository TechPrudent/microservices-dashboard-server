package be.ordina.msdashboard.aggregator;

import be.ordina.msdashboard.aggregator.health.HealthIndicatorsAggregator;
import be.ordina.msdashboard.aggregator.index.IndexesAggregator;
import be.ordina.msdashboard.aggregator.pact.PactsAggregator;
import be.ordina.msdashboard.constants.Constants;
import be.ordina.msdashboard.model.Node;
import be.ordina.msdashboard.model.NodeBuilder;
import be.ordina.msdashboard.store.NodeStore;
import com.google.common.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import rx.Observable;
import rx.Subscription;
import rx.functions.Func1;

import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static be.ordina.msdashboard.constants.Constants.*;
import static com.google.common.base.CaseFormat.LOWER_CAMEL;
import static com.google.common.base.CaseFormat.LOWER_HYPHEN;
import static com.google.common.collect.Maps.newHashMap;

public class ObservablesToGraphConverter {

	private static final Logger LOG = LoggerFactory.getLogger(ObservablesToGraphConverter.class);

	private HealthIndicatorsAggregator healthIndicatorsAggregator;
	private IndexesAggregator indexesAggregator;
	private PactsAggregator pactsAggregator;

	private NodeStore redisService;

	private VirtualAndRealDependencyIntegrator virtualAndRealDependencyIntegrator;

	private final Map<String, Object> graph;

	@Autowired
	public ObservablesToGraphConverter(final HealthIndicatorsAggregator healthIndicatorsAggregator, final IndexesAggregator indexesAggregator,
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
		Node healthNode = healthIndicatorsAggregator.fetchCombinedDependencies();
		//Node indexNode = indexesAggregator.fetchIndexes();
		Collection<Node> virtualNodesFromRedis = redisService.getAllNodes();

		Observable<Node> microservicesAndBackends = Observable.from(healthNode.getLinkedToNodes());
		Observable<Node> resources = indexesAggregator.fetchIndexesWithObservable();
		Observable<Node> pactComponents = pactsAggregator.fetchPactNodesAsObservable();
		Observable<Node> virtualNodes = Observable.from(new Node[]{});
//		Observable.zip(microservicesAndBackends, resources, pactComponents, (m, r, p) -> {
//			return m;
//		});
		//virtualAndRealDependencyIntegrator.integrateVirtualNodesWithReal(microservicesAndBackends, resources, virtualNodes);

		return createGraph(microservicesAndBackends, resources, pactComponents);
	}

	private Map<String, Object> createGraph(final Observable<Node> microservicesWithTheirBackends, Observable<Node> resources, Observable<Node> pactComponents) {
		Observable<Map<String, Object>> displayableMicroservices = microservicesWithTheirBackends.map(node -> {
			Set<Map<String, Object>> displayableNodes = new HashSet<>();
			String microserviceName = convertMicroserviceName(node.getId());
			Map<String, Object> microserviceNode = createMicroserviceNode(microserviceName, node);
			displayableNodes.add(microserviceNode);
			Set<Node> dependencyNodes = node.getLinkedToNodes();
			removeEurekaDescription(dependencyNodes);
			for (Node dependencyNode : dependencyNodes) {
				if (MICROSERVICE.equals(dependencyNode.getDetails().get(TYPE))) {
					dependencyNode.setId(convertMicroserviceName(dependencyNode.getId()));
				}
				Integer lane = determineLane(dependencyNode.getDetails());
				displayableNodes.add(createNode(dependencyNode.getId(), lane, dependencyNode.getDetails()));
			}
			return Observable.from(displayableNodes);
		}).flatMap(el -> el);

		Observable<Node> mergedObservable = Observable.merge(microservicesWithTheirBackends, resources, pactComponents);
		Observable<Map<String, Object>> displayableNodesAndLinks = reduceToNodesAndLinksMap(mergedObservable).map(mapToDisplayableNode());
		displayableNodesAndLinks.subscribe(element -> {
			graph.put(NODES, element.get(NODES));
			graph.put(LINKS, element.get(LINKS));
		});

		return graph;
	}

	private Func1<Map<String, Object>, Map<String, Object>> mapToDisplayableNode() {
		return map -> {
			List<Node> nodes = (List<Node>) map.get(NODES);
			List<Map<String, Object>> displayableNodes = new ArrayList<>();
			for (Node node : nodes) {
				displayableNodes.add(createDisplayableNode(node));
			}
			map.replace(NODES, displayableNodes);
			return map;
		};
	}

	private Observable<Map<String, Object>> reduceToNodesAndLinksMap(Observable<Node> mergedObservable) {
		Map initialExistingNodes = new HashMap<>();
		initialExistingNodes.put(NODES, new ArrayList<>());
		initialExistingNodes.put(LINKS, new HashSet<>());
		Observable<Map<String, Object>> reduced = mergedObservable.reduce(initialExistingNodes, (Map<String, Object> returnedNodesAndLinks, Node node) -> {
				List<Node> existingNodes = (List<Node>) returnedNodesAndLinks.get(NODES);
				//System.out.println("Current existing nodes: " + existingNodes);
				int existingNodeIndex;
				Optional<Integer> nodeIndex = findNodeIndexByNode(existingNodes, node);

				if (!nodeIndex.isPresent()) {
					//System.out.println("NodeIndex not found, adding node: " + node);
					existingNodes.add(node);
					existingNodeIndex = existingNodes.size() - 1;
				} else {
					//System.out.println("NodeIndex found: " + nodeIndex.get() + ", node: "+ node);
					Node existingNode = existingNodes.get(nodeIndex.get());
					existingNode.mergeWith(node);
					existingNodeIndex = nodeIndex.get();
				}
				Set<Map<String, Integer>> links = (Set<Map<String, Integer>>) returnedNodesAndLinks.get(LINKS);
				Set<String> linkedToNodeIds = node.getLinkedToNodeIds();
				for (String nodeId : linkedToNodeIds) {
					nodeIndex = findNodeIndexById(existingNodes, nodeId);
					int existingLinkedNodeIndex;
					if (!nodeIndex.isPresent()) {
						existingNodes.add(createNodeById(nodeId));
						existingLinkedNodeIndex = existingNodes.size() - 1;
					} else {
						existingLinkedNodeIndex = nodeIndex.get();
					}
					links.add(createLink(existingNodeIndex, existingLinkedNodeIndex));
				}
				return returnedNodesAndLinks;
		});
		return reduced;
	}

	private Optional<Integer> findNodeIndexByNode(List<Node> existingNodes, Node node) {
		return findNodeIndexById(existingNodes, node.getId());
	}

	private Node createNodeById(String nodeId) {
		return NodeBuilder.node().withId(nodeId).build();
	}

	private Optional<Integer> findNodeIndexById(List<Node> existingNodes, String nodeId) {
		return existingNodes.stream()
				.filter(node -> node.getId().equals(nodeId))
				.map(node -> existingNodes.indexOf(node))
				.findFirst();
	}

	private String convertMicroserviceName(String name) {
		return LOWER_CAMEL.to(LOWER_HYPHEN, name);
	}

	@VisibleForTesting
	Integer determineLane(Map<String, Object> details) {
		String type = (String) details.get(TYPE);
		if (type == null) {
			return new Integer("3");
		}
		switch (type) {
			case UI_COMPONENT: return new Integer("0");
			case RESOURCE: return new Integer("1");
			case MICROSERVICE: return new Integer("2");
			default: return new Integer("3");
		}
	}

//	private Optional<Integer> findNodeIndex(final Map<String, Object> nodeToFind, final List<Map<String, Object>> nodes) {
//		for (int i = 0; i < nodes.size(); i++) {
//			Map<String, Object> node = nodes.get(i);
//			if (nodeToFind.get(ID).equals(node.get(ID))) {
//				return Optional.of(i);
//			}
//		}
//		return Optional.empty();
//	}

//	private int findNode(final String dependency, final List<Map<String, Object>> nodes) {
//		for (Map<String, Object> map : nodes) {
//			if (dependency.equals(map.get(ID))) {
//				return nodes.indexOf(map);
//			}
//		}
//		return -1;
//	}

//	private Optional<Integer> getNodeIndex(final List<Map<String, Object>> nodes, final String nodeId) {
//		for (int i = 0; i < nodes.size(); i++) {
//			Map<String, Object> node = nodes.get(i);
//			if (nodeId.equals(node.get(ID))) {
//				return Optional.of(i);
//			}
//		}
//		return Optional.empty();
//	}

//	private Optional<Integer> getNodeIndexByLane(final List<Map<String, Object>> nodes, final String nodeId, final int lane) {
//		for (int i = 0; i < nodes.size(); i++) {
//			Map<String, Object> node = nodes.get(i);
//			if (lane == (Integer) node.get(Constants.LANE) && nodeId.equals(node.get(ID))) {
//				return Optional.of(i);
//			}
//		}
//		return Optional.empty();
//	}

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

	private Map<String, Object> createDisplayableNode(final Node node) {
		Integer lane = determineLane(node.getDetails());
		return createNode(node.getId(), lane, node.getDetails());
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
		node.put(ID, id);
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
		types.add(MICROSERVICE);
		types.add(Constants.REST);
		types.add(Constants.SOAP);
		types.add(Constants.JMS);
		types.add(RESOURCE);
		return types;
	}

	private Map<Object, Object> constructLane(final int lane, final String type) {
		Map<Object, Object> laneMap = newHashMap();
		laneMap.put(Constants.LANE, lane);
		laneMap.put(TYPE, type);
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

	private void initGraph(final Map<String, Object> graph) {
		graph.put(Constants.DIRECTED, true);
		graph.put(Constants.MULTIGRAPH, false);
		graph.put(Constants.GRAPH, new String[0]);
		graph.put(Constants.LANES, constructLanes());
		graph.put(Constants.TYPES, constructTypes());
	}
}
